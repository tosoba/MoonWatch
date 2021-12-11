package com.moonwatch.core.repo

import com.moonwatch.core.model.ITokenAlertWithValue
import java.math.BigDecimal
import kotlinx.coroutines.flow.Flow

interface IAlertRepo {
  fun getTokenAlertsWithValue(): Flow<List<ITokenAlertWithValue>>
  suspend fun addAlert(
      address: String,
      sellPriceTargetUsd: BigDecimal?,
      buyPriceTargetUsd: BigDecimal?
  )
}
