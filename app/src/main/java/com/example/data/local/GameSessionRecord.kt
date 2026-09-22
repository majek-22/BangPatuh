package com.example.data.local

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "game_sessions")
data class GameSessionRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val username: String,
    val levelNumber: Int,
    val missionName: String,
    val score: Int,
    val violationsSliced: Int,
    val trapsAvoided: Int,
    val trapsSliced: Int,
    val accuracyPercent: Int,
    val timestamp: Long = System.currentTimeMillis()
)

@Dao
interface GameSessionDao {
    @Query("SELECT * FROM game_sessions WHERE username = :username ORDER BY timestamp DESC LIMIT 20")
    fun getRecentSessions(username: String): Flow<List<GameSessionRecord>>

    @Query("SELECT * FROM game_sessions WHERE username = :username ORDER BY timestamp DESC LIMIT 20")
    suspend fun getRecentSessionsDirect(username: String): List<GameSessionRecord>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(record: GameSessionRecord)
}
