package com.moonwatch.db.result

import androidx.room.Embedded
import com.moonwatch.core.model.ITokenWithValue
import com.moonwatch.db.entity.TokenEntity
import com.moonwatch.db.entity.TokenValueEntity

data class TokenWithValue(
    @Embedded override val token: TokenEntity,
    @Embedded(prefix = "value_") override val value: TokenValueEntity
) : ITokenWithValue
