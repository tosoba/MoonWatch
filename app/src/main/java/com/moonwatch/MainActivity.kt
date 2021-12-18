package com.moonwatch

import android.content.ClipData
import android.content.ClipboardManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.activity.compose.setContent
import androidx.annotation.DrawableRes
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.getSystemService
import androidx.hilt.navigation.compose.hiltViewModel
import coil.annotation.ExperimentalCoilApi
import coil.compose.ImagePainter
import coil.compose.rememberImagePainter
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.moonwatch.core.android.model.*
import com.moonwatch.model.Token
import com.moonwatch.model.TokenAlertWithValue
import com.moonwatch.model.TokenWithValue
import com.moonwatch.ui.theme.MoonWatchTheme
import com.moonwatch.ui.theme.Purple700
import com.moonwatch.ui.theme.Typography
import dagger.hilt.android.AndroidEntryPoint
import java.io.IOException
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*
import kotlinx.coroutines.*
import retrofit2.HttpException

@AndroidEntryPoint
@OptIn(
    ExperimentalCoilApi::class,
    ExperimentalCoroutinesApi::class,
    ExperimentalMaterialApi::class,
    ExperimentalPagerApi::class,
    FlowPreview::class,
)
class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      MoonWatchTheme { Surface(color = MaterialTheme.colors.background) { MainScaffold() } }
    }
  }
}

private enum class BottomSheetDialogMode {
  ADD_TOKEN,
  VIEW_TOKEN,
  ADD_ALERT,
  EDIT_ALERT,
}

@Composable
@OptIn(
    ExperimentalCoilApi::class,
    ExperimentalCoroutinesApi::class,
    ExperimentalMaterialApi::class,
    ExperimentalPagerApi::class,
    FlowPreview::class,
)
private fun MainScaffold(viewModel: MainViewModel = hiltViewModel()) {
  val scope = rememberCoroutineScope()
  val pageState = rememberPagerState()
  val scaffoldState = rememberScaffoldState()
  val modalBottomSheetState =
      rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)
  val items = MainBottomNavigationItem.values()
  var bottomSheetDialogMode by rememberSaveable { mutableStateOf(BottomSheetDialogMode.ADD_TOKEN) }

  BackHandler(enabled = modalBottomSheetState.isVisible) {
    scope.launch { modalBottomSheetState.hide() }
  }
  ModalBottomSheetLayout(
      sheetContent = {
        when (bottomSheetDialogMode) {
          BottomSheetDialogMode.ADD_TOKEN -> SaveTokenBottomSheetContent(modalBottomSheetState)
          BottomSheetDialogMode.VIEW_TOKEN -> {
            ViewTokenBottomSheetContent(
                onAddAlertClick = { bottomSheetDialogMode = BottomSheetDialogMode.ADD_ALERT },
            )
          }
          BottomSheetDialogMode.ADD_ALERT -> AddAlertBottomSheetContent(modalBottomSheetState)
          BottomSheetDialogMode.EDIT_ALERT -> Box(modifier = Modifier.size(20.dp))
        }
      },
      sheetState = modalBottomSheetState,
  ) {
    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
          TopAppBar {
            Text(
                text = "MoonWatch",
                style = Typography.h6.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(horizontal = 5.dp),
            )
          }
        },
        bottomBar = {
          BottomAppBar {
            items.forEachIndexed { index, item ->
              BottomNavigationItem(
                  icon = {
                    Icon(
                        painter = painterResource(id = item.drawableResource),
                        contentDescription = item.title,
                    )
                  },
                  selected = index == pageState.currentPage,
                  onClick = { scope.launch { pageState.animateScrollToPage(index) } },
                  selectedContentColor = Color.Magenta,
                  unselectedContentColor = Color.LightGray,
                  label = { Text(text = item.title) },
              )
            }
          }
        },
        floatingActionButton = {
          FloatingActionButton(
              onClick = {
                bottomSheetDialogMode = BottomSheetDialogMode.ADD_TOKEN
                scope.launch { modalBottomSheetState.show() }
              },
          ) { Icon(Icons.Filled.Add, "") }
        },
    ) {
      HorizontalPager(state = pageState, count = items.size) { page ->
        when (items[page]) {
          MainBottomNavigationItem.TOKENS -> {
            TokensWithValueList(
                onItemClick = {
                  bottomSheetDialogMode = BottomSheetDialogMode.VIEW_TOKEN
                  viewModel.setTokenWithValueBeingViewed(it)
                  scope.launch { modalBottomSheetState.show() }
                },
            )
          }
          MainBottomNavigationItem.ALERTS -> TokenAlertsList()
        }
      }
    }
  }
}

