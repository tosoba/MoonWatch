package com.moonwatch.core.repo

import androidx.paging.PagingData
import com.moonwatch.core.model.ITokenAlertWithValue
import java.math.BigDecimal
import kotlinx.coroutines.flow.Flow

interface IAlertRepo {
  fun getTokenAlertsWithValue(pageSize: Int): Flow<PagingData<ITokenAlertWithValue>>

  suspend fun addAlert(
      address: String,
      sellPriceTargetUsd: BigDecimal?,
      buyPriceTargetUsd: BigDecimal?
  )

  suspend fun deleteAlert(id: Long)

  suspend fun toggleAlertActive(id: Long)

  suspend fun updateAlert(id: Long, sellPriceTargetUsd: BigDecimal?, buyPriceTargetUsd: BigDecimal?)
}
