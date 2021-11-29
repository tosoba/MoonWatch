package com.moonwatch.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

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
    @PrimaryKey val address: String,
    val name: String,
    val symbol: String,
    val chain: Chain
)
