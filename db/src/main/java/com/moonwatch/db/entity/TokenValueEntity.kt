package com.moonwatch.db.entity

import androidx.room.*
import com.moonwatch.core.model.ITokenValue
import java.util.*

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
    override val usd: Double,
    val bnb: Double? = null,
    val eth: Double? = null,
    @ColumnInfo(name = "updated_at") override val updatedAt: Date
) : ITokenValue {
  @PrimaryKey(autoGenerate = true) var id: Long = 0

  constructor(
      other: ITokenValue
  ) : this(address = other.address, usd = other.usd, updatedAt = other.updatedAt)
}
