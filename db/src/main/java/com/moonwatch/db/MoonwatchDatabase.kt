package com.moonwatch.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.moonwatch.db.dao.TokenDao
import com.moonwatch.db.entity.TokenEntity
import com.moonwatch.db.entity.TokenValueEntity

@Database(
    version = 1,
    exportSchema = false,
    entities = [TokenEntity::class, TokenValueEntity::class],
)
@TypeConverters(value = [Converters::class])
abstract class MoonwatchDatabase : RoomDatabase() {
  abstract fun tokenDao(): TokenDao
}
