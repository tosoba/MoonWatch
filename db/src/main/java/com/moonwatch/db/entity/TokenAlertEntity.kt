package com.moonwatch.db.entity

import androidx.room.*
import com.moonwatch.core.model.ITokenAlert
import java.math.BigDecimal
import org.threeten.bp.LocalDateTime

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
            ForeignKey(
                entity = TokenValueEntity::class,
                parentColumns = ["id"],
                childColumns = ["creation_value_id"],
                onDelete = ForeignKey.RESTRICT,
            ),
        ],
    indices =
        [
            Index(value = ["address"]),
            Index(value = ["created_at"]),
            Index(value = ["last_fired_at"]),
            Index(value = ["creation_value_id"]),
        ],
)
data class TokenAlertEntity(
    override val address: String,
    override val active: Boolean,
    @ColumnInfo(name = "creation_value_id") val creationValueId: Long,
    @ColumnInfo(name = "created_at") override val createdAt: LocalDateTime,
    @ColumnInfo(name = "last_fired_at") override val lastFiredAt: LocalDateTime? = null,
    @ColumnInfo(name = "sell_price_target_usd") override val sellPriceTargetUsd: BigDecimal? = null,
    @ColumnInfo(name = "buy_price_target_usd") override val buyPriceTargetUsd: BigDecimal? = null,
) : ITokenAlert {
  @PrimaryKey(autoGenerate = true) override var id: Long = 0
}
