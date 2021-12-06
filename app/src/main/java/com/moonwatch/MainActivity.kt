package com.moonwatch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.moonwatch.core.android.model.*
import com.moonwatch.core.model.ITokenAlertWithValue
import com.moonwatch.core.model.ITokenWithValue
import com.moonwatch.core.usecase.GetAlertsFlow
import com.moonwatch.core.usecase.GetTokensFlow
import com.moonwatch.ui.theme.MoonWatchTheme
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import javax.inject.Inject
import kotlinx.coroutines.launch

@ExperimentalMaterialApi
@ExperimentalPagerApi
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
  @Inject internal lateinit var getAlertsFlow: GetAlertsFlow
  @Inject internal lateinit var getTokensFlow: GetTokensFlow

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      MoonWatchTheme {
        Surface(color = MaterialTheme.colors.background) {
          val alerts = getAlertsFlow().collectAsState(initial = emptyList())
          val tokens = getTokensFlow().collectAsState(initial = emptyList())
          MainScaffold(alerts.value, tokens.value)
        }
      }
    }
  }
}

@ExperimentalMaterialApi
@ExperimentalPagerApi
@Composable
private fun MainScaffold(
    alerts: List<ITokenAlertWithValue>,
    tokens: List<ITokenWithValue>,
    viewModel: MainViewModel = hiltViewModel()
) {
  val scope = rememberCoroutineScope()
  val pageState = rememberPagerState()
  val scaffoldState = rememberScaffoldState()
  val modalBottomSheetState =
      rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)
  val items = MainBottomNavigationItem.values()
  ModalBottomSheetLayout(
      sheetContent = {
        Column(modifier = Modifier.padding(10.dp)) {
          Text(text = "Add a new token")
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
              OutlinedButton(
                  onClick = { scope.launch { viewModel.retryLoadingToken() } },
                  modifier = Modifier.fillMaxWidth(),
              ) { Text(text = "Retry") }
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
                  onClick = { scope.launch { viewModel.saveCurrentToken() } },
                  modifier = Modifier.fillMaxWidth(),
              ) { Text(text = "Save") }
            }
            is LoadingInProgress -> CircularProgressIndicator()
            else -> return@Column
          }
        }
      },
      sheetState = modalBottomSheetState,
  ) {
    Scaffold(
        scaffoldState = scaffoldState,
        bottomBar = {
          BottomAppBar(
              backgroundColor = MaterialTheme.colors.primary,
              content = {
                items.forEachIndexed { index, item ->
                  BottomNavigationItem(
                      icon = {
                        Icon(
                            painterResource(id = item.drawableResource),
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
              },
          )
        },
        floatingActionButton = {
          FloatingActionButton(onClick = { scope.launch { modalBottomSheetState.show() } }) {
            Icon(Icons.Filled.Add, "")
          }
        },
    ) {
      HorizontalPager(state = pageState, count = items.size) { page ->
        when (items[page]) {
          MainBottomNavigationItem.TOKENS -> TokensList(tokens = tokens)
          MainBottomNavigationItem.ALERTS -> AlertsList(alerts = alerts)
        }
      }
    }
  }
}

@ExperimentalMaterialApi
@Composable
private fun AlertsList(alerts: List<ITokenAlertWithValue>) {
  if (alerts.isEmpty()) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
      Text(text = "No saved alerts.", textAlign = TextAlign.Center)
    }
  } else {
    LazyColumn { items(alerts) { ListItem { it.alert.address } } }
  }
}

@ExperimentalMaterialApi
@Composable
private fun TokensList(tokens: List<ITokenWithValue>) {
  if (tokens.isEmpty()) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
      Text(text = "No saved tokens.", textAlign = TextAlign.Center)
    }
  } else {
    LazyColumn { items(tokens) { ListItem { it.token.address } } }
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

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
  MoonWatchTheme {}
}
