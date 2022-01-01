package com.moonwatch.repo

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.moonwatch.core.model.ITokenAlertWithValues
import com.moonwatch.core.repo.IAlertRepo
import com.moonwatch.db.dao.AlertDao
import com.moonwatch.db.entity.TokenAlertEntity
import dagger.Reusable
import java.math.BigDecimal
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.threeten.bp.LocalDateTime

@Reusable
class AlertRepo @Inject constructor(private val dao: AlertDao) : IAlertRepo {
  override fun getTokenAlertsWithValues(pageSize: Int): Flow<PagingData<ITokenAlertWithValues>> {
    val pager =
        Pager(PagingConfig(pageSize = pageSize)) {
          dao.selectTokenAlertsWithValuesOrderedByCreatedAt()
        }
    return pager.flow.map { pagingData -> pagingData.map { it } }
  }

  override suspend fun addAlert(
      address: String,
      creationValueId: Long,
      sellPriceTargetUsd: BigDecimal?,
      buyPriceTargetUsd: BigDecimal?
  ) {
    dao.insertAlert(
        TokenAlertEntity(
            address = address,
            creationValueId = creationValueId,
            active = true,
            createdAt = LocalDateTime.now(),
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
