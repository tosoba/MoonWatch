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

    val thresholdPercentage = .15 // TODO: move to shared prefs + split for buy/sell
    val alerts = alertDao.selectActiveTokenAlerts()
    val alertIdsToFire = mutableListOf<Long>()
    val silentAlerts = mutableListOf<TokenAlertEntity>()
    val loudAlerts = mutableListOf<TokenAlertEntity>()
    alerts.forEach { alert ->
      if (!updatedValues.containsKey(alert.address)) return@forEach
      val updatedValue = updatedValues[alert.address]

      val sellPriceTarget = alert.sellPriceTargetUsd
      if (sellPriceTarget != null) {}

      val buyPriceTarget = alert.buyPriceTargetUsd
      if (buyPriceTarget != null) {}
    }

    if (alertIdsToFire.isNotEmpty()) {
      alertDao.updateLastFiredAtForAlerts(alertIdsToFire, Date())
    }

    return Result.success()
  }
}
