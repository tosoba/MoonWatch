package com.moonwatch.core.repo

import com.moonwatch.core.model.TokenAlertWithValue
import kotlinx.coroutines.flow.Flow

interface IAlertRepo {
  fun getTokenAlertsWithValue(): Flow<List<TokenAlertWithValue>>
}
