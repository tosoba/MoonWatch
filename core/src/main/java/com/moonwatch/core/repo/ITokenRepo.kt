package com.moonwatch.core.repo

import androidx.paging.PagingData
import com.moonwatch.core.model.IToken
import com.moonwatch.core.model.ITokenValue
import com.moonwatch.core.model.ITokenWithValue
import kotlinx.coroutines.flow.Flow

interface ITokenRepo {
  fun getTokensWithValue(pageSize: Int): Flow<PagingData<ITokenWithValue>>
  suspend fun getTokenWithValueByAddress(address: String): ITokenWithValue
  suspend fun saveTokenWithValue(token: IToken, value: ITokenValue)
  suspend fun deleteTokenByAddress(address: String)
}
