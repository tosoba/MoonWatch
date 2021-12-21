package com.moonwatch.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.annotation.ExperimentalCoilApi
import coil.compose.ImagePainter
import coil.compose.rememberImagePainter
import com.moonwatch.model.Token
import com.moonwatch.ui.theme.Purple700
import com.moonwatch.ui.theme.Typography

@Composable
@OptIn(ExperimentalCoilApi::class)
fun TokenIcon(token: Token) {
  // TODO: fix coil
  val painter =
      rememberImagePainter("https://r.poocoin.app/smartchain/assets/${token.address}/logo.png")
  val state = painter.state
  Box(
      contentAlignment = Alignment.Center,
      modifier = Modifier.height(60.dp).wrapContentWidth(),
  ) {
    if (state is ImagePainter.State.Success) {
      Image(painter = painter, contentDescription = token.name, modifier = Modifier.size(40.dp))
    } else {
      Box(
          contentAlignment = Alignment.Center,
          modifier = Modifier.size(40.dp).clip(CircleShape).background(Purple700),
      ) {
        Text(
            text = token.name.substring(0, 1),
            style = Typography.h6.copy(fontWeight = FontWeight.Bold),
            color = Color.White,
        )
      }
    }
  }
}
