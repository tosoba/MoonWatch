package com.moonwatch.ui

import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalLifecycleOwner

@Composable
fun BackPressedHandler(enabled: Boolean = true, onBackPressed: () -> Unit) {
  val currentOnBackPressed by rememberUpdatedState(onBackPressed)
  val onBackPressedCallback = remember {
    object : OnBackPressedCallback(enabled) {
      override fun handleOnBackPressed() {
        currentOnBackPressed()
      }
    }
  }
  SideEffect { onBackPressedCallback.isEnabled = enabled }
  val backDispatcher =
      checkNotNull(LocalOnBackPressedDispatcherOwner.current) {
            "No OnBackPressedDispatcherOwner was provided via LocalOnBackPressedDispatcherOwner"
          }
          .onBackPressedDispatcher
  val lifecycleOwner = LocalLifecycleOwner.current
  DisposableEffect(lifecycleOwner, backDispatcher) {
    backDispatcher.addCallback(lifecycleOwner, onBackPressedCallback)
    onDispose(onBackPressedCallback::remove)
  }
}
