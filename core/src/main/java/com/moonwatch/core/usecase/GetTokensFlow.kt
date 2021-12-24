package com.moonwatch.core.usecase

import androidx.paging.PagingData
import com.moonwatch.core.model.ITokenWithValue
import com.moonwatch.core.repo.ITokenRepo
import dagger.Reusable
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

@Reusable
class GetTokensFlow @Inject constructor(private val repo: ITokenRepo) {
  operator fun invoke(pageSize: Int): Flow<PagingData<ITokenWithValue>> =
      repo.getTokensWithValue(pageSize)
}
