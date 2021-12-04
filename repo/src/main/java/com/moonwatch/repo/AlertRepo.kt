package com.moonwatch.repo

import com.moonwatch.core.model.ITokenAlertWithValue
import com.moonwatch.core.repo.IAlertRepo
import com.moonwatch.db.dao.AlertDao
import dagger.Reusable
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

@Reusable
class AlertRepo @Inject constructor(private val dao: AlertDao) : IAlertRepo {
  override fun getTokenAlertsWithValue(): Flow<List<ITokenAlertWithValue>> = emptyFlow()
}
