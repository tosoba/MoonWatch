package com.moonwatch.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.moonwatch.core.model.Chain
import com.moonwatch.core.model.IToken

@Entity(
    tableName = "token",
    indices =
        [
            Index(value = ["name"]),
            Index(value = ["symbol"]),
            Index(value = ["chain"]),
        ],
)
data class TokenEntity(
    @PrimaryKey override val address: String,
    override val name: String,
    override val symbol: String,
    override val chain: Chain
) : IToken {
  constructor(other: IToken) : this(other.address, other.name, other.symbol, other.chain)
}
