package com.moonwatch.db.dao

import androidx.paging.PagingSource
import androidx.room.*
import com.moonwatch.core.model.Chain
import com.moonwatch.db.entity.TokenEntity
import com.moonwatch.db.entity.TokenValueEntity
import com.moonwatch.db.result.TokenWithLatestValue
import org.threeten.bp.LocalDateTime

@Dao
interface TokenDao {
  @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertToken(token: TokenEntity)

  @Insert suspend fun insertTokenValue(value: TokenValueEntity)

  @Transaction
  suspend fun insertTokenWithValue(token: TokenEntity, value: TokenValueEntity) {
    insertToken(token)
    insertTokenValue(value)
  }

  @Insert suspend fun insertTokenValues(tokenValues: List<TokenValueEntity>)

  @Query("SELECT * FROM token WHERE chain = :chain")
  suspend fun selectTokensByChain(chain: Chain): List<TokenEntity>

  @Query("DELETE FROM token_value WHERE updated_at < :timestamp")
  suspend fun deleteTokenValuesOlderThen(timestamp: LocalDateTime)

  @Query(
      """SELECT t.*, 
    v.address AS value_address, v.usd AS value_usd,
    v.bnb AS value_bnb, v.eth AS value_eth,
    v.updated_at AS value_updated_at, v.id AS value_id
    FROM token AS t  
    INNER JOIN token_value v ON v.address = t.address 
    WHERE v.id = (SELECT MAX(id) FROM token_value WHERE address = t.address) 
    ORDER BY v.usd DESC""")
  fun selectTokensWithLatestValueOrderedByUsdDesc(): PagingSource<Int, TokenWithLatestValue>

  @Query("DELETE FROM token WHERE address = :address")
  suspend fun deleteTokenByAddress(address: String)
}
