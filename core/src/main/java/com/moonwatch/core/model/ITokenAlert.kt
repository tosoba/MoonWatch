package com.moonwatch.core.model

import org.threeten.bp.LocalDateTime
import java.math.BigDecimal

interface ITokenAlert {
  val id: Long
  val address: String
  val active: Boolean
  val createdAt: LocalDateTime
  val lastFiredAt: LocalDateTime?
  val sellPriceTargetUsd: BigDecimal?
  val buyPriceTargetUsd: BigDecimal?
}
