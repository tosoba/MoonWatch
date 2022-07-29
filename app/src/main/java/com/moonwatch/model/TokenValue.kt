package com.moonwatch.model

import android.os.Parcelable
import com.moonwatch.core.model.ITokenValue
import kotlinx.parcelize.Parcelize
import org.threeten.bp.LocalDateTime
import java.math.BigDecimal

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
