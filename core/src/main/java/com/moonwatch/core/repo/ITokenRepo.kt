package com.moonwatch.core.repo

import com.moonwatch.core.model.IToken
import com.moonwatch.core.model.ITokenValue
import com.moonwatch.core.model.ITokenWithValue
import kotlinx.coroutines.flow.Flow

interface ITokenRepo {
  fun getTokensWithValue(): Flow<List<ITokenWithValue>>
  suspend fun getTokenWithValueByAddress(address: String): ITokenWithValue
  suspend fun saveTokenWithValue(token: IToken, value: ITokenValue)
  suspend fun deleteTokenByAddress(address: String)
}
