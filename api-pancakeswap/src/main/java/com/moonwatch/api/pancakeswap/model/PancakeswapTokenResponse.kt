package com.moonwatch.api.pancakeswap.model

import com.moonwatch.core.model.ITokenValue
import com.moonwatch.core.model.ITokenWithValue
import com.squareup.moshi.Json
import org.threeten.bp.Instant
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneId
import java.math.BigDecimal

data class PancakeswapTokenResponse(
    @field:Json(name = "data") override val token: PancakeswapToken,
    @field:Json(name = "updated_at") val updatedAtMillis: Long
) : ITokenValue, ITokenWithValue {
  var tokenAddress: String? = null
    set(value) {
      field = value
      token.tokenAddress = value
    }
  override val address: String
    get() = requireNotNull(tokenAddress) { "Token address is not set." }

  override val usd: BigDecimal
    get() = token.priceInUsd.toBigDecimal()

  override val updatedAt: LocalDateTime
    get() = Instant.ofEpochMilli(updatedAtMillis).atZone(ZoneId.systemDefault()).toLocalDateTime()

  override val value: ITokenValue
    get() = this
}
