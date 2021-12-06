import com.moonwatch.core.model.ITokenValue
import com.moonwatch.core.model.ITokenWithValue
import com.squareup.moshi.Json
import java.util.*

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

  override val usd: Double
    get() = token.priceInUsd.toDouble()

  override val updatedAt: Date
    get() = Date(updatedAtMillis)

  override val value: ITokenValue
    get() = this
}
