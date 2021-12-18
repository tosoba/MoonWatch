package com.moonwatch.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.moonwatch.MainViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.launch

@Composable
@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
fun RetryLoadingTokenButton(
    scope: CoroutineScope,
    viewModel: MainViewModel = hiltViewModel(),
) {
  OutlinedButton(
      onClick = { scope.launch { viewModel.retryLoadingToken() } },
      modifier = Modifier.fillMaxWidth(),
  ) { Text(text = "Retry") }
}
