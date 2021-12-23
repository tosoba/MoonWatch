package com.moonwatch.model

import android.os.Parcelable
import com.moonwatch.core.model.ITokenAlert
import java.math.BigDecimal
import java.util.*
import kotlinx.parcelize.Parcelize
import org.threeten.bp.LocalDateTime

@Parcelize
data class TokenAlert(
    override val id: Long,
    override val address: String,
    override val active: Boolean,
    override val createdAt: LocalDateTime,
    override val lastFiredAt: LocalDateTime?,
    override val sellPriceTargetUsd: BigDecimal?,
    override val buyPriceTargetUsd: BigDecimal?,
) : ITokenAlert, Parcelable {
  constructor(
      other: ITokenAlert
  ) : this(
      id = other.id,
      address = other.address,
      active = other.active,
      createdAt = other.createdAt,
      lastFiredAt = other.lastFiredAt,
      sellPriceTargetUsd = other.sellPriceTargetUsd,
      buyPriceTargetUsd = other.buyPriceTargetUsd,
  )
}
