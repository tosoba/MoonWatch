package com.moonwatch.repo

import com.moonwatch.core.model.ITokenWithValue
import com.moonwatch.core.repo.ITokenRepo
import com.moonwatch.db.dao.TokenDao
import dagger.Reusable
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

@Reusable
class TokenRepo @Inject constructor(private val dao: TokenDao) : ITokenRepo {
  override fun getTokensWithValue(): Flow<List<ITokenWithValue>> =
      dao.selectTokensWithLatestValueOrderedByUsdDesc()
}
