package com.moonwatch.repo.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.moonwatch.api.pancakeswap.PancakeswapEndpoints
import com.moonwatch.db.dao.AlertDao
import com.moonwatch.db.dao.TokenDao
import com.moonwatch.db.entity.Chain
import com.moonwatch.db.entity.TokenAlertEntity
import com.moonwatch.db.entity.TokenValueEntity
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.*
import kotlinx.coroutines.delay
import timber.log.Timber

@HiltWorker
class TokenValuesSyncWorker
@AssistedInject
constructor(
    @Assisted ctx: Context,
    @Assisted params: WorkerParameters,
    private val alertDao: AlertDao,
    private val tokenDao: TokenDao,
    private val pancakeswapEndpoints: PancakeswapEndpoints
) : CoroutineWorker(ctx, params) {
  override suspend fun doWork(): Result {
    // TODO: tokens with latest values and values from 1 hour ago
    // keep global alert percentage change settings in shared prefs
    val bscTokens = tokenDao.selectTokensByChain(Chain.BSC)
    val updatedValues = mutableMapOf<String, TokenValueEntity>()
    bscTokens.forEach { token ->
      try {
        val (tokenData, updatedAtMillis) = pancakeswapEndpoints.getToken(token.address)
        updatedValues[token.address] =
            TokenValueEntity(
                address = token.address,
                usd = tokenData.priceInUsd,
                bnb = tokenData.priceInBnb,
                updatedAt = Date(updatedAtMillis),
            )
        delay(500L)
      } catch (ex: Exception) {
        Timber.tag("GET_BSC_TOKEN").e(ex)
      }
    }

    if (updatedValues.isNotEmpty()) {
      tokenDao.insertTokenValues(updatedValues.values.toList())
    } else {
      return if (bscTokens.isEmpty()) Result.success() else Result.failure()
    }

    val groupedAlerts = alertDao.selectActiveTokenAlerts().groupBy(TokenAlertEntity::address)
    val alertIdsToFire = mutableListOf<Long>()
    val alertsToFire = mutableListOf<TokenAlertEntity>()
    updatedValues.forEach { (address, value) ->
      val alerts = groupedAlerts[address] ?: return@forEach
      alerts
          .filter { alert -> alert.sellPriceTargetUsd?.let { it > value.usd } ?: false }
          .maxByOrNull { it.sellPriceTargetUsd!! }
          ?.let {
            alertIdsToFire.add(it.id)
            alertsToFire.add(it)
          }
      alerts
          .filter { alert -> alert.buyPriceTargetUsd?.let { it < value.usd } ?: false }
          .minByOrNull { it.buyPriceTargetUsd!! }
          ?.let {
            alertIdsToFire.add(it.id)
            alertsToFire.add(it)
          }
    }

    if (alertIdsToFire.isNotEmpty()) {
      // TODO: create notifications (wake up device)
      alertDao.updateLastFiredAtForAlerts(alertIdsToFire, Date())
    }

    return Result.success()
  }
}
