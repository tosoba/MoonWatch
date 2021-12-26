package com.moonwatch.db

import androidx.room.TypeConverter
import com.moonwatch.core.android.ext.millisToLocalDateTime
import com.moonwatch.core.model.Chain
import java.math.BigDecimal
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneId

object Converters {
  @TypeConverter fun toChain(value: String?): Chain? = value?.let { enumValueOf<Chain>(it) }
  @TypeConverter fun fromChain(value: Chain?): String? = value?.name

  @TypeConverter fun toLocalDateTime(value: Long?): LocalDateTime? = value?.millisToLocalDateTime
  @TypeConverter
  fun fromLocalDateTime(value: LocalDateTime?): Long? =
      value?.atZone(ZoneId.systemDefault())?.toInstant()?.toEpochMilli()

  @TypeConverter fun fromBigDecimal(value: BigDecimal?): String? = value?.toPlainString()
  @TypeConverter
  fun toBigDecimal(value: String?): BigDecimal? =
      if (value.isNullOrBlank()) null else value.toBigDecimalOrNull()
}
