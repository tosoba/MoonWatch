package com.moonwatch.core.model

import java.util.*

interface ITokenValue {
  val address: String
  val usd: Double
  val updatedAt: Date
}
