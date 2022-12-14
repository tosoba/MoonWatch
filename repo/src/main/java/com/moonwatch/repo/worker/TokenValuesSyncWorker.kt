package com.moonwatch.repo.worker

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Build
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.moonwatch.api.pancakeswap.PancakeswapEndpoints
import com.moonwatch.core.android.ext.millisToLocalDateTime
import com.moonwatch.core.di.PancakeswapEndpointsWithExtendedTimeouts
import com.moonwatch.core.model.Chain
import com.moonwatch.core.model.ITokenAlertWithCurrentValue
import com.moonwatch.core.repo.IAlertRepo
import com.moonwatch.db.dao.AlertDao
import com.moonwatch.db.dao.TokenDao
import com.moonwatch.db.entity.TokenValueEntity
import com.moonwatch.repo.notification.AlertNotificationManager
import com.moonwatch.repo.receiver.TokenAlertBroadcastReceiver
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull
import org.threeten.bp.LocalDateTime
import timber.log.Timber

@HiltWorker
class TokenValuesSyncWorker
@AssistedInject
constructor(
    @Assisted ctx: Context,
    @Assisted params: WorkerParameters,
    private val alertDao: AlertDao,
    private val tokenDao: TokenDao,
    @PancakeswapEndpointsWithExtendedTimeouts
    private val pancakeswapEndpoints: PancakeswapEndpoints,
    private val alertRepo: IAlertRepo,
    private val alertNotificationManager: AlertNotificationManager,
) : CoroutineWorker(ctx, params) {
  override suspend fun doWork(): Result {
    val bscTokens = tokenDao.selectTokensByChain(Chain.BSC)
    if (bscTokens.isEmpty()) return Result.success()

    val updatedValues = mutableMapOf<String, TokenValueEntity>()
    bscTokens.forEach { token ->
      try {
        val (tokenData, updatedAtMillis) = pancakeswapEndpoints.getToken(token.address)
        updatedValues[token.address] =
            TokenValueEntity(
                address = token.address,
                usd = tokenData.priceInUsd.toBigDecimal(),
                bnb = tokenData.priceInBnb.toBigDecimal(),
                updatedAt = updatedAtMillis.millisToLocalDateTime,
            )
        delay(500L)
      } catch (ex: Exception) {
        Timber.tag("GET_BSC_TOKEN").e(ex)
      }
    }
    if (updatedValues.isNotEmpty()) {
      tokenDao.insertTokenValues(updatedValues.values.toList())
    } else {
      return Result.failure()
    }

    val groupedAlerts = alertDao.selectActiveTokenAlerts().groupBy { it.token.address }
    if (groupedAlerts.isEmpty()) return Result.success()

    val alertIdsToFire = mutableListOf<Long>()
    val sellAlertsToFire = mutableListOf<ITokenAlertWithCurrentValue>()
    val buyAlertsToFire = mutableListOf<ITokenAlertWithCurrentValue>()

    updatedValues.forEach { (address, value) ->
      val alerts = groupedAlerts[address] ?: return@forEach
      alerts
          .filter { (alert) -> alert.sellPriceTargetUsd?.let { it <= value.usd } ?: false }
          .maxByOrNull { (alert) -> alert.sellPriceTargetUsd!! }
          ?.let {
            alertIdsToFire.add(it.alert.id)
            sellAlertsToFire.add(it)
          }
      alerts
          .filter { (alert) -> alert.buyPriceTargetUsd?.let { it >= value.usd } ?: false }
          .minByOrNull { (alert) -> alert.buyPriceTargetUsd!! }
          ?.let {
            alertIdsToFire.add(it.alert.id)
            buyAlertsToFire.add(it)
          }
    }

    if (alertIdsToFire.isNotEmpty()) {
      alertNotificationManager.show(sellAlerts = sellAlertsToFire, buyAlerts = buyAlertsToFire)
      if (shouldTriggerAlarm()) triggerAlarm()
      alertDao.updateLastFiredAtForAlerts(alertIdsToFire, LocalDateTime.now())
    }

    return Result.success()
  }

  @SuppressLint("MissingPermission")
  private fun triggerAlarm() {
    val pendingIntent =
        PendingIntent.getBroadcast(
            applicationContext,
            1,
            Intent(applicationContext, TokenAlertBroadcastReceiver::class.java),
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
              PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            } else {
              PendingIntent.FLAG_UPDATE_CURRENT
            },
        )
    val alarmManager = applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    alarmManager.apply {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, 0, pendingIntent)
      } else {
        setExact(AlarmManager.RTC_WAKEUP, 0, pendingIntent)
      }
    }
  }

  private suspend fun shouldTriggerAlarm(): Boolean {
    val useAlarms = alertRepo.useAlarmsFlow.firstOrNull() ?: return false
    val alarmManager = applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val audioManager = applicationContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    val silentOrVibrateMode = audioManager.ringerMode != AudioManager.RINGER_MODE_NORMAL
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      useAlarms && silentOrVibrateMode && alarmManager.canScheduleExactAlarms()
    } else {
      useAlarms && silentOrVibrateMode
    }
  }
}
