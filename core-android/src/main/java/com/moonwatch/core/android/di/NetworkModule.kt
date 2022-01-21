package com.moonwatch.core.android.di

import com.moonwatch.core.android.BuildConfig
import com.moonwatch.core.di.DefaultHttpClient
import com.moonwatch.core.di.HttpClientWithExtendedTimeouts
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.TimeUnit
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
  @DefaultHttpClient
  fun defaultHttpClient(loggingInterceptor: HttpLoggingInterceptor): OkHttpClient =
      OkHttpClient.Builder()
          .run { if (BuildConfig.DEBUG) addInterceptor(loggingInterceptor) else this }
          .build()

  @Provides
  @Singleton
  @HttpClientWithExtendedTimeouts
  fun httpClientWithExtendedTimeouts(loggingInterceptor: HttpLoggingInterceptor): OkHttpClient =
      OkHttpClient.Builder()
          .connectTimeout(20, TimeUnit.SECONDS)
          .readTimeout(20, TimeUnit.SECONDS)
          .writeTimeout(20, TimeUnit.SECONDS)
          .run { if (BuildConfig.DEBUG) addInterceptor(loggingInterceptor) else this }
          .build()
}
