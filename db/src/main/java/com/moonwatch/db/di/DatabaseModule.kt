package com.moonwatch.db.di

import android.content.Context
import com.moonwatch.core.android.ext.buildRoom
import com.moonwatch.db.MoonwatchDatabase
import com.moonwatch.db.dao.AlertDao
import com.moonwatch.db.dao.TokenDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
  @Provides
  @Singleton
  fun overpassDatabase(@ApplicationContext context: Context): MoonwatchDatabase =
      context.buildRoom()

  @Provides fun alertDao(db: MoonwatchDatabase): AlertDao = db.alertDao()
  @Provides fun tokenDao(db: MoonwatchDatabase): TokenDao = db.tokenDao()
}
