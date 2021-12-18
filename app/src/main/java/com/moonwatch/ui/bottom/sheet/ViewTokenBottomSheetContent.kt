package com.moonwatch.ui.bottom.sheet

import androidx.compose.foundation.layout.*
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.moonwatch.MainViewModel
import com.moonwatch.model.TokenWithValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@Composable
@OptIn(ExperimentalCoroutinesApi::class, ExperimentalMaterialApi::class, FlowPreview::class)
fun ViewTokenBottomSheetContent(
    onAddAlertClick: (TokenWithValue) -> Unit,
    viewModel: MainViewModel = hiltViewModel()
) {
  val tokenWithValue: TokenWithValue =
      viewModel.tokenWithValueBeingViewed.value ?: throw IllegalArgumentException()
  Column(modifier = Modifier.padding(horizontal = 10.dp)) {
    Box(modifier = Modifier.height(15.dp))
    TokenValueBottomSheetColumnContent(tokenWithValue)
    OutlinedButton(
        onClick = { onAddAlertClick(tokenWithValue) },
        modifier = Modifier.fillMaxWidth(),
    ) { Text(text = "Create an alert") }
    Box(modifier = Modifier.height(15.dp))
  }
}
