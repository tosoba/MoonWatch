package com.moonwatch.model

import android.os.Parcelable
import com.moonwatch.core.model.ITokenValue
import java.util.*
import kotlinx.parcelize.Parcelize

@Parcelize
data class TokenValue(
    override val address: String,
    override val usd: Double,
    override val updatedAt: Date
) : ITokenValue, Parcelable {
  constructor(
      other: ITokenValue
  ) : this(address = other.address, usd = other.usd, updatedAt = other.updatedAt)
}
