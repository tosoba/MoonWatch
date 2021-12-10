package com.moonwatch.db.entity

import androidx.room.*
import com.moonwatch.core.model.ITokenAlert
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
    override val address: String,
    override val active: Boolean,
    @ColumnInfo(name = "created_at") override val createdAt: Date,
    @ColumnInfo(name = "last_fired_at") override val lastFiredAt: Date? = null,
    @ColumnInfo(name = "sell_price_target_usd") override val sellPriceTargetUsd: Double? = null,
    @ColumnInfo(name = "buy_price_target_usd") override val buyPriceTargetUsd: Double? = null,
) : ITokenAlert {
  @PrimaryKey(autoGenerate = true) var id: Long = 0
}
