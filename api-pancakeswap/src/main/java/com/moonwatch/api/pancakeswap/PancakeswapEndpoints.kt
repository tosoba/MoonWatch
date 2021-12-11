package com.moonwatch.api.pancakeswap

import com.moonwatch.api.pancakeswap.model.PancakeswapTokenResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface PancakeswapEndpoints {
  @GET("tokens/{address}")
  suspend fun getToken(@Path("address") address: String): PancakeswapTokenResponse

  companion object {
    internal const val BASE_URL = "https://api.pancakeswap.info/api/v2/"
  }
}
