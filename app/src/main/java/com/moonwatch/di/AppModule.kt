package com.moonwatch.di

import android.content.Context
import android.content.Intent
import com.moonwatch.MainActivity
import com.moonwatch.core.di.MainActivityIntent
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
  @Provides
  @MainActivityIntent
  fun mainActivityIntent(@ApplicationContext context: Context): Intent =
      Intent(context, MainActivity::class.java)
}
