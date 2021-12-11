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
import com.moonwatch.model.TokenWithValue
import com.moonwatch.ui.theme.MoonWatchTheme
import com.moonwatch.ui.theme.Purple700
import com.moonwatch.ui.theme.Typography
import dagger.hilt.android.AndroidEntryPoint
import java.io.IOException
import java.math.BigDecimal
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
          BottomSheetDialogMode.ADD_TOKEN -> AddTokenBottomSheetContent(modalBottomSheetState)
          BottomSheetDialogMode.VIEW_TOKEN -> {
            ViewTokenBottomSheetContent(
                onAddAlertClick = { bottomSheetDialogMode = BottomSheetDialogMode.ADD_ALERT },
            )
          }
          BottomSheetDialogMode.ADD_ALERT -> AddAlertBottomSheetContent()
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
          MainBottomNavigationItem.ALERTS -> AlertsList()
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
  Column(modifier = Modifier.padding(vertical = 15.dp, horizontal = 10.dp)) {
    TokenValueBottomSheetColumnContent(tokenWithValue)
    OutlinedButton(
        onClick = { onAddAlertClick(tokenWithValue) },
        modifier = Modifier.fillMaxWidth(),
    ) { Text(text = "Create an alert") }
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
@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
fun AddAlertBottomSheetContent(viewModel: MainViewModel = hiltViewModel()) {
  val tokenWithValue = viewModel.tokenWithValueBeingViewed.value ?: throw IllegalArgumentException()

  var sellTarget by rememberSaveable { mutableStateOf("") }
  var buyTarget by rememberSaveable { mutableStateOf("") }

  fun isTargetValid(target: String): Boolean = target.toDoubleOrNull() != null
  fun isTargetValidOrEmpty(target: String): Boolean = target.isEmpty() || isTargetValid(target)

  var sellTargetX by rememberSaveable { mutableStateOf(BigDecimal.ONE) }
  var buyTargetX by rememberSaveable { mutableStateOf(BigDecimal.ONE) }

  val scrollState = rememberScrollState()

  Column(
      modifier = Modifier.padding(vertical = 15.dp, horizontal = 10.dp).verticalScroll(scrollState),
  ) {
    BottomSheetContentTitleText(text = "Add a new alert")
    TokenValueBottomSheetColumnContent(tokenWithValue)

    OutlinedTextField(
        value = buyTarget,
        onValueChange = {
          buyTarget = it
          buyTargetX =
              if (isTargetValid(buyTarget)) {
                buyTarget.toBigDecimal() / tokenWithValue.value.usd
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
            buyTarget = (tokenWithValue.value.usd * buyTargetX).toString()
          },
          modifier = Modifier.weight(1f),
      ) { Text("-0.1X") }
      OutlinedButton(
          enabled = buyTargetX < BigDecimal.ONE && isTargetValidOrEmpty(buyTarget),
          onClick = {
            buyTargetX += BigDecimal(0.1)
            buyTarget = (tokenWithValue.value.usd * buyTargetX).toString()
          },
          modifier = Modifier.weight(1f),
      ) { Text("0.1X") }
      Box(
          contentAlignment = Alignment.Center,
          modifier = Modifier.padding(horizontal = 3.dp).fillMaxHeight(),
      ) {
        Text(
            text = "${String.format("%.02f", buyTargetX)}X",
            style = Typography.subtitle1.copy(fontWeight = FontWeight.Bold),
        )
      }
    }

    OutlinedTextField(
        value = sellTarget,
        onValueChange = {
          sellTarget = it
          sellTargetX =
              if (isTargetValid(sellTarget)) {
                sellTarget.toBigDecimal() / tokenWithValue.value.usd
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
          enabled = sellTargetX > BigDecimal.ONE && isTargetValid(sellTarget),
          onClick = {
            sellTargetX -= BigDecimal.ONE
            sellTarget = (tokenWithValue.value.usd * sellTargetX).toString()
          },
          modifier = Modifier.weight(1f),
      ) { Text("-1X") }
      OutlinedButton(
          enabled = sellTargetX > BigDecimal.ONE && isTargetValidOrEmpty(sellTarget),
          onClick = {
            sellTargetX -= BigDecimal(0.1)
            sellTarget = (tokenWithValue.value.usd * sellTargetX).toString()
          },
          modifier = Modifier.weight(1f),
      ) { Text("-0.1X") }
      OutlinedButton(
          enabled = isTargetValidOrEmpty(sellTarget),
          onClick = {
            sellTargetX += BigDecimal(0.1)
            sellTarget = (tokenWithValue.value.usd * sellTargetX).toString()
          },
          modifier = Modifier.weight(1f),
      ) { Text("0.1X") }
      OutlinedButton(
          enabled = isTargetValidOrEmpty(sellTarget),
          onClick = {
            sellTargetX += BigDecimal.ONE
            sellTarget = (tokenWithValue.value.usd * sellTargetX).toString()
          },
          modifier = Modifier.weight(1f),
      ) { Text("1X") }
      Box(
          contentAlignment = Alignment.Center,
          modifier = Modifier.padding(horizontal = 3.dp).fillMaxHeight(),
      ) {
        Text(
            text = "${String.format("%.02f", sellTargetX)}X",
            style = Typography.subtitle1.copy(fontWeight = FontWeight.Bold),
        )
      }
    }

    // TODO: show warning if sell target is below current price

    OutlinedButton(
        onClick = {},
        modifier = Modifier.fillMaxWidth(),
    ) { Text(text = "Add alert") }
  }
}

@Composable
@OptIn(ExperimentalCoroutinesApi::class, ExperimentalMaterialApi::class, FlowPreview::class)
private fun AddTokenBottomSheetContent(
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
private fun DeleteTokenDialog(
    token: Token,
    dismiss: () -> Unit,
    viewModel: MainViewModel = hiltViewModel(),
) {
  AlertDialog(
      onDismissRequest = dismiss,
      title = { Text(text = "Delete token", fontWeight = FontWeight.Bold) },
      text = {
        Text(text = "Do you really want to delete ${token.name} with all associated alerts?")
      },
      buttons = {
        val context = LocalContext.current
        Row(
            modifier = Modifier.padding(all = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
          OutlinedButton(
              modifier = Modifier.weight(1f),
              onClick = {
                viewModel.deleteToken(token.address)
                dismiss()
                Toast.makeText(context, "${token.name} was deleted.", Toast.LENGTH_SHORT).show()
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
private fun AlertsList(viewModel: MainViewModel = hiltViewModel()) {
  val alerts = viewModel.alertsFlow.collectAsState(initial = emptyList())
  if (alerts.value.isEmpty()) {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
      Text(text = "No saved alerts.", textAlign = TextAlign.Center)
    }
  } else {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
      items(alerts.value) { ListItem { Text(text = it.alert.address) } }
    }
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
  tokenBeingDeleted?.let { DeleteTokenDialog(token = it, dismiss = { tokenBeingDeleted = null }) }

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
        Row {
          Text(
              text = tokenWithValue.value.usd.toPlainString(),
              style = Typography.subtitle2,
              maxLines = 1,
              modifier = Modifier.weight(1f),
          )
          Text(
              text = "$",
              style = Typography.subtitle2,
              maxLines = 1,
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
