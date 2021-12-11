package com.moonwatch.core.model

import java.math.BigDecimal
import java.util.*

interface ITokenAlert {
  val id: Long
  val address: String
  val active: Boolean
  val createdAt: Date
  val lastFiredAt: Date?
  val sellPriceTargetUsd: BigDecimal?
  val buyPriceTargetUsd: BigDecimal?
}
