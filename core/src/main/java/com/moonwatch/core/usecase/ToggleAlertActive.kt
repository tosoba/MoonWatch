package com.moonwatch.core.usecase

import com.moonwatch.core.repo.IAlertRepo
import dagger.Reusable
import javax.inject.Inject

@Reusable
class ToggleAlertActive @Inject constructor(private val repo: IAlertRepo) {
  suspend operator fun invoke(id: Long) {
    repo.toggleAlertActive(id)
  }
}
