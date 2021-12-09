package com.moonwatch.repo

import com.moonwatch.api.pancakeswap.PancakeswapEndpoints
import com.moonwatch.core.model.IToken
import com.moonwatch.core.model.ITokenValue
import com.moonwatch.core.model.ITokenWithValue
import com.moonwatch.core.repo.ITokenRepo
import com.moonwatch.db.dao.TokenDao
import com.moonwatch.db.entity.TokenEntity
import com.moonwatch.db.entity.TokenValueEntity
import dagger.Reusable
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

@Reusable
class TokenRepo
@Inject
constructor(
    private val dao: TokenDao,
    private val pancakeswapEndpoints: PancakeswapEndpoints,
) : ITokenRepo {
  override fun getTokensWithValue(): Flow<List<ITokenWithValue>> =
      dao.selectTokensWithLatestValueOrderedByUsdDesc()

  override suspend fun getTokenWithValueByAddress(address: String): ITokenWithValue =
      pancakeswapEndpoints.getToken(address).also { it.tokenAddress = address }

  override suspend fun saveTokenWithValue(token: IToken, value: ITokenValue) {
    dao.insertTokenWithValue(TokenEntity(token), TokenValueEntity(value))
  }

  override suspend fun deleteTokenByAddress(address: String) {
    dao.deleteTokenByAddress(address)
  }
}
