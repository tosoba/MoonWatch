package com.moonwatch.core.repo

import com.moonwatch.core.model.TokenWithValue
import kotlinx.coroutines.flow.Flow

interface ITokenRepo {
  fun getTokensWithValue(): Flow<List<TokenWithValue>>
}
