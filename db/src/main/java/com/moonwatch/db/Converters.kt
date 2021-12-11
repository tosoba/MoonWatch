package com.moonwatch.db

import androidx.room.TypeConverter
import com.moonwatch.core.model.Chain
import java.math.BigDecimal
import java.util.*

object Converters {
  @TypeConverter fun toChain(value: String?): Chain? = value?.let { enumValueOf<Chain>(it) }
  @TypeConverter fun fromChain(value: Chain?): String? = value?.name

  @TypeConverter fun toDate(value: Long?): Date? = value?.let(::Date)
  @TypeConverter fun fromDate(value: Date?): Long? = value?.time

  @TypeConverter fun fromBigDecimal(value: BigDecimal?): String = value?.toPlainString() ?: ""
  @TypeConverter
  fun toBigDecimal(value: String?): BigDecimal =
      if (value.isNullOrBlank()) BigDecimal.valueOf(0.0)
      else value.toBigDecimalOrNull() ?: BigDecimal.valueOf(0.0)
}
