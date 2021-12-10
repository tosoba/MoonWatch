package com.moonwatch.core.usecase

import com.moonwatch.core.repo.IAlertRepo
import dagger.Reusable
import javax.inject.Inject

@Reusable
class AddAlert @Inject constructor(private val repo: IAlertRepo) {
  suspend operator fun invoke(
      address: String,
      sellPriceTargetUsd: Double?,
      buyPriceTargetUsd: Double?
  ) {
    if (sellPriceTargetUsd == null && buyPriceTargetUsd == null) {
      throw IllegalArgumentException()
    }
    repo.addAlert(address, sellPriceTargetUsd, buyPriceTargetUsd)
  }
}