@Composable
@OptIn(ExperimentalCoroutinesApi::class, ExperimentalMaterialApi::class, FlowPreview::class)
private fun ViewTokenBottomSheetContent(
    onAddAlertClick: (TokenWithValue) -> Unit,
    viewModel: MainViewModel = hiltViewModel()
) {
  val tokenWithValue: TokenWithValue =
      viewModel.tokenWithValueBeingViewed.value ?: throw IllegalArgumentException()
  Column(modifier = Modifier.padding(horizontal = 10.dp)) {
    Box(modifier = Modifier.height(15.dp))
    TokenValueBottomSheetColumnContent(tokenWithValue)
    OutlinedButton(
        onClick = { onAddAlertClick(tokenWithValue) },
        modifier = Modifier.fillMaxWidth(),
    ) { Text(text = "Create an alert") }
    Box(modifier = Modifier.height(15.dp))
  }
}

@Composable
private fun TokenValueBottomSheetColumnContent(tokenWithValue: TokenWithValue) {
  ViewTokenBottomSheetTextField(
      value = tokenWithValue.token.address,
      label = "Address",
      toastText = "Copied token address",
  )
  ViewTokenBottomSheetTextField(
      value = tokenWithValue.token.name,
      label = "Name",
      toastText = "Copied token name",
  )
  ViewTokenBottomSheetTextField(
      value = tokenWithValue.value.usd.toString(),
      label = "Value in USD",
      toastText = "Copied token value",
  )
}

@Composable
private fun ViewTokenBottomSheetTextField(value: String, label: String, toastText: String) {
  OutlinedTextField(
      value = value,
      onValueChange = {},
      readOnly = true,
      label = { Text(label) },
      singleLine = true,
      trailingIcon = { CopyIconButton(value, toastText) },
      modifier = Modifier.fillMaxWidth(),
  )
}

@Composable
private fun CopyIconButton(text: String, toastText: String) {
  val context = LocalContext.current
  IconButton(
      onClick = {
        val clip = ClipData.newPlainText("view_token_copied_value", text)
        getSystemService(context, ClipboardManager::class.java)?.let { manager ->
          manager.setPrimaryClip(clip)
          Toast.makeText(context, toastText, Toast.LENGTH_SHORT).show()
        }
      },
  ) { Icon(painterResource(R.drawable.ic_baseline_content_copy_24), "") }
}

