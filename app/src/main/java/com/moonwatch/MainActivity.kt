package com.moonwatch

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import coil.annotation.ExperimentalCoilApi
import com.google.accompanist.pager.ExperimentalPagerApi
import com.moonwatch.repo.notification.AlertNotificationManager
import com.moonwatch.ui.MainScaffold
import com.moonwatch.ui.theme.MoonWatchTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@AndroidEntryPoint
@OptIn(
    ExperimentalCoilApi::class,
    ExperimentalCoroutinesApi::class,
    ExperimentalMaterialApi::class,
    ExperimentalPagerApi::class,
    FlowPreview::class,
)
class MainActivity : ComponentActivity() {
  private val viewModel: MainViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      MoonWatchTheme { Surface(color = MaterialTheme.colors.background) { MainScaffold() } }
    }
  }

  override fun onNewIntent(intent: Intent?) {
    val clickedFiredAlertId =
        intent?.getLongExtra(AlertNotificationManager.ALERT_ID_EXTRA_KEY, -1L) ?: return
    if (clickedFiredAlertId == -1L) return
    viewModel.setClickedFiredAlertId(id = clickedFiredAlertId)
  }
}
