package com.moonwatch.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.moonwatch.core.model.Chain
import com.moonwatch.db.entity.TokenEntity
import com.moonwatch.db.entity.TokenValueEntity
import java.util.*

@Dao
interface TokenDao {
  @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertToken(token: TokenEntity)

  @Insert suspend fun insertTokenValues(tokenValues: List<TokenValueEntity>)

  @Query("SELECT * FROM token WHERE chain = :chain")
  suspend fun selectTokensByChain(chain: Chain): List<TokenEntity>

  @Query("DELETE FROM token_value WHERE updated_at < :timestamp")
  suspend fun deleteTokenValuesOlderThen(timestamp: Date)
}
