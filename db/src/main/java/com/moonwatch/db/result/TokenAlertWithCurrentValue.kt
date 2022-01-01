package com.moonwatch.db.result

import androidx.room.Embedded
import com.moonwatch.core.model.ITokenAlertWithCurrentValue
import com.moonwatch.db.entity.TokenAlertEntity
import com.moonwatch.db.entity.TokenEntity
import com.moonwatch.db.entity.TokenValueEntity

data class TokenAlertWithCurrentValue(
    @Embedded override val alert: TokenAlertEntity,
    @Embedded(prefix = "token_") override val token: TokenEntity,
    @Embedded(prefix = "value_") override val currentValue: TokenValueEntity
) : ITokenAlertWithCurrentValue
