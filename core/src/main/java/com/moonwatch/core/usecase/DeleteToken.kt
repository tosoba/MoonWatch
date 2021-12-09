package com.moonwatch.core.usecase

import com.moonwatch.core.repo.ITokenRepo
import dagger.Reusable
import javax.inject.Inject

@Reusable
class DeleteToken @Inject constructor(private val repo: ITokenRepo) {
  suspend operator fun invoke(address: String) = repo.deleteTokenByAddress(address)
}
