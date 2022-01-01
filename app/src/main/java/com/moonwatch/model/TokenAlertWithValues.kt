package com.moonwatch.model

import android.os.Parcelable
import com.moonwatch.core.model.ITokenAlertWithValues
import kotlinx.parcelize.Parcelize

@Parcelize
data class TokenAlertWithValues(
    override val token: Token,
    override val alert: TokenAlert,
    override val creationValue: TokenValue,
    override val currentValue: TokenValue,
) : ITokenAlertWithValues, Parcelable {
  constructor(
      other: ITokenAlertWithValues
  ) : this(
      Token(other.token),
      TokenAlert(other.alert),
      creationValue = TokenValue(other.creationValue),
      currentValue = TokenValue(other.currentValue),
  )
}
