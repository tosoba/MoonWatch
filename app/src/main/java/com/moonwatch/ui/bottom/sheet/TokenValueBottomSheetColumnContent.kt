package com.moonwatch.ui.bottom.sheet

import androidx.compose.runtime.Composable
import com.moonwatch.model.Token
import com.moonwatch.model.TokenValue

@Composable
fun TokenValueBottomSheetColumnContent(token: Token, value: TokenValue) {
  ViewTokenBottomSheetTextField(
      value = token.address,
      label = "Address",
      toastText = "Copied token address",
  )
  ViewTokenBottomSheetTextField(
      value = token.name,
      label = "Name",
      toastText = "Copied token name",
  )
  ViewTokenBottomSheetTextField(
      value = value.usd.toPlainString(),
      label = "Value in USD",
      toastText = "Copied token value",
  )
}
