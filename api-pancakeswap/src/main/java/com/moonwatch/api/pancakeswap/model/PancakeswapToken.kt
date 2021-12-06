import com.moonwatch.core.model.Chain
import com.moonwatch.core.model.IToken
import com.squareup.moshi.Json

data class PancakeswapToken(
    @field:Json(name = "name") override val name: String,
    @field:Json(name = "symbol") override val symbol: String,
    @field:Json(name = "price") val priceInUsd: String,
    @field:Json(name = "price_BNB") val priceInBnb: String
) : IToken {
  override val address: String
    get() = symbol
  override val chain: Chain
    get() = Chain.BSC
}