@Composable
@OptIn(ExperimentalCoroutinesApi::class, ExperimentalMaterialApi::class, FlowPreview::class)
fun AddAlertBottomSheetContent(
    modalBottomSheetState: ModalBottomSheetState,
    viewModel: MainViewModel = hiltViewModel(),
) {
  val tokenWithValue = viewModel.tokenWithValueBeingViewed.value ?: throw IllegalArgumentException()
  val (token, tokenValue) = tokenWithValue
  val tokenValueScale = 0.coerceAtLeast(tokenValue.usd.stripTrailingZeros().scale())

  var sellTarget by rememberSaveable { mutableStateOf("") }
  var buyTarget by rememberSaveable { mutableStateOf("") }

  fun isTargetValid(target: String): Boolean = target.toBigDecimalOrNull() != null
  fun isTargetValidOrEmpty(target: String): Boolean = target.isEmpty() || isTargetValid(target)

  var sellTargetX by rememberSaveable { mutableStateOf(BigDecimal.ONE) }
  var buyTargetX by rememberSaveable { mutableStateOf(BigDecimal.ONE) }

  fun BigDecimal.toStringInTokenValueScale(): String =
      stripTrailingZeros().setScale(tokenValueScale, RoundingMode.HALF_UP).toPlainString()

  val scrollState = rememberScrollState()
  val scope = rememberCoroutineScope()

  var priceTargetValidationMessages by rememberSaveable {
    mutableStateOf<List<String>>(emptyList())
  }
  if (priceTargetValidationMessages.isNotEmpty()) {
    PriceTargetValidationMessagesDialog(messages = priceTargetValidationMessages) {
      priceTargetValidationMessages = emptyList()
    }
  }

  Column(
      modifier = Modifier.padding(horizontal = 10.dp).verticalScroll(scrollState),
  ) {
    Box(modifier = Modifier.height(15.dp))

    BottomSheetContentTitleText(text = "Add a new alert")
    TokenValueBottomSheetColumnContent(tokenWithValue)

    OutlinedTextField(
        value = buyTarget,
        onValueChange = {
          buyTarget = it
          buyTargetX =
              if (isTargetValid(buyTarget)) {
                buyTarget.toBigDecimal() / tokenValue.usd
              } else {
                BigDecimal.ONE
              }
        },
        isError =
            !isTargetValidOrEmpty(buyTarget) ||
                (isTargetValid(buyTarget) && buyTargetX >= BigDecimal.ONE),
        label = { Text(text = "Buy target") },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        trailingIcon = {
          IconButton(
              onClick = {
                buyTarget = ""
                buyTargetX = BigDecimal.ONE
              },
          ) { Icon(Icons.Default.Clear, "") }
        },
        modifier = Modifier.fillMaxWidth(),
    )
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 5.dp).fillMaxWidth(),
    ) {
      OutlinedButton(
          enabled = buyTargetX > BigDecimal(0.1) && isTargetValidOrEmpty(buyTarget),
          onClick = {
            buyTargetX -= BigDecimal(0.1)
            buyTarget = (tokenValue.usd * buyTargetX).toStringInTokenValueScale()
          },
          modifier = Modifier.weight(1f),
      ) { Text("-0.1X") }
      OutlinedButton(
          enabled = buyTargetX < BigDecimal.ONE && isTargetValidOrEmpty(buyTarget),
          onClick = {
            buyTargetX += BigDecimal(0.1)
            buyTarget = (tokenValue.usd * buyTargetX).toStringInTokenValueScale()
          },
          modifier = Modifier.weight(1f),
      ) { Text("+0.1X") }
      PriceTargetXText(buyTargetX)
    }

    OutlinedTextField(
        value = sellTarget,
        onValueChange = {
          sellTarget = it
          sellTargetX =
              if (isTargetValid(sellTarget)) {
                sellTarget.toBigDecimal() / tokenValue.usd
              } else {
                BigDecimal.ONE
              }
        },
        isError =
            !isTargetValidOrEmpty(sellTarget) ||
                (isTargetValid(sellTarget) && sellTargetX <= BigDecimal.ONE),
        label = { Text(text = "Sell target") },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        trailingIcon = {
          IconButton(
              onClick = {
                sellTarget = ""
                sellTargetX = BigDecimal.ONE
              },
          ) { Icon(Icons.Default.Clear, "") }
        },
        modifier = Modifier.fillMaxWidth(),
    )
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 5.dp).fillMaxWidth(),
    ) {
      OutlinedButton(
          enabled = sellTargetX > BigDecimal(2.0) && isTargetValid(sellTarget),
          onClick = {
            sellTargetX -= BigDecimal.ONE
            sellTarget = (tokenValue.usd * sellTargetX).toStringInTokenValueScale()
          },
          modifier = Modifier.weight(1f),
      ) { Text("-1X") }
      OutlinedButton(
          enabled = sellTargetX > BigDecimal.ONE && isTargetValidOrEmpty(sellTarget),
          onClick = {
            sellTargetX -= BigDecimal(0.1)
            sellTarget = (tokenValue.usd * sellTargetX).toStringInTokenValueScale()
          },
          modifier = Modifier.weight(1f),
      ) { Text("-0.1X") }
      OutlinedButton(
          enabled = isTargetValidOrEmpty(sellTarget),
          onClick = {
            sellTargetX += BigDecimal(0.1)
            sellTarget = (tokenValue.usd * sellTargetX).toStringInTokenValueScale()
          },
          modifier = Modifier.weight(1f),
      ) { Text("+0.1X") }
      OutlinedButton(
          enabled = isTargetValidOrEmpty(sellTarget),
          onClick = {
            sellTargetX += BigDecimal.ONE
            sellTarget = (tokenValue.usd * sellTargetX).toStringInTokenValueScale()
          },
          modifier = Modifier.weight(1f),
      ) { Text("+1X") }
      PriceTargetXText(sellTargetX)
    }

    val context = LocalContext.current
    OutlinedButton(
        onClick = {
          val validationMessages = mutableListOf<String>()
          if (buyTarget.isBlank() && sellTarget.isBlank()) {
            validationMessages.add("You must specify either a buy or a sell price target.")
          } else {
            if (isTargetValid(buyTarget) && buyTarget.toBigDecimal() >= tokenValue.usd) {
              validationMessages.add(
                  "Chosen buy price target is larger or equal to current token price.")
            }
            if (isTargetValid(sellTarget) && sellTarget.toBigDecimal() <= tokenValue.usd) {
              validationMessages.add(
                  "Chosen sell price target is less or equal to current token price.")
            }
          }

          if (validationMessages.isEmpty()) {
            viewModel.addAlert(
                token.address,
                sellPriceTargetUsd = sellTarget.toBigDecimalOrNull(),
                buyPriceTargetUsd = buyTarget.toBigDecimalOrNull(),
            )
            scope.launch { modalBottomSheetState.hide() }
            Toast.makeText(context, "Alert was created.", Toast.LENGTH_SHORT).show()
          } else {
            priceTargetValidationMessages = validationMessages
          }
        },
        modifier = Modifier.fillMaxWidth(),
    ) { Text(text = "Add alert") }

    Box(modifier = Modifier.height(15.dp))
  }
}

