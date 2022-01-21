package com.moonwatch.core.usecase

import com.moonwatch.core.repo.IAlertRepo
import dagger.Reusable
import javax.inject.Inject

@Reusable
class ToggleUseAlarms @Inject constructor(private val alertRepo: IAlertRepo) {
  suspend operator fun invoke(): Boolean = alertRepo.toggleUseAlarms()
}
