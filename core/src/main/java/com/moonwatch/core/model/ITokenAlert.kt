package com.moonwatch.core.model

import java.math.BigDecimal
import org.threeten.bp.LocalDateTime

interface ITokenAlert {
  val id: Long
  val address: String
  val active: Boolean
  val createdAt: LocalDateTime
  val lastFiredAt: LocalDateTime?
  val sellPriceTargetUsd: BigDecimal?
  val buyPriceTargetUsd: BigDecimal?
}
