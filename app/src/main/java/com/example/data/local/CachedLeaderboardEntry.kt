package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_leaderboard")
data class CachedLeaderboardEntry(
    @PrimaryKey
    val rank: Int,
    val username: String,
    val bestScore: Int,
    val highestTierReached: Int = 0,
    val bestScoreLevel: Int = 1,
    val difficulty: String = "Auto",
    val updatedAt: Long = System.currentTimeMillis()
)
