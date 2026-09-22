package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UserStatsDao {
    @Query("SELECT * FROM user_stats WHERE username = :username LIMIT 1")
    fun getStats(username: String): Flow<UserStats?>

    @Query("SELECT * FROM user_stats WHERE username = :username LIMIT 1")
    suspend fun getStatsDirect(username: String): UserStats?

    @Query("SELECT * FROM user_stats ORDER BY overallBestScore DESC LIMIT 100")
    suspend fun getAllStatsDirect(): List<UserStats>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(stats: UserStats)
}
