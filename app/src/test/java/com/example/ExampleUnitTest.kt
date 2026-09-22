package com.example

import com.example.data.SessionManager
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun sessionManager_validWithin30Days() {
        val now = 1700000000000L
        val twentyNineDaysAgo = now - (29L * 24 * 60 * 60 * 1000L)
        assertTrue("Session active within 29 days should be valid", SessionManager.isSessionValid(twentyNineDaysAgo, now))

        val thirtyDaysAgo = now - (30L * 24 * 60 * 60 * 1000L)
        assertTrue("Session active exactly at 30 days should be valid", SessionManager.isSessionValid(thirtyDaysAgo, now))
    }

    @Test
    fun sessionManager_expiredAfter30Days() {
        val now = 1700000000000L
        val thirtyOneDaysAgo = now - (31L * 24 * 60 * 60 * 1000L)
        assertFalse("Session active 31 days ago should be expired", SessionManager.isSessionValid(thirtyOneDaysAgo, now))

        val sixtyDaysAgo = now - (60L * 24 * 60 * 60 * 1000L)
        assertFalse("Session active 60 days ago should be expired", SessionManager.isSessionValid(sixtyDaysAgo, now))
    }

    @Test
    fun userStats_levelProgressionAndUnlocking() {
        // New player: only level 1 unlocked
        val freshPlayer = com.example.data.local.UserStats(username = "newbie", maxUnlockedLevel = 1)
        assertTrue(freshPlayer.isLevelUnlocked(1))
        assertFalse(freshPlayer.isLevelUnlocked(2))
        assertFalse(freshPlayer.isLevelUnlocked(3))
        assertFalse(freshPlayer.isLevelUnlocked(4))

        // Restored player from cloud with maxUnlockedLevel = 3
        val cloudPlayer = com.example.data.local.UserStats(username = "veteran", maxUnlockedLevel = 3)
        assertTrue(cloudPlayer.isLevelUnlocked(1))
        assertTrue(cloudPlayer.isLevelUnlocked(2))
        assertTrue(cloudPlayer.isLevelUnlocked(3))
        assertFalse(cloudPlayer.isLevelUnlocked(4))

        // Restored player with level scores (Sector 1: 1500, Sector 2: 2600)
        val scorePlayer = com.example.data.local.UserStats(
            username = "pro",
            level1Best = 1500,
            level2Best = 2600
        )
        assertTrue(scorePlayer.isLevelUnlocked(1))
        assertTrue(scorePlayer.isLevelUnlocked(2))
        assertTrue(scorePlayer.isLevelUnlocked(3))
        assertEquals(3, scorePlayer.calculateMaxUnlockedLevel())
    }
}

