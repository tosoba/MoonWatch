package com.moonwatch.model

import android.os.Parcelable
import com.moonwatch.core.model.ITokenWithValue
import kotlinx.parcelize.Parcelize

@Parcelize
data class TokenWithValue(
    override val token: Token,
    override val value: TokenValue,
) : ITokenWithValue, Parcelable {
  constructor(other: ITokenWithValue) : this(Token(other.token), TokenValue(other.value))
}
