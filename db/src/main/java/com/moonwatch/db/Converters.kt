package com.moonwatch.db

import androidx.room.TypeConverter
import com.moonwatch.core.model.Chain
import java.util.*

object Converters {
  @TypeConverter fun toChain(value: String) = enumValueOf<Chain>(value)
  @TypeConverter fun fromChain(value: Chain) = value.name

  @TypeConverter fun toDate(value: Long): Date = Date(value)
  @TypeConverter fun fromDate(value: Date): Long = value.time
}
