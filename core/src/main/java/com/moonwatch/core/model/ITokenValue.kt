package com.moonwatch.core.model

import org.threeten.bp.LocalDateTime
import java.math.BigDecimal

interface ITokenValue {
  val id: Long
  val address: String
  val usd: BigDecimal
  val updatedAt: LocalDateTime
}
