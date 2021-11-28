import com.squareup.moshi.Json

data class PancakeswapToken(
    @Json(name = "name") val name: String,
    @Json(name = "symbol") val symbol: String,
    @Json(name = "price") val priceInUsd: Double,
    @Json(name = "price_BNB") val priceInBnb: Double
)
