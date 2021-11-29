package com.moonwatch.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
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
            Index(value = ["timestamp"]),
        ],
)
data class TokenValueEntity(
    val address: String,
    val usd: Double,
    val bnb: Double?,
    val eth: Double?,
    val timestamp: Date
) {
  @PrimaryKey(autoGenerate = true) var id: Long = 0
}