@Composable
fun PriceTargetValidationMessagesDialog(messages: List<String>, dismiss: () -> Unit) {
  if (messages.isEmpty()) throw IllegalArgumentException()
  val scrollState = rememberScrollState()
  AlertDialog(
      onDismissRequest = dismiss,
      title = { Text(text = "Price target validation failed.", fontWeight = FontWeight.Bold) },
      text = {
        Column(modifier = Modifier.verticalScroll(scrollState)) {
          for (message in messages) Text(text = message)
        }
      },
      buttons = {
        Row(modifier = Modifier.padding(all = 8.dp)) {
          OutlinedButton(modifier = Modifier.fillMaxWidth(), onClick = dismiss) { Text("OK") }
        }
      },
  )
}

@Composable
private fun PriceTargetXText(targetX: BigDecimal?) {
  Box(
      contentAlignment = Alignment.Center,
      modifier = Modifier.padding(horizontal = 3.dp).fillMaxHeight(),
  ) {
    Text(
        text = "${String.format("%.02f", targetX)}X",
        style = Typography.subtitle1.copy(fontWeight = FontWeight.Bold),
    )
  }
}

@Composable
@OptIn(ExperimentalCoroutinesApi::class, ExperimentalMaterialApi::class, FlowPreview::class)
private fun SaveTokenBottomSheetContent(
    modalBottomSheetState: ModalBottomSheetState,
    viewModel: MainViewModel = hiltViewModel()
) {
  val scope = rememberCoroutineScope()
  Column(modifier = Modifier.padding(vertical = 15.dp, horizontal = 10.dp)) {
    BottomSheetContentTitleText("Add a new token")
    val tokenAddress = viewModel.tokenAddress.collectAsState(initial = "")
    OutlinedTextField(
        value = tokenAddress.value,
        onValueChange = viewModel::setTokenAddress,
        label = { Text("Address") },
        singleLine = true,
        isError = viewModel.tokenWithValueBeingAdded.value.loadable is Failed,
        modifier = Modifier.fillMaxWidth(),
    )
    when (val tokenWithValue = viewModel.tokenWithValueBeingAdded.value.loadable) {
      is Failed -> {
        when (val error = tokenWithValue.error) {
          is HttpException -> {
            if (error.code() == 404) {
              Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
                Text(text = "Token not found.", textAlign = TextAlign.Center)
              }
            } else {
              Toast.makeText(
                      LocalContext.current,
                      "Unknown network error.",
                      Toast.LENGTH_SHORT,
                  )
                  .show()
              RetryLoadingTokenButton(scope)
            }
          }
          is TimeoutCancellationException -> {
            Toast.makeText(LocalContext.current, "Request has timed out.", Toast.LENGTH_SHORT)
                .show()
            RetryLoadingTokenButton(scope)
          }
          is IOException -> {
            Toast.makeText(
                    LocalContext.current,
                    "No internet connection.",
                    Toast.LENGTH_SHORT,
                )
                .show()
            RetryLoadingTokenButton(scope)
          }
          is InvalidAddressException -> {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
              Text(text = "Invalid token address.", textAlign = TextAlign.Center)
            }
          }
          else -> {
            Toast.makeText(LocalContext.current, "Unknown error.", Toast.LENGTH_SHORT).show()
            RetryLoadingTokenButton(scope)
          }
        }
      }
      is Ready -> {
        OutlinedTextField(
            value = tokenWithValue.value.token.name,
            onValueChange = {},
            label = { Text("Name") },
            singleLine = true,
            readOnly = true,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = tokenWithValue.value.value.usd.toString(),
            onValueChange = {},
            label = { Text("Value in USD") },
            singleLine = true,
            readOnly = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Row(horizontalArrangement = Arrangement.SpaceAround) {
          OutlinedButton(
              onClick = {
                scope.launch {
                  viewModel.clearTokenBeingAddedAddress()
                  modalBottomSheetState.hide()
                }
              },
              modifier = Modifier.weight(1f),
          ) { Text(text = "Cancel") }
          Box(modifier = Modifier.size(5.dp))
          OutlinedButton(
              onClick = {
                scope.launch {
                  viewModel.saveCurrentToken()
                  modalBottomSheetState.hide()
                }
              },
              modifier = Modifier.weight(1f),
          ) { Text(text = "Save") }
        }
      }
      is LoadingInProgress -> {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxWidth().padding(10.dp),
        ) { CircularProgressIndicator() }
      }
      else -> return@Column
    }
  }
}

