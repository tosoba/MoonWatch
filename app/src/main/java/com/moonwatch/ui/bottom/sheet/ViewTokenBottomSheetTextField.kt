package com.moonwatch.ui.bottom.sheet

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.moonwatch.ui.CopyIconButton

@Composable
fun ViewTokenBottomSheetTextField(value: String, label: String, toastText: String) {
  OutlinedTextField(
      value = value,
      onValueChange = {},
      readOnly = true,
      label = { Text(label) },
      singleLine = true,
      trailingIcon = { CopyIconButton(value, toastText) },
      modifier = Modifier.fillMaxWidth(),
  )
}
