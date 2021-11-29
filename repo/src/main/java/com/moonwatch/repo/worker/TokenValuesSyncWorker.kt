package com.moonwatch.repo.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class TokenValuesSyncWorker
@AssistedInject
constructor(@Assisted ctx: Context, @Assisted params: WorkerParameters) : Worker(ctx, params) {
  override fun doWork(): Result {
    TODO("Not yet implemented")
  }
}
