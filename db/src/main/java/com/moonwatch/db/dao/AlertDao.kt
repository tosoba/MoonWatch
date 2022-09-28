package com.moonwatch.db.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.moonwatch.db.entity.TokenAlertEntity
import com.moonwatch.db.result.TokenAlertWithCurrentValue
import com.moonwatch.db.result.TokenAlertWithValues
import kotlinx.coroutines.flow.Flow
import org.threeten.bp.LocalDateTime
import java.math.BigDecimal

@Dao
interface AlertDao {
  @Insert suspend fun insertAlert(alert: TokenAlertEntity)

  @Query("SELECT * FROM token_alert ORDER BY created_at DESC")
  fun selectTokenAlertsOrderedByCreatedAt(): Flow<TokenAlertEntity>

  @Query("SELECT * FROM token_alert ORDER BY last_fired_at DESC")
  fun selectTokenAlertsOrderedByLastFiredAt(): Flow<TokenAlertEntity>

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
    WHERE a.active = 1 AND v.id = (SELECT MAX(id) FROM token_value WHERE address = t.address) 
    ORDER BY a.created_at DESC""")
  suspend fun selectActiveTokenAlerts(): List<TokenAlertWithCurrentValue>

  @Query("UPDATE token_alert SET last_fired_at = :timestamp WHERE id IN (:ids)")
  suspend fun updateLastFiredAtForAlerts(ids: List<Long>, timestamp: LocalDateTime)

  @Query("UPDATE token_alert SET active = CASE WHEN active = 0 THEN 1 ELSE 0 END WHERE id = :id")
  suspend fun updateToggleAlertActiveById(id: Long)

  @Query(
      """SELECT a.*, 
    t.address AS token_address, t.name AS token_name, 
    t.symbol AS token_symbol, t.chain AS token_chain,
    crv.address AS _creation_value_address, crv.usd AS _creation_value_usd,
    crv.bnb AS _creation_value_bnb, crv.eth AS _creation_value_eth,
    crv.updated_at AS _creation_value_updated_at, crv.id AS _creation_value_id,
    cuv.address AS current_value_address, cuv.usd AS current_value_usd,
    cuv.bnb AS current_value_bnb, cuv.eth AS current_value_eth,
    cuv.updated_at AS current_value_updated_at, cuv.id AS current_value_id
    FROM token_alert a 
    INNER JOIN token AS t ON a.address = t.address 
    INNER JOIN token_value cuv ON cuv.address = t.address 
    INNER JOIN token_value crv ON crv.id = a.creation_value_id
    WHERE cuv.id = (SELECT MAX(id) FROM token_value WHERE address = t.address) 
    ORDER BY a.last_fired_at, a.created_at DESC""")
  fun selectTokenAlertsWithValuesOrderedByCreatedAt(): PagingSource<Int, TokenAlertWithValues>

  @Query(
      """SELECT a.*, 
    t.address AS token_address, t.name AS token_name, 
    t.symbol AS token_symbol, t.chain AS token_chain,
    crv.address AS _creation_value_address, crv.usd AS _creation_value_usd,
    crv.bnb AS _creation_value_bnb, crv.eth AS _creation_value_eth,
    crv.updated_at AS _creation_value_updated_at, crv.id AS _creation_value_id,
    cuv.address AS current_value_address, cuv.usd AS current_value_usd,
    cuv.bnb AS current_value_bnb, cuv.eth AS current_value_eth,
    cuv.updated_at AS current_value_updated_at, cuv.id AS current_value_id
    FROM token_alert a 
    INNER JOIN token AS t ON a.address = t.address 
    INNER JOIN token_value cuv ON cuv.address = t.address 
    INNER JOIN token_value crv ON crv.id = a.creation_value_id
    WHERE cuv.id = (SELECT MAX(id) FROM token_value WHERE address = t.address)  
    AND a.id = :id""")
  suspend fun selectTokenAlertWithValueById(id: Long): TokenAlertWithValues

  @Query("DELETE FROM token_alert WHERE id = :id") suspend fun deleteAlertById(id: Long)

  @Query(
      """UPDATE token_alert 
          SET sell_price_target_usd = :sellPriceTargetUsd, buy_price_target_usd = :buyPriceTargetUsd 
          WHERE id = :id""")
  suspend fun updateTokenAlertPriceTargetsById(
      id: Long,
      sellPriceTargetUsd: BigDecimal?,
      buyPriceTargetUsd: BigDecimal?
  )
}
