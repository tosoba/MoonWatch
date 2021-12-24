package com.moonwatch.core.usecase

import androidx.paging.PagingData
import com.moonwatch.core.model.ITokenAlertWithValue
import com.moonwatch.core.repo.IAlertRepo
import dagger.Reusable
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

@Reusable
class GetAlertsFlow @Inject constructor(private val repo: IAlertRepo) {
  operator fun invoke(pageSize: Int): Flow<PagingData<ITokenAlertWithValue>> =
      repo.getTokenAlertsWithValue(pageSize)
}
