package com.moonwatch.core.android.di

import com.moonwatch.core.android.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
  @Provides
  @Singleton
  fun httpLoggingInterceptor(): HttpLoggingInterceptor =
      HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }

  @Provides
  @Singleton
  fun httpClient(loggingInterceptor: HttpLoggingInterceptor): OkHttpClient =
      OkHttpClient.Builder()
          .run { if (BuildConfig.DEBUG) addInterceptor(loggingInterceptor) else this }
          .build()
}
