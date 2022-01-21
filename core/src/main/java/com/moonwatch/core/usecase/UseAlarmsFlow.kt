package com.moonwatch.core.usecase

import com.moonwatch.core.repo.IAlertRepo
import dagger.Reusable
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

@Reusable
class UseAlarmsFlow @Inject constructor(private val alertRepo: IAlertRepo) {
  operator fun invoke(): Flow<Boolean> = alertRepo.useAlarmsFlow
}
