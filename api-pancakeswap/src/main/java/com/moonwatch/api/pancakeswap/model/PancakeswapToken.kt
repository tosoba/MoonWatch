import com.moonwatch.core.model.Chain
import com.moonwatch.core.model.IToken
import com.squareup.moshi.Json

data class PancakeswapToken(
    @Json(name = "name") override val name: String,
    @Json(name = "symbol") override val symbol: String,
    @Json(name = "price") val priceInUsd: Double,
    @Json(name = "price_BNB") val priceInBnb: Double
) : IToken {
  override val address: String
    get() = symbol
  override val chain: Chain
    get() = Chain.BSC
}
