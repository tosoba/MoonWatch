package com.moonwatch.core.usecase

import androidx.paging.PagingData
import com.moonwatch.core.model.ITokenAlertWithValues
import com.moonwatch.core.repo.IAlertRepo
import dagger.Reusable
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@Reusable
class GetAlertsFlow @Inject constructor(private val repo: IAlertRepo) {
  operator fun invoke(pageSize: Int): Flow<PagingData<ITokenAlertWithValues>> =
      repo.getTokenAlertsWithValues(pageSize)
}
