package com.moonwatch.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.annotation.ExperimentalCoilApi
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.moonwatch.MainViewModel
import com.moonwatch.ui.bottom.sheet.AddAlertBottomSheetContent
import com.moonwatch.ui.bottom.sheet.BottomSheetMode
import com.moonwatch.ui.bottom.sheet.SaveTokenBottomSheetContent
import com.moonwatch.ui.bottom.sheet.ViewTokenBottomSheetContent
import com.moonwatch.ui.list.TokenAlertsList
import com.moonwatch.ui.list.TokensWithValueList
import com.moonwatch.ui.theme.Typography
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
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
  val modalBottomSheetState =
      rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)
  val items = MainBottomNavigationItem.values()
  var bottomSheetDialogMode by rememberSaveable { mutableStateOf(BottomSheetMode.ADD_TOKEN) }

  BackPressedHandler(enabled = modalBottomSheetState.isVisible) {
    scope.launch { modalBottomSheetState.hide() }
  }
  ModalBottomSheetLayout(
      sheetContent = {
        when (bottomSheetDialogMode) {
          BottomSheetMode.ADD_TOKEN -> SaveTokenBottomSheetContent(modalBottomSheetState)
          BottomSheetMode.VIEW_TOKEN -> {
            ViewTokenBottomSheetContent(
                onAddAlertClick = { bottomSheetDialogMode = BottomSheetMode.ADD_ALERT },
            )
          }
          BottomSheetMode.ADD_ALERT -> AddAlertBottomSheetContent(modalBottomSheetState)
          BottomSheetMode.EDIT_ALERT -> Box(modifier = Modifier.size(20.dp))
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
                bottomSheetDialogMode = BottomSheetMode.ADD_TOKEN
                scope.launch { modalBottomSheetState.show() }
              },
          ) { Icon(Icons.Filled.Add, "") }
        },
    ) {
      HorizontalPager(
          state = pageState,
          count = items.size,
          modifier = Modifier.fillMaxSize(),
      ) { page ->
        when (items[page]) {
          MainBottomNavigationItem.TOKENS -> {
            TokensWithValueList(
                onItemClick = {
                  bottomSheetDialogMode = BottomSheetMode.VIEW_TOKEN
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
