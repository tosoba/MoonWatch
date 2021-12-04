package com.moonwatch.core.repo

import com.moonwatch.core.model.ITokenWithValue
import kotlinx.coroutines.flow.Flow

interface ITokenRepo {
  fun getTokensWithValue(): Flow<List<ITokenWithValue>>
}