@Composable
private fun BottomSheetContentTitleText(text: String) {
  Text(
      text = text,
      style =
          Typography.h6.copy(
              color = MaterialTheme.colors.primary,
              fontWeight = FontWeight.Bold,
          ),
      modifier = Modifier.padding(horizontal = 5.dp),
  )
}

@Composable
@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
private fun RetryLoadingTokenButton(
    scope: CoroutineScope,
    viewModel: MainViewModel = hiltViewModel()
) {
  OutlinedButton(
      onClick = { scope.launch { viewModel.retryLoadingToken() } },
      modifier = Modifier.fillMaxWidth(),
  ) { Text(text = "Retry") }
}

@Composable
@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
private fun DeleteItemDialog(itemName: String, dismiss: () -> Unit, delete: () -> Unit) {
  AlertDialog(
      onDismissRequest = dismiss,
      title = { Text(text = "Delete item", fontWeight = FontWeight.Bold) },
      text = { Text(text = "Do you really want to delete $itemName?") },
      buttons = {
        val context = LocalContext.current
        Row(
            modifier = Modifier.padding(all = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
          OutlinedButton(
              modifier = Modifier.weight(1f),
              onClick = {
                delete()
                dismiss()
                Toast.makeText(context, "$itemName was deleted.", Toast.LENGTH_SHORT).show()
              },
          ) { Text("Confirm") }
          Box(Modifier.size(5.dp))
          OutlinedButton(
              modifier = Modifier.weight(1f),
              onClick = dismiss,
          ) { Text("Cancel") }
        }
      },
  )
}

@Composable
@OptIn(
    ExperimentalCoroutinesApi::class,
    ExperimentalMaterialApi::class,
    ExperimentalPagerApi::class,
    FlowPreview::class,
)
private fun TokenAlertsList(viewModel: MainViewModel = hiltViewModel()) {
  var tokenAlertBeingDeleted by rememberSaveable { mutableStateOf<TokenAlertWithValue?>(null) }
  tokenAlertBeingDeleted?.let { tokenAlertWithValue ->
    DeleteItemDialog(
        itemName = "${tokenAlertWithValue.token.name} alert",
        dismiss = { tokenAlertBeingDeleted = null },
        delete = { viewModel.deleteAlert(tokenAlertWithValue.alert.id) },
    )
  }

  val alerts = viewModel.alertsFlow.collectAsState(initial = emptyList())
  if (alerts.value.isEmpty()) {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
      Text(text = "No saved alerts.", textAlign = TextAlign.Center)
    }
  } else {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
      items(alerts.value) {
        ListItem {
          TokenAlertWithValueListItem(
              tokenAlertWithValue = it,
              onItemClick = {},
              onDeleteClick = { tokenAlertBeingDeleted = it },
          )
        }
      }
    }
  }
}

