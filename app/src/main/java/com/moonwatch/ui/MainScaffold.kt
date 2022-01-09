package com.moonwatch.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.annotation.ExperimentalCoilApi
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.moonwatch.MainViewModel
import com.moonwatch.model.Token
import com.moonwatch.ui.bottom.sheet.*
import com.moonwatch.ui.dialog.DeleteItemDialog
import com.moonwatch.ui.list.TokenAlertsList
import com.moonwatch.ui.list.TokensWithValueList
import com.moonwatch.ui.theme.Typography
import kotlin.math.roundToInt
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@Composable
@OptIn(
    ExperimentalCoilApi::class,
    ExperimentalCoroutinesApi::class,
    ExperimentalMaterialApi::class,
    ExperimentalPagerApi::class,
    FlowPreview::class,
)
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
    viewModel
        .showAlertBottomSheet
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
          TopAppBar {
            Text(
                text = "MoonWatch",
                style = Typography.h6.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(horizontal = 5.dp),
            )
          }
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
          ) { Icon(Icons.Filled.Add, "") }
        },
    ) {
      HorizontalPager(
          state = pageState,
          count = bottomNavigationItems.size,
          modifier = Modifier.fillMaxSize(),
      ) { page ->
        when (bottomNavigationItems[page]) {
          MainBottomNavigationItem.TOKENS -> {
            TokensWithValueList(
                onItemClick = {
                  bottomSheetDialogMode = BottomSheetMode.VIEW_TOKEN
                  viewModel.tokenWithValueBeingViewed = it
                  scope.launch { modalBottomSheetState.show() }
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
