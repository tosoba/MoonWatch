package com.moonwatch.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.moonwatch.ui.theme.Typography
import java.math.BigDecimal

@Composable
fun PriceTargetXText(targetX: BigDecimal) {
  Box(
      contentAlignment = Alignment.Center,
      modifier = Modifier.padding(horizontal = 3.dp).fillMaxHeight(),
  ) {
    Text(
        text = "${String.format("%.02f", targetX)}X",
        style = Typography.subtitle1.copy(fontWeight = FontWeight.Bold),
    )
  }
}
