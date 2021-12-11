package com.moonwatch.model

import android.os.Parcelable
import com.moonwatch.core.model.ITokenAlertWithValue
import kotlinx.parcelize.Parcelize

@Parcelize
data class TokenAlertWithValue(
    override val token: Token,
    override val alert: TokenAlert,
    override val value: TokenValue,
) : ITokenAlertWithValue, Parcelable {
  constructor(
      other: ITokenAlertWithValue
  ) : this(Token(other.token), TokenAlert(other.alert), TokenValue(other.value))
}
