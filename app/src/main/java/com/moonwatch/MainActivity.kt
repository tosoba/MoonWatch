package com.moonwatch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.moonwatch.ui.theme.MoonWatchTheme
import java.util.*
import kotlinx.coroutines.launch

@ExperimentalPagerApi
class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      MoonWatchTheme { Surface(color = MaterialTheme.colors.background) { MainScaffold() } }
    }
  }
}

@ExperimentalPagerApi
@Composable
private fun MainScaffold() {
  val scope = rememberCoroutineScope()
  val pageState = rememberPagerState()
  val scaffoldState = rememberScaffoldState()
  val items = MainBottomNavigationItem.values()
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
  ) {
    HorizontalPager(state = pageState, count = items.size) { page ->
      val item = items[page]
      Image(
          painterResource(id = item.drawableResource),
          contentDescription = item.title,
          modifier = Modifier.fillMaxSize(),
          contentScale = ContentScale.Crop,
      )
    }
  }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
  MoonWatchTheme {}
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
