package com.moonwatch

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.activity.compose.setContent
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.annotation.ExperimentalCoilApi
import coil.compose.ImagePainter
import coil.compose.rememberImagePainter
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.moonwatch.core.android.model.*
import com.moonwatch.core.model.IToken
import com.moonwatch.core.model.ITokenWithValue
import com.moonwatch.ui.theme.MoonWatchTheme
import com.moonwatch.ui.theme.Purple700
import com.moonwatch.ui.theme.Typography
import dagger.hilt.android.AndroidEntryPoint
import java.io.IOException
import java.util.*
import kotlinx.coroutines.*
import retrofit2.HttpException

@AndroidEntryPoint
@ExperimentalCoilApi
@ExperimentalCoroutinesApi
@ExperimentalMaterialApi
@ExperimentalPagerApi
@FlowPreview
class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      MoonWatchTheme { Surface(color = MaterialTheme.colors.background) { MainScaffold() } }
    }
  }
}

@Composable
@ExperimentalCoilApi
@ExperimentalCoroutinesApi
@ExperimentalMaterialApi
@ExperimentalPagerApi
@FlowPreview
private fun MainScaffold() {
  val scope = rememberCoroutineScope()
  val pageState = rememberPagerState()
  val scaffoldState = rememberScaffoldState()
  val modalBottomSheetState =
      rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)
  val items = MainBottomNavigationItem.values()
  BackHandler(enabled = modalBottomSheetState.isVisible) {
    scope.launch { modalBottomSheetState.hide() }
  }
  ModalBottomSheetLayout(
      sheetContent = {
        when (pageState.currentPage) {
          MainBottomNavigationItem.TOKENS.ordinal -> {
            AddTokenBottomSheetContent(modalBottomSheetState)
          }
          MainBottomNavigationItem.ALERTS.ordinal -> {
            // TODO: show add alert dialog with either a saved token or an address field
            Box(Modifier.size(20.dp))
          }
          else -> throw IllegalArgumentException()
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
          FloatingActionButton(onClick = { scope.launch { modalBottomSheetState.show() } }) {
            Icon(Icons.Filled.Add, "")
          }
        },
    ) {
      HorizontalPager(state = pageState, count = items.size) { page ->
        when (items[page]) {
          MainBottomNavigationItem.TOKENS -> TokensWithValueList()
          MainBottomNavigationItem.ALERTS -> AlertsList()
        }
      }
    }
  }
}

@Composable
@ExperimentalCoroutinesApi
@ExperimentalMaterialApi
@FlowPreview
private fun AddTokenBottomSheetContent(
    modalBottomSheetState: ModalBottomSheetState,
    viewModel: MainViewModel = hiltViewModel()
) {
  val scope = rememberCoroutineScope()
  Column(modifier = Modifier.padding(15.dp)) {
    Text(
        text = "Add a new token",
        style =
            Typography.h6.copy(
                color = MaterialTheme.colors.primary,
                fontWeight = FontWeight.Bold,
            ),
        modifier = Modifier.padding(horizontal = 5.dp),
    )
    val tokenAddress = viewModel.tokenAddress.collectAsState(initial = "")
    OutlinedTextField(
        value = tokenAddress.value,
        onValueChange = viewModel::setTokenAddress,
        label = { Text("Address") },
        singleLine = true,
        isError = viewModel.tokenWithValue.value is Failed,
        modifier = Modifier.fillMaxWidth(),
    )
    when (val tokenWithValue = viewModel.tokenWithValue.value) {
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
        OutlinedButton(
            onClick = {
              scope.launch {
                viewModel.saveCurrentToken()
                modalBottomSheetState.hide()
              }
            },
            modifier = Modifier.fillMaxWidth(),
        ) { Text(text = "Save") }
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
@ExperimentalCoroutinesApi
@FlowPreview
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
@ExperimentalCoroutinesApi
@ExperimentalMaterialApi
@ExperimentalPagerApi
@FlowPreview
private fun AlertsList(viewModel: MainViewModel = hiltViewModel()) {
  val alerts = viewModel.getAlertsFlow().collectAsState(initial = emptyList())
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
@ExperimentalCoilApi
@ExperimentalCoroutinesApi
@ExperimentalMaterialApi
@ExperimentalPagerApi
@FlowPreview
private fun TokensWithValueList(viewModel: MainViewModel = hiltViewModel()) {
  val tokens = viewModel.getTokensFlow().collectAsState(initial = emptyList())
  if (tokens.value.isEmpty()) {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
      Text(text = "No saved tokens.", textAlign = TextAlign.Center)
    }
  } else {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
      items(tokens.value) { TokenWithValueListItem(it) }
    }
  }
}

@Composable
@ExperimentalCoilApi
@ExperimentalMaterialApi
private fun TokenWithValueListItem(tokenWithValue: ITokenWithValue) {
  ListItem(
      icon = { TokenIcon(tokenWithValue.token) },
      secondaryText = { Text(text = "${tokenWithValue.value.usd}$", style = Typography.subtitle2) },
  ) {
    Text(
        text = tokenWithValue.token.name,
        style = Typography.subtitle1.copy(fontWeight = FontWeight.Bold),
    )
  }
}

@Composable
@ExperimentalCoilApi
private fun TokenIcon(token: IToken) {
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
