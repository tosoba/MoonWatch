package com.moonwatch.api.pancakeswap.di

import com.moonwatch.api.pancakeswap.PancakeswapEndpoints
import com.moonwatch.core.di.DefaultHttpClient
import com.moonwatch.core.di.DefaultPancakeswapEndpoints
import com.moonwatch.core.di.HttpClientWithExtendedTimeouts
import com.moonwatch.core.di.PancakeswapEndpointsWithExtendedTimeouts
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

@Module
@InstallIn(SingletonComponent::class)
object PancakeswapNetworkModule {
  @Provides
  @Singleton
  fun moshiConverterFactory(): MoshiConverterFactory = MoshiConverterFactory.create()

  @Provides
  @Singleton
  @DefaultPancakeswapEndpoints
  fun defaultPancakeswapEndpoints(
      converterFactory: MoshiConverterFactory,
      @DefaultHttpClient httpClient: OkHttpClient,
  ): PancakeswapEndpoints = pancakeswapEndpoints(converterFactory, httpClient)

  @Provides
  @Singleton
  @PancakeswapEndpointsWithExtendedTimeouts
  fun pancakeswapEndpointsWithExtendedTimeouts(
      converterFactory: MoshiConverterFactory,
      @HttpClientWithExtendedTimeouts httpClient: OkHttpClient,
  ): PancakeswapEndpoints = pancakeswapEndpoints(converterFactory, httpClient)

  private fun pancakeswapEndpoints(
      converterFactory: MoshiConverterFactory,
      httpClient: OkHttpClient
  ) =
      Retrofit.Builder()
          .baseUrl(PancakeswapEndpoints.BASE_URL)
          .addConverterFactory(converterFactory)
          .client(httpClient)
          .build()
          .create(PancakeswapEndpoints::class.java)
}
