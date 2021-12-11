package com.moonwatch.core.model

import java.math.BigDecimal
import java.util.*

interface ITokenValue {
  val address: String
  val usd: BigDecimal
  val updatedAt: Date
}