@Composable
@OptIn(
    ExperimentalCoilApi::class,
    ExperimentalCoroutinesApi::class,
    ExperimentalMaterialApi::class,
    FlowPreview::class,
)
private fun TokenAlertWithValueListItem(
    tokenAlertWithValue: TokenAlertWithValue,
    onItemClick: (TokenAlertWithValue) -> Unit,
    onDeleteClick: (TokenAlertWithValue) -> Unit,
    viewModel: MainViewModel = hiltViewModel()
) {
  ListItem(
      icon = { TokenIcon(tokenAlertWithValue.token) },
      secondaryText = {
        Row(horizontalArrangement = Arrangement.Start) {
          Text(
              text = "$",
              style = Typography.subtitle2,
          )
          Text(
              text = tokenAlertWithValue.value.usd.toPlainString(),
              style = Typography.subtitle2,
              maxLines = 1,
              modifier = Modifier.weight(1f),
          )
        }
      },
      trailing = {
        Row {
          IconButton(onClick = { onDeleteClick(tokenAlertWithValue) }) {
            Icon(Icons.Outlined.Delete, "")
          }
          Switch(
              checked = tokenAlertWithValue.alert.active,
              onCheckedChange = { viewModel.toggleAlertActive(tokenAlertWithValue.alert.id) },
          )
        }
      },
      modifier = Modifier.clickable { onItemClick(tokenAlertWithValue) },
  ) {
    Text(
        text = tokenAlertWithValue.token.name,
        style = Typography.subtitle1.copy(fontWeight = FontWeight.Bold),
    )
  }
}

