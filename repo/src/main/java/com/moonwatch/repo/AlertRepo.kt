package com.moonwatch.repo

import com.moonwatch.core.model.ITokenAlertWithValue
import com.moonwatch.core.repo.IAlertRepo
import com.moonwatch.db.dao.AlertDao
import com.moonwatch.db.entity.TokenAlertEntity
import dagger.Reusable
import java.math.BigDecimal
import java.util.*
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

@Reusable
class AlertRepo @Inject constructor(private val dao: AlertDao) : IAlertRepo {
  override fun getTokenAlertsWithValue(): Flow<List<ITokenAlertWithValue>> =
      dao.selectTokenAlertsWithLatestValueOrderedByCreatedAt()

  override suspend fun addAlert(
      address: String,
      sellPriceTargetUsd: BigDecimal?,
      buyPriceTargetUsd: BigDecimal?
  ) {
    dao.insertAlert(
        TokenAlertEntity(
            address = address,
            active = true,
            createdAt = Date(),
            sellPriceTargetUsd = sellPriceTargetUsd,
            buyPriceTargetUsd = buyPriceTargetUsd,
        ),
    )
  }

  override suspend fun deleteAlert(id: Long) {
    dao.deleteAlertById(id)
  }

  override suspend fun toggleAlertActive(id: Long) {
    dao.updateToggleAlertActiveById(id)
  }

  override suspend fun updateAlert(
      id: Long,
      sellPriceTargetUsd: BigDecimal?,
      buyPriceTargetUsd: BigDecimal?
  ) {
    dao.updateTokenAlertPriceTargetsById(
        id,
        sellPriceTargetUsd = sellPriceTargetUsd,
        buyPriceTargetUsd = buyPriceTargetUsd,
    )
  }
}
