package com.moonwatch.ui.dialog

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.AlertDialog
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun DeleteItemDialog(itemName: String, dismiss: () -> Unit, delete: () -> Unit) {
  AlertDialog(
      onDismissRequest = dismiss,
      title = { Text(text = "Delete item", fontWeight = FontWeight.Bold) },
      text = { Text(text = "Do you really want to delete $itemName?") },
      buttons = {
        val context = LocalContext.current
        Row(
            modifier = Modifier.padding(all = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
          OutlinedButton(
              modifier = Modifier.weight(1f),
              onClick = {
                delete()
                dismiss()
                Toast.makeText(context, "$itemName was deleted.", Toast.LENGTH_SHORT).show()
              },
          ) {
            Text("Confirm")
          }
          Box(Modifier.size(5.dp))
          OutlinedButton(
              modifier = Modifier.weight(1f),
              onClick = dismiss,
          ) {
            Text("Cancel")
          }
        }
      },
  )
}
