import com.squareup.moshi.Json

data class PancakeswapTokenResponse(
    @Json(name = "data") val token: PancakeswapToken,
    @Json(name = "updated_at") val updatedAtMillis: Long
)
