package com.moonwatch.repo.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.moonwatch.db.dao.TokenDao
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.*

@HiltWorker
class DeleteOldTokenValuesWorker
@AssistedInject
constructor(
    @Assisted ctx: Context,
    @Assisted params: WorkerParameters,
    private val tokenDao: TokenDao
) : CoroutineWorker(ctx, params) {
  override suspend fun doWork(): Result {
    tokenDao.deleteTokenValuesOlderThen(
        timestamp =
            Calendar.getInstance().run {
              add(Calendar.DATE, -7)
              time
            })
    return Result.success()
  }
}
