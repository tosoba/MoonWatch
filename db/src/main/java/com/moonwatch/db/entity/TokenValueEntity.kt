package com.moonwatch.db.entity

import androidx.room.*
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
    val address: String,
    val usd: Double,
    val bnb: Double? = null,
    val eth: Double? = null,
    @ColumnInfo(name = "updated_at") val updatedAt: Date
) {
  @PrimaryKey(autoGenerate = true) var id: Long = 0
}
