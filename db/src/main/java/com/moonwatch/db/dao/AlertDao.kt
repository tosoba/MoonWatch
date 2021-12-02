package com.moonwatch.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.moonwatch.db.entity.TokenAlertEntity
import java.util.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AlertDao {
  @Insert suspend fun insertAlert(alert: TokenAlertEntity)

  @Query("SELECT * FROM token_alert ORDER BY created_at DESC")
  fun selectTokenAlertsOrderedByCreatedAt(): Flow<TokenAlertEntity>

  @Query("SELECT * FROM token_alert ORDER BY last_fired_at DESC")
  fun selectTokenAlertsOrderedByLastFiredAt(): Flow<TokenAlertEntity>

  @Query("SELECT * FROM token_alert WHERE active = 1")
  suspend fun selectActiveTokenAlerts(): List<TokenAlertEntity>

  @Query("UPDATE token_alert SET last_fired_at = :timestamp WHERE id IN (:ids)")
  suspend fun updateLastFiredAtForAlerts(ids: List<Long>, timestamp: Date)

  @Query("UPDATE token_alert SET active = CASE WHEN active = 0 THEN 1 ELSE 0 END WHERE id = :id")
  suspend fun updateToggleAlertActiveById(id: Long)
}
