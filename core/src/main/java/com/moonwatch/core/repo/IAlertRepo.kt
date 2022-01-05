package com.moonwatch.core.repo

import androidx.paging.PagingData
import com.moonwatch.core.model.ITokenAlertWithValues
import java.math.BigDecimal
import kotlinx.coroutines.flow.Flow

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
}
