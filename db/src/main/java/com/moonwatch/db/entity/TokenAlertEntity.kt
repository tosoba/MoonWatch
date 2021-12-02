package com.moonwatch.db.entity

import androidx.room.*
import java.util.*

@Entity(
    tableName = "token_alert",
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
            Index(value = ["created_at"]),
            Index(value = ["last_fired_at"]),
        ],
)
data class TokenAlertEntity(
    val address: String,
    val active: Boolean,
    @ColumnInfo(name = "created_at") val createdAt: Date,
    @ColumnInfo(name = "last_fired_at") val lastFiredAt: Date,
    @ColumnInfo(name = "sell_price_target_usd") val sellPriceTargetUsd: Double?,
    @ColumnInfo(name = "buy_price_target_usd") val buyPriceTargetUsd: Double?,
) {
  @PrimaryKey(autoGenerate = true) var id: Long = 0
}
