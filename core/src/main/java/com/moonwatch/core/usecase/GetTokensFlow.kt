package com.moonwatch.core.usecase

import com.moonwatch.core.model.ITokenWithValue
import com.moonwatch.core.repo.ITokenRepo
import dagger.Reusable
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

@Reusable
class GetTokensFlow @Inject constructor(private val repo: ITokenRepo) {
  operator fun invoke(): Flow<List<ITokenWithValue>> = repo.getTokensWithValue()
}
