package com.moonwatch.core.usecase

import com.moonwatch.core.repo.IAlertRepo
import dagger.Reusable
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@Reusable
class UseAlarmsFlow @Inject constructor(private val alertRepo: IAlertRepo) {
  operator fun invoke(): Flow<Boolean> = alertRepo.useAlarmsFlow
}
