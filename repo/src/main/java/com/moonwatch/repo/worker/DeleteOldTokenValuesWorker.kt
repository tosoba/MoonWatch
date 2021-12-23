package com.moonwatch.repo.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.moonwatch.db.dao.TokenDao
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import org.threeten.bp.LocalDateTime

@HiltWorker
class DeleteOldTokenValuesWorker
@AssistedInject
constructor(
    @Assisted ctx: Context,
    @Assisted params: WorkerParameters,
    private val tokenDao: TokenDao
) : CoroutineWorker(ctx, params) {
  override suspend fun doWork(): Result {
    tokenDao.deleteTokenValuesOlderThen(timestamp = LocalDateTime.now().minusDays(7))
    return Result.success()
  }
}