@Composable
@OptIn(
    ExperimentalCoilApi::class,
    ExperimentalCoroutinesApi::class,
    ExperimentalMaterialApi::class,
    ExperimentalPagerApi::class,
    FlowPreview::class,
)
private fun TokensWithValueList(
    onItemClick: (TokenWithValue) -> Unit,
    viewModel: MainViewModel = hiltViewModel()
) {
  var tokenBeingDeleted by rememberSaveable { mutableStateOf<Token?>(null) }
  tokenBeingDeleted?.let {
    DeleteItemDialog(
        itemName = "${it.name} with all associated alerts",
        dismiss = { tokenBeingDeleted = null },
        delete = { viewModel.deleteToken(it.address) },
    )
  }

  val tokens = viewModel.tokensFlow.collectAsState(initial = emptyList())
  if (tokens.value.isEmpty()) {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
      Text(text = "No saved tokens.", textAlign = TextAlign.Center)
    }
  } else {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
      items(tokens.value) { tokenWithValue ->
        TokenWithValueListItem(
            tokenWithValue = tokenWithValue,
            onItemClick = onItemClick,
            onDeleteClick = { tokenBeingDeleted = it },
        )
      }
    }
  }
}

@Composable
@OptIn(
    ExperimentalCoilApi::class,
    ExperimentalCoroutinesApi::class,
    ExperimentalMaterialApi::class,
    FlowPreview::class,
)
private fun TokenWithValueListItem(
    tokenWithValue: TokenWithValue,
    onItemClick: (TokenWithValue) -> Unit,
    onDeleteClick: (Token) -> Unit
) {
  ListItem(
      icon = { TokenIcon(tokenWithValue.token) },
      secondaryText = {
        Row(horizontalArrangement = Arrangement.Start) {
          Text(
              text = "$",
              style = Typography.subtitle2,
          )
          Text(
              text = tokenWithValue.value.usd.toPlainString(),
              style = Typography.subtitle2,
              maxLines = 1,
              modifier = Modifier.weight(1f),
          )
        }
      },
      trailing = {
        IconButton(onClick = { onDeleteClick(tokenWithValue.token) }) {
          Icon(Icons.Outlined.Delete, "")
        }
      },
      modifier = Modifier.clickable { onItemClick(tokenWithValue) },
  ) {
    Text(
        text = tokenWithValue.token.name,
        style = Typography.subtitle1.copy(fontWeight = FontWeight.Bold),
    )
  }
}

@Composable
@OptIn(ExperimentalCoilApi::class)
private fun TokenIcon(token: Token) {
  // TODO: fix coil
  val painter =
      rememberImagePainter("https://r.poocoin.app/smartchain/assets/${token.address}/logo.png")
  val state = painter.state
  if (state is ImagePainter.State.Success) {
    Image(painter = painter, contentDescription = token.name, modifier = Modifier.size(40.dp))
  } else {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(40.dp).clip(CircleShape).background(Purple700),
    ) {
      Text(
          text = token.name.substring(0, 1),
          style = Typography.h6.copy(fontWeight = FontWeight.Bold),
          color = Color.White,
      )
    }
  }
}

@Composable
private fun BackHandler(enabled: Boolean = true, onBack: () -> Unit) {
  val currentOnBack by rememberUpdatedState(onBack)
  val backCallback = remember {
    object : OnBackPressedCallback(enabled) {
      override fun handleOnBackPressed() {
        currentOnBack()
      }
    }
  }
  SideEffect { backCallback.isEnabled = enabled }
  val backDispatcher =
      checkNotNull(LocalOnBackPressedDispatcherOwner.current) {
            "No OnBackPressedDispatcherOwner was provided via LocalOnBackPressedDispatcherOwner"
          }
          .onBackPressedDispatcher
  val lifecycleOwner = LocalLifecycleOwner.current
  DisposableEffect(lifecycleOwner, backDispatcher) {
    backDispatcher.addCallback(lifecycleOwner, backCallback)
    onDispose(backCallback::remove)
  }
}

private enum class MainBottomNavigationItem(@DrawableRes val drawableResource: Int) {
  TOKENS(R.drawable.ic_baseline_account_balance_wallet_24),
  ALERTS(R.drawable.ic_baseline_notifications_24);

  val title: String
    get() =
        name.lowercase(Locale.getDefault()).replaceFirstChar {
          if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
        }
}
