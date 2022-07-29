package com.moonwatch.ui

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.moonwatch.MainViewModel
import com.moonwatch.R
import com.moonwatch.model.Token
import com.moonwatch.ui.bottom.sheet.*
import com.moonwatch.ui.dialog.DeleteItemDialog
import com.moonwatch.ui.list.TokenAlertsList
import com.moonwatch.ui.list.TokensWithValueList
import com.moonwatch.ui.theme.Typography
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
@OptIn(ExperimentalMaterialApi::class, ExperimentalPagerApi::class)
fun MainScaffold(viewModel: MainViewModel = hiltViewModel()) {
  val scope = rememberCoroutineScope()
  val pageState = rememberPagerState()
  val scaffoldState = rememberScaffoldState()
  val bottomNavigationItems = MainBottomNavigationItem.values()
  val modalBottomSheetState =
      rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)
  var bottomSheetDialogMode by rememberSaveable { mutableStateOf(BottomSheetMode.ADD_TOKEN) }

  val appBarHeightDp = 56.dp
  val appBarHeightPx = with(LocalDensity.current) { appBarHeightDp.roundToPx().toFloat() }
  var bottomAppBarOffsetHeightPx by rememberSaveable { mutableStateOf(0f) }

  val nestedScrollConnection = remember {
    object : NestedScrollConnection {
      override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
        val delta = available.y
        val newOffset = bottomAppBarOffsetHeightPx - delta
        bottomAppBarOffsetHeightPx = newOffset.coerceIn(0f, appBarHeightPx)
        return Offset.Zero
      }
    }
  }

  LaunchedEffect(Unit) {
    viewModel.showAlertBottomSheet
        .onEach {
          pageState.animateScrollToPage(
              MainBottomNavigationItem.values().indexOf(MainBottomNavigationItem.ALERTS))
          bottomSheetDialogMode = BottomSheetMode.EDIT_ALERT
          scope.launch { modalBottomSheetState.show() }
        }
        .launchIn(scope)
  }

  BackPressedHandler(enabled = modalBottomSheetState.isVisible) {
    scope.launch { modalBottomSheetState.hide() }
  }

  var tokenBeingDeleted by rememberSaveable { mutableStateOf<Token?>(null) }
  tokenBeingDeleted?.let { token ->
    DeleteItemDialog(
        itemName = "${token.name} with all associated alerts",
        dismiss = { tokenBeingDeleted = null },
        delete = {
          scope.launch { modalBottomSheetState.hide() }
          viewModel.deleteToken(token.address)
        },
    )
  }

  ModalBottomSheetLayout(
      sheetContent = {
        when (bottomSheetDialogMode) {
          BottomSheetMode.ADD_TOKEN -> {
            SaveTokenBottomSheetContent(
                modalBottomSheetState,
                onAddAlertClick = {
                  viewModel.tokenWithValueBeingViewed = it
                  bottomSheetDialogMode = BottomSheetMode.ADD_ALERT
                },
            )
          }
          BottomSheetMode.VIEW_TOKEN -> {
            ViewTokenBottomSheetContent(
                onAddAlertClick = { bottomSheetDialogMode = BottomSheetMode.ADD_ALERT },
                onDeleteTokenClick = { (token) -> tokenBeingDeleted = token },
            )
          }
          BottomSheetMode.ADD_ALERT -> {
            AddEditAlertBottomSheetContent(
                modalBottomSheetState = modalBottomSheetState,
                alertBottomSheetMode = AlertBottomSheetMode.ADD,
            )
          }
          BottomSheetMode.EDIT_ALERT -> {
            AddEditAlertBottomSheetContent(
                modalBottomSheetState = modalBottomSheetState,
                alertBottomSheetMode = AlertBottomSheetMode.EDIT,
            )
          }
        }
      },
      sheetState = modalBottomSheetState,
  ) {
    Scaffold(
        modifier = Modifier.nestedScroll(nestedScrollConnection),
        scaffoldState = scaffoldState,
        topBar = {
          val useAlarms = viewModel.useAlarmsFlow().collectAsState(initial = false)
          val context = LocalContext.current
          TopAppBar(
              title = {
                Text(
                    text = stringResource(R.string.app_name),
                    style = Typography.h6.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(horizontal = 5.dp),
                )
              },
              actions = {
                IconButton(
                    onClick = {
                      scope.launch {
                        val toggled = viewModel.toggleUseAlarms()
                        Toast.makeText(
                                context,
                                context.getString(
                                    if (toggled) R.string.loud_alarms_on
                                    else R.string.loud_alarms_off,
                                ),
                                Toast.LENGTH_SHORT,
                            )
                            .show()
                      }
                    },
                    modifier = Modifier.wrapContentSize(),
                ) {
                  Icon(
                      painterResource(
                          if (useAlarms.value) R.drawable.ic_baseline_alarm_on_24
                          else R.drawable.ic_baseline_alarm_off_24,
                      ),
                      "",
                  )
                }
              },
          )
        },
        bottomBar = {
          BottomAppBar(
              modifier =
                  Modifier.height(appBarHeightDp).offset {
                    IntOffset(x = 0, y = bottomAppBarOffsetHeightPx.roundToInt())
                  },
          ) {
            bottomNavigationItems.forEachIndexed { index, item ->
              BottomNavigationItem(
                  icon = {
                    Icon(
                        painter = painterResource(id = item.drawableResource),
                        contentDescription = item.title,
                    )
                  },
                  selected = index == pageState.currentPage,
                  onClick = {
                    bottomAppBarOffsetHeightPx = 0f
                    scope.launch { pageState.animateScrollToPage(index) }
                  },
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
                bottomSheetDialogMode = BottomSheetMode.ADD_TOKEN
                scope.launch { modalBottomSheetState.show() }
              },
              modifier =
                  Modifier.offset { IntOffset(x = 0, y = bottomAppBarOffsetHeightPx.roundToInt()) },
          ) {
            Icon(Icons.Filled.Add, "")
          }
        },
    ) {
      HorizontalPager(
          state = pageState,
          count = bottomNavigationItems.size,
          modifier = Modifier.fillMaxSize(),
      ) { page ->
        when (bottomNavigationItems[page]) {
          MainBottomNavigationItem.TOKENS -> {
            val context = LocalContext.current
            TokensWithValueList(
                onItemClick = {
                  bottomSheetDialogMode = BottomSheetMode.VIEW_TOKEN
                  viewModel.tokenWithValueBeingViewed = it
                  scope.launch { modalBottomSheetState.show() }
                },
                onTrailingClick = {
                  context.startActivity(
                      Intent(Intent.ACTION_VIEW).apply {
                        data = Uri.parse("https://poocoin.app/tokens/${it.token.address}")
                      },
                  )
                },
            )
          }
          MainBottomNavigationItem.ALERTS ->
              TokenAlertsList(
                  onItemClick = {
                    bottomSheetDialogMode = BottomSheetMode.EDIT_ALERT
                    viewModel.tokenAlertWithValuesBeingViewed = it
                    scope.launch { modalBottomSheetState.show() }
                  },
              )
        }
      }
    }
  }
}
