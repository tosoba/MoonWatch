package com.moonwatch

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.*
import com.jakewharton.threetenabp.AndroidThreeTen
import com.moonwatch.repo.notification.AlertNotificationManager
import com.moonwatch.repo.worker.DeleteOldTokenValuesWorker
import com.moonwatch.repo.worker.TokenValuesSyncWorker
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class MoonwatchApp : Application(), Configuration.Provider {
  @Inject internal lateinit var workerFactory: HiltWorkerFactory

  override fun onCreate() {
    super.onCreate()
    AndroidThreeTen.init(this)
    AlertNotificationManager.createChannel(this)
    enqueueTokenValuesSync()
    enqueueDeletingOldTokenValues()
  }

  override fun getWorkManagerConfiguration() =
      Configuration.Builder().setWorkerFactory(workerFactory).build()

  private fun enqueueTokenValuesSync() {
    val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
    val periodicSyncDataWork =
        PeriodicWorkRequest.Builder(TokenValuesSyncWorker::class.java, 15, TimeUnit.MINUTES)
            .addTag(TokenValuesSyncWorker::class.simpleName!!)
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.LINEAR, PeriodicWorkRequest.MIN_BACKOFF_MILLIS, TimeUnit.MILLISECONDS)
            .build()
    WorkManager.getInstance(this)
        .enqueueUniquePeriodicWork(
            TokenValuesSyncWorker::class.simpleName!!,
            ExistingPeriodicWorkPolicy.KEEP,
            periodicSyncDataWork)
  }

  private fun enqueueDeletingOldTokenValues() {
    val periodicSyncDataWork =
        PeriodicWorkRequest.Builder(TokenValuesSyncWorker::class.java, 1, TimeUnit.DAYS)
            .addTag(DeleteOldTokenValuesWorker::class.simpleName!!)
            .build()
    WorkManager.getInstance(this)
        .enqueueUniquePeriodicWork(
            DeleteOldTokenValuesWorker::class.simpleName!!,
            ExistingPeriodicWorkPolicy.KEEP,
            periodicSyncDataWork)
  }
}
