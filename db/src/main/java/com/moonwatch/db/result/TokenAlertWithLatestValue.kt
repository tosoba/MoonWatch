package com.moonwatch.db.result

import androidx.room.Embedded
import com.moonwatch.core.model.ITokenAlertWithValue
import com.moonwatch.db.entity.TokenAlertEntity
import com.moonwatch.db.entity.TokenEntity
import com.moonwatch.db.entity.TokenValueEntity

data class TokenAlertWithLatestValue(
    @Embedded override val alert: TokenAlertEntity,
    @Embedded(prefix = "token_") override val token: TokenEntity,
    @Embedded(prefix = "value_") override val value: TokenValueEntity
) : ITokenAlertWithValue
