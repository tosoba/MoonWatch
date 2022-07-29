package com.moonwatch.core.repo

import androidx.paging.PagingData
import com.moonwatch.core.model.ITokenAlertWithValues
import kotlinx.coroutines.flow.Flow
import java.math.BigDecimal

interface IAlertRepo {
  fun getTokenAlertsWithValues(pageSize: Int): Flow<PagingData<ITokenAlertWithValues>>

  suspend fun getTokenAlertWithValues(id: Long): ITokenAlertWithValues

  suspend fun addAlert(
      address: String,
      creationValueId: Long,
      sellPriceTargetUsd: BigDecimal?,
      buyPriceTargetUsd: BigDecimal?
  )

  suspend fun deleteAlert(id: Long)

  suspend fun toggleAlertActive(id: Long)

  suspend fun updateAlert(id: Long, sellPriceTargetUsd: BigDecimal?, buyPriceTargetUsd: BigDecimal?)

  val useAlarmsFlow: Flow<Boolean>

  suspend fun toggleUseAlarms(): Boolean
}
