package com.moonwatch.ui.bottom.sheet

import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.moonwatch.ui.theme.Typography

@Composable
fun BottomSheetContentTitleText(text: String) {
  Text(
      text = text,
      style =
          Typography.h6.copy(
              color = MaterialTheme.colors.primary,
              fontWeight = FontWeight.Bold,
          ),
      modifier = Modifier.padding(horizontal = 5.dp),
  )
}
