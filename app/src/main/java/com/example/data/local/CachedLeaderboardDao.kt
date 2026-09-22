package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface CachedLeaderboardDao {
    @Query("SELECT * FROM cached_leaderboard ORDER BY rank ASC LIMIT 100")
    fun getAll(): Flow<List<CachedLeaderboardEntry>>

    @Query("SELECT * FROM cached_leaderboard ORDER BY rank ASC LIMIT 100")
    suspend fun getAllDirect(): List<CachedLeaderboardEntry>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<CachedLeaderboardEntry>)

    @Query("DELETE FROM cached_leaderboard")
    suspend fun clear()

    @Transaction
    suspend fun replaceAll(entries: List<CachedLeaderboardEntry>) {
        clear()
        insertAll(entries)
    }
}
