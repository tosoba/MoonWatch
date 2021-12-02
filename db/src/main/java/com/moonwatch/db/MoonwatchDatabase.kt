package com.moonwatch.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.moonwatch.db.dao.AlertDao
import com.moonwatch.db.dao.TokenDao
import com.moonwatch.db.entity.TokenAlertEntity
import com.moonwatch.db.entity.TokenEntity
import com.moonwatch.db.entity.TokenValueEntity

@Database(
    version = 1,
    exportSchema = false,
    entities = [TokenEntity::class, TokenValueEntity::class, TokenAlertEntity::class],
)
@TypeConverters(value = [Converters::class])
abstract class MoonwatchDatabase : RoomDatabase() {
  abstract fun tokenDao(): TokenDao
  abstract fun alertDao(): AlertDao
}
