package com.moonwatch.core.usecase

import com.moonwatch.core.model.ITokenWithValue
import com.moonwatch.core.repo.ITokenRepo
import dagger.Reusable
import javax.inject.Inject

@Reusable
class GetTokenWithValue @Inject constructor(private val repo: ITokenRepo) {
  suspend operator fun invoke(address: String): ITokenWithValue =
      repo.getTokenWithValueByAddress(address)
}
