package com.moonwatch.core.model

import java.math.BigDecimal
import org.threeten.bp.LocalDateTime

interface ITokenValue {
  val address: String
  val usd: BigDecimal
  val updatedAt: LocalDateTime
}
