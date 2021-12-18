package com.moonwatch.ui.bottom.sheet

import androidx.compose.runtime.Composable
import com.moonwatch.model.TokenWithValue

@Composable
fun TokenValueBottomSheetColumnContent(tokenWithValue: TokenWithValue) {
  ViewTokenBottomSheetTextField(
      value = tokenWithValue.token.address,
      label = "Address",
      toastText = "Copied token address",
  )
  ViewTokenBottomSheetTextField(
      value = tokenWithValue.token.name,
      label = "Name",
      toastText = "Copied token name",
  )
  ViewTokenBottomSheetTextField(
      value = tokenWithValue.value.usd.toString(),
      label = "Value in USD",
      toastText = "Copied token value",
  )
}
