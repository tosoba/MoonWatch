import com.squareup.moshi.Json

data class PancakeswapTokenResponse(
    @Json(name = "updated_at") val updatedAtMillis: Long,
    @Json(name = "data") val token: PancakeswapToken
)
