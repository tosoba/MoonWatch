package com.moonwatch.core.model

import java.util.*

interface ITokenAlert {
  val address: String
  val active: Boolean
  val createdAt: Date
  val lastFiredAt: Date?
  val sellPriceTargetUsd: Double?
  val buyPriceTargetUsd: Double?
}
