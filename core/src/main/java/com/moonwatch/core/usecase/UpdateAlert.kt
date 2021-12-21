package com.moonwatch.core.usecase

import com.moonwatch.core.repo.IAlertRepo
import dagger.Reusable
import java.math.BigDecimal
import javax.inject.Inject

@Reusable
class UpdateAlert @Inject constructor(private val repo: IAlertRepo) {
  suspend operator fun invoke(
      id: Long,
      sellPriceTargetUsd: BigDecimal?,
      buyPriceTargetUsd: BigDecimal?
  ) {
    if (sellPriceTargetUsd == null && buyPriceTargetUsd == null) {
      throw IllegalArgumentException()
    }
    repo.updateAlert(id, sellPriceTargetUsd, buyPriceTargetUsd)
  }
}
