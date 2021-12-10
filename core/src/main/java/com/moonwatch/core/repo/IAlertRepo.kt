package com.moonwatch.core.repo

import com.moonwatch.core.model.ITokenAlertWithValue
import kotlinx.coroutines.flow.Flow

interface IAlertRepo {
  fun getTokenAlertsWithValue(): Flow<List<ITokenAlertWithValue>>
  suspend fun addAlert(address: String, sellPriceTargetUsd: Double?, buyPriceTargetUsd: Double?)
}
