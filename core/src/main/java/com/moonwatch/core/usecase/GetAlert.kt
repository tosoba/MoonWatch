package com.moonwatch.core.usecase

import com.moonwatch.core.model.ITokenAlertWithValues
import com.moonwatch.core.repo.IAlertRepo
import dagger.Reusable
import javax.inject.Inject

@Reusable
class GetAlert @Inject constructor(private val repo: IAlertRepo) {
  suspend operator fun invoke(id: Long): ITokenAlertWithValues = repo.getTokenAlertWithValues(id)
}
