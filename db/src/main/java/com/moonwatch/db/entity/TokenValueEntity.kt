package com.moonwatch.db.entity

import androidx.room.*
import com.moonwatch.core.model.ITokenValue
import org.threeten.bp.LocalDateTime
import java.math.BigDecimal

@Entity(
    tableName = "token_value",
    foreignKeys =
        [
            ForeignKey(
                entity = TokenEntity::class,
                parentColumns = ["address"],
                childColumns = ["address"],
                onDelete = ForeignKey.CASCADE,
            ),
        ],
    indices =
        [
            Index(value = ["address"]),
            Index(value = ["updated_at"]),
        ],
)
data class TokenValueEntity(
    override val address: String,
    override val usd: BigDecimal,
    val bnb: BigDecimal? = null,
    val eth: BigDecimal? = null,
    @ColumnInfo(name = "updated_at") override val updatedAt: LocalDateTime
) : ITokenValue {
  @PrimaryKey(autoGenerate = true) override var id: Long = 0

  constructor(
      other: ITokenValue
  ) : this(address = other.address, usd = other.usd, updatedAt = other.updatedAt)
}
