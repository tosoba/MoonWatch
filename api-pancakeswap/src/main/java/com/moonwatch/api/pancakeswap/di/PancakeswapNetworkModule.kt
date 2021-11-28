package com.moonwatch.api.pancakeswap.di

import com.moonwatch.api.pancakeswap.PancakeswapEndpoints
import com.squareup.moshi.Moshi
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
  fun moshiConverterFactory(): MoshiConverterFactory =
      MoshiConverterFactory.create(Moshi.Builder().build())

  @Provides
  @Singleton
  fun pancakeswapEndpoints(
      converterFactory: MoshiConverterFactory,
      httpClient: OkHttpClient,
  ): PancakeswapEndpoints =
      Retrofit.Builder()
          .baseUrl(PancakeswapEndpoints.BASE_URL)
          .addConverterFactory(converterFactory)
          .client(httpClient)
          .build()
          .create(PancakeswapEndpoints::class.java)
}
