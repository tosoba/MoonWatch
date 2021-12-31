package com.moonwatch.model

import android.os.Parcelable
import com.moonwatch.core.model.ITokenValue
import java.math.BigDecimal
import kotlinx.parcelize.Parcelize
import org.threeten.bp.LocalDateTime

@Parcelize
data class TokenValue(
    override val id: Long,
    override val address: String,
    override val usd: BigDecimal,
    override val updatedAt: LocalDateTime,
) : ITokenValue, Parcelable {
  constructor(
      other: ITokenValue
  ) : this(id = other.id, address = other.address, usd = other.usd, updatedAt = other.updatedAt)
}
