package com.moonwatch

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.lifecycle.lifecycleScope
import com.moonwatch.repo.notification.AlertNotificationManager
import com.moonwatch.ui.MainScaffold
import com.moonwatch.ui.theme.MoonWatchTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
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
    lifecycleScope.launchWhenResumed { viewModel.setClickedFiredAlertId(id = clickedFiredAlertId) }
  }
}
