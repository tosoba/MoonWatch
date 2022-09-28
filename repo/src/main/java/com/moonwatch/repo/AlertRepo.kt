package com.moonwatch.repo

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.moonwatch.core.android.ext.dataStore
import com.moonwatch.core.model.ITokenAlertWithValues
import com.moonwatch.core.repo.IAlertRepo
import com.moonwatch.db.dao.AlertDao
import com.moonwatch.db.entity.TokenAlertEntity
import dagger.Reusable
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.threeten.bp.LocalDateTime
import java.math.BigDecimal
import javax.inject.Inject

@Reusable
class AlertRepo
@Inject
constructor(
    @ApplicationContext private val context: Context,
    private val dao: AlertDao,
) : IAlertRepo {
  override fun getTokenAlertsWithValues(pageSize: Int): Flow<PagingData<ITokenAlertWithValues>> {
    val pager =
        Pager(PagingConfig(pageSize = pageSize)) {
          dao.selectTokenAlertsWithValuesOrderedByCreatedAt()
        }
    return pager.flow.map { pagingData -> pagingData.map { it } }
  }

  override suspend fun getTokenAlertWithValues(id: Long): ITokenAlertWithValues =
      dao.selectTokenAlertWithValueById(id)

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

  private val useAlarmsPreferencesKey = booleanPreferencesKey("USE_ALARMS")

  override val useAlarmsFlow: Flow<Boolean>
    get() =
        context.dataStore.data.map { preferences -> preferences[useAlarmsPreferencesKey] ?: false }

  override suspend fun toggleUseAlarms(): Boolean =
      context.dataStore
          .edit { preferences ->
            val useAlarms = preferences[useAlarmsPreferencesKey] ?: false
            preferences[useAlarmsPreferencesKey] = !useAlarms
          }[useAlarmsPreferencesKey]
          ?: false
}
