package com.moonwatch.ui.bottom.sheet

import androidx.compose.foundation.layout.*
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.moonwatch.MainViewModel
import com.moonwatch.model.TokenWithValue

@Composable
fun ViewTokenBottomSheetContent(
    onAddAlertClick: (TokenWithValue) -> Unit,
    onDeleteTokenClick: (TokenWithValue) -> Unit,
    viewModel: MainViewModel = hiltViewModel()
) {
  val tokenWithValue = viewModel.tokenWithValueBeingViewed ?: throw IllegalArgumentException()
  Column(modifier = Modifier.padding(horizontal = 10.dp)) {
    Box(modifier = Modifier.height(15.dp))
    TokenValueBottomSheetColumnContent(tokenWithValue.token, tokenWithValue.value)
    Row(horizontalArrangement = Arrangement.SpaceEvenly) {
      OutlinedButton(
          onClick = { onDeleteTokenClick(tokenWithValue) },
          modifier = Modifier.weight(1f),
      ) {
        Text(text = "Delete")
      }
      Box(Modifier.size(5.dp))
      OutlinedButton(
          onClick = { onAddAlertClick(tokenWithValue) },
          modifier = Modifier.weight(1f),
      ) {
        Text(text = "Add an alert")
      }
    }
    Box(modifier = Modifier.height(15.dp))
  }
}
