package com.moonwatch.ui.dialog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.AlertDialog
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun PriceTargetValidationMessagesDialog(messages: List<String>, dismiss: () -> Unit) {
  if (messages.isEmpty()) throw IllegalArgumentException()
  val scrollState = rememberScrollState()
  AlertDialog(
      onDismissRequest = dismiss,
      title = { Text(text = "Price target validation failed.", fontWeight = FontWeight.Bold) },
      text = {
        Column(modifier = Modifier.verticalScroll(scrollState)) {
          for (message in messages) Text(text = message)
        }
      },
      buttons = {
        Row(modifier = Modifier.padding(all = 8.dp)) {
          OutlinedButton(modifier = Modifier.fillMaxWidth(), onClick = dismiss) { Text("OK") }
        }
      },
  )
}
