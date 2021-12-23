package com.moonwatch.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.moonwatch.db.entity.TokenAlertEntity
import com.moonwatch.db.result.TokenAlertWithLatestValue
import java.math.BigDecimal
import kotlinx.coroutines.flow.Flow
import org.threeten.bp.LocalDateTime

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
  suspend fun updateLastFiredAtForAlerts(ids: List<Long>, timestamp: LocalDateTime)

  @Query("UPDATE token_alert SET active = CASE WHEN active = 0 THEN 1 ELSE 0 END WHERE id = :id")
  suspend fun updateToggleAlertActiveById(id: Long)

  @Query(
      """SELECT a.*, 
    t.address AS token_address, t.name AS token_name, 
    t.symbol AS token_symbol, t.chain AS token_chain,
    v.address AS value_address, v.usd AS value_usd,
    v.bnb AS value_bnb, v.eth AS value_eth,
    v.updated_at AS value_updated_at, v.id AS value_id
    FROM token_alert a 
    INNER JOIN token AS t ON a.address = t.address 
    INNER JOIN token_value v ON v.address = t.address 
    WHERE v.updated_at = (SELECT MAX(updated_at) FROM token_value WHERE address = t.address LIMIT 1) 
    ORDER BY v.usd DESC""")
  fun selectTokenAlertsWithLatestValueOrderedByCreatedAt(): Flow<List<TokenAlertWithLatestValue>>

  @Query("DELETE FROM token_alert WHERE id = :id") suspend fun deleteAlertById(id: Long)

  @Query(
      """UPDATE token_alert 
          SET sell_price_target_usd = :sellPriceTargetUsd, sell_price_target_usd = :buyPriceTargetUsd 
          WHERE id = :id""")
  suspend fun updateTokenAlertPriceTargetsById(
      id: Long,
      sellPriceTargetUsd: BigDecimal?,
      buyPriceTargetUsd: BigDecimal?
  )
}
