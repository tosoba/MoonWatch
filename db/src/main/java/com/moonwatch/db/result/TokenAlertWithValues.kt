package com.moonwatch.db.result

import androidx.room.Embedded
import com.moonwatch.core.model.ITokenAlertWithValues
import com.moonwatch.db.entity.TokenAlertEntity
import com.moonwatch.db.entity.TokenEntity
import com.moonwatch.db.entity.TokenValueEntity

data class TokenAlertWithValues(
    @Embedded override val alert: TokenAlertEntity,
    @Embedded(prefix = "token_") override val token: TokenEntity,
    @Embedded(prefix = "_creation_value_") override val creationValue: TokenValueEntity,
    @Embedded(prefix = "current_value_") override val currentValue: TokenValueEntity,
) : ITokenAlertWithValues
