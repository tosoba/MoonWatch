import com.moonwatch.core.model.ITokenValue
import com.moonwatch.core.model.ITokenWithValue
import com.squareup.moshi.Json
import java.util.*

data class PancakeswapTokenResponse(
    @Json(name = "data") override val token: PancakeswapToken,
    @Json(name = "updated_at") val updatedAtMillis: Long
) : ITokenValue, ITokenWithValue {
  override val address: String
    get() = token.symbol
  override val usd: Double
    get() = token.priceInUsd
  override val updatedAt: Date
    get() = Date(updatedAtMillis)
  override val value: ITokenValue
    get() = this
}
