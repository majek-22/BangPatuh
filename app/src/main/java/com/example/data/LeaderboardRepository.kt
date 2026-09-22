package com.example.data

import android.content.Context
import android.util.Log
import com.example.ComplianceApplication
import com.example.data.local.AppDatabase
import com.example.data.local.CachedLeaderboardEntry
import com.example.data.local.UserStats
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

data class LeaderboardItem(
    val rank: Int,
    val username: String,
    val bestScore: Int,
    val highestTierReached: Int = 0,
    val bestScoreLevel: Int = 1,
    val difficulty: String = "Auto",
    val updatedAt: Long
)

data class LeaderboardResult(
    val entries: List<LeaderboardItem>,
    val isOffline: Boolean,
    val errorMessage: String? = null
)

class LeaderboardRepository(
    private val database: AppDatabase,
    private val context: Context? = null
) {
    companion object {
        private const val TAG = "LeaderboardRepo"
        private const val COLLECTION_LEADERBOARD = "leaderboard"
        private const val COLLECTION_USERS = "users"
    }

    private fun getFirestore(): FirebaseFirestore {
        context?.let { ctx ->
            try {
                if (FirebaseApp.getApps(ctx).isEmpty()) {
                    val defaultApp = FirebaseApp.initializeApp(ctx)
                    if (defaultApp == null) {
                        val options = FirebaseOptions.Builder()
                            .setApplicationId(ComplianceApplication.FIREBASE_APP_ID)
                            .setApiKey(ComplianceApplication.FIREBASE_API_KEY)
                            .setProjectId(ComplianceApplication.FIREBASE_PROJECT_ID)
                            .setGcmSenderId(ComplianceApplication.FIREBASE_GCM_SENDER_ID)
                            .setStorageBucket(ComplianceApplication.FIREBASE_STORAGE_BUCKET)
                            .build()
                        FirebaseApp.initializeApp(ctx.applicationContext, options)
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error ensuring Firebase init in getFirestore: ${e.message}")
            }
        }
        return FirebaseFirestore.getInstance()
    }

    fun observeUserStats(username: String): Flow<UserStats?> {
        return database.userStatsDao().getStats(username)
    }

    suspend fun getUserStats(username: String): UserStats? = withContext(Dispatchers.IO) {
        database.userStatsDao().getStatsDirect(username)
    }

    /**
     * Fetch and sync user stats from Room & Firestore.
     * Ensures player's level progression, stars, and all-time best scores are preserved
     * even across uninstall and reinstallation.
     */
    suspend fun fetchAndSyncUserStats(username: String): Pair<UserStats, Int> = withContext(Dispatchers.IO) {
        var localStats = database.userStatsDao().getStatsDirect(username) ?: UserStats(username = username)

        try {
            // 1. Query Firestore users collection
            val userDoc = suspendCancellableCoroutine<DocumentSnapshot?> { continuation ->
                getFirestore().collection(COLLECTION_USERS).document(username.lowercase()).get()
                    .addOnSuccessListener { if (continuation.isActive) continuation.resume(it) }
                    .addOnFailureListener { if (continuation.isActive) continuation.resume(null) }
            }

            // 2. Query Firestore leaderboard collection
            val lbDoc = suspendCancellableCoroutine<DocumentSnapshot?> { continuation ->
                getFirestore().collection(COLLECTION_LEADERBOARD).document(username).get()
                    .addOnSuccessListener { if (continuation.isActive) continuation.resume(it) }
                    .addOnFailureListener { if (continuation.isActive) continuation.resume(null) }
            }

            val remoteLvl1Best = maxOf(
                (userDoc?.getLong("level1Best") ?: 0L).toInt(),
                (lbDoc?.getLong("level1Best") ?: 0L).toInt()
            )
            val remoteLvl2Best = maxOf(
                (userDoc?.getLong("level2Best") ?: 0L).toInt(),
                (lbDoc?.getLong("level2Best") ?: 0L).toInt()
            )
            val remoteLvl3Best = maxOf(
                (userDoc?.getLong("level3Best") ?: 0L).toInt(),
                (lbDoc?.getLong("level3Best") ?: 0L).toInt()
            )
            val remoteLvl4Best = maxOf(
                (userDoc?.getLong("level4Best") ?: 0L).toInt(),
                (lbDoc?.getLong("level4Best") ?: 0L).toInt()
            )

            val remoteLvl1Stars = (userDoc?.getLong("level1Stars") ?: 0L).toInt()
            val remoteLvl2Stars = (userDoc?.getLong("level2Stars") ?: 0L).toInt()
            val remoteLvl3Stars = (userDoc?.getLong("level3Stars") ?: 0L).toInt()
            val remoteLvl4Stars = (userDoc?.getLong("level4Stars") ?: 0L).toInt()

            val remoteMaxUnlocked = maxOf(
                (userDoc?.getLong("maxUnlockedLevel") ?: 1L).toInt(),
                (lbDoc?.getLong("maxUnlockedLevel") ?: 1L).toInt()
            )
            val remoteOverallBest = maxOf(
                (userDoc?.getLong("overallBestScore") ?: 0L).toInt(),
                (lbDoc?.getLong("bestScore") ?: 0L).toInt()
            )
            val remoteHighestTier = maxOf(
                (userDoc?.getLong("highestTierReached") ?: 0L).toInt(),
                (lbDoc?.getLong("highestTierReached") ?: 0L).toInt()
            )

            // Merge local and remote
            val mergedLvl1Best = maxOf(localStats.level1Best, remoteLvl1Best)
            val mergedLvl2Best = maxOf(localStats.level2Best, remoteLvl2Best)
            val mergedLvl3Best = maxOf(localStats.level3Best, remoteLvl3Best)
            val mergedLvl4Best = maxOf(localStats.level4Best, remoteLvl4Best)

            val mergedLvl1Stars = maxOf(localStats.level1Stars, remoteLvl1Stars)
            val mergedLvl2Stars = maxOf(localStats.level2Stars, remoteLvl2Stars)
            val mergedLvl3Stars = maxOf(localStats.level3Stars, remoteLvl3Stars)
            val mergedLvl4Stars = maxOf(localStats.level4Stars, remoteLvl4Stars)

            val mergedOverallBest = maxOf(localStats.overallBestScore, remoteOverallBest)
            val mergedHighestTier = maxOf(localStats.highestTierReached, remoteHighestTier)

            var mergedMaxUnlocked = maxOf(localStats.maxUnlockedLevel, remoteMaxUnlocked)
            if (mergedLvl1Best >= 1200) mergedMaxUnlocked = maxOf(mergedMaxUnlocked, 2)
            if (mergedLvl1Best >= 1200 && mergedLvl2Best >= 2500) mergedMaxUnlocked = maxOf(mergedMaxUnlocked, 3)
            if (mergedLvl1Best >= 1200 && mergedLvl2Best >= 2500 && mergedLvl3Best >= 5000) mergedMaxUnlocked = maxOf(mergedMaxUnlocked, 4)

            val mergedAvatar = if (localStats.avatarId in 1..10) localStats.avatarId else ((userDoc?.getLong("avatarId") ?: 1L).toInt().coerceIn(1, 10))

            val mergedStats = localStats.copy(
                avatarId = mergedAvatar,
                overallBestScore = mergedOverallBest,
                highestTierReached = mergedHighestTier,
                level1Best = mergedLvl1Best,
                level1Stars = mergedLvl1Stars,
                level2Best = mergedLvl2Best,
                level2Stars = mergedLvl2Stars,
                level3Best = mergedLvl3Best,
                level3Stars = mergedLvl3Stars,
                level4Best = mergedLvl4Best,
                level4Stars = mergedLvl4Stars,
                maxUnlockedLevel = mergedMaxUnlocked,
                gamesPlayed = maxOf(localStats.gamesPlayed, (userDoc?.getLong("gamesPlayed") ?: 0L).toInt()),
                totalViolationsSliced = maxOf(localStats.totalViolationsSliced, (userDoc?.getLong("totalViolationsSliced") ?: 0L).toInt()),
                totalTrapsAvoided = maxOf(localStats.totalTrapsAvoided, (userDoc?.getLong("totalTrapsAvoided") ?: 0L).toInt()),
                totalTrapsSliced = maxOf(localStats.totalTrapsSliced, (userDoc?.getLong("totalTrapsSliced") ?: 0L).toInt()),
                bestComboStreak = maxOf(localStats.bestComboStreak, (userDoc?.getLong("bestComboStreak") ?: 0L).toInt()),
                briberySliced = maxOf(localStats.briberySliced, (userDoc?.getLong("briberySliced") ?: 0L).toInt()),
                fraudSliced = maxOf(localStats.fraudSliced, (userDoc?.getLong("fraudSliced") ?: 0L).toInt()),
                moneyLaunderingSliced = maxOf(localStats.moneyLaunderingSliced, (userDoc?.getLong("moneyLaunderingSliced") ?: 0L).toInt()),
                dataBreachSliced = maxOf(localStats.dataBreachSliced, (userDoc?.getLong("dataBreachSliced") ?: 0L).toInt()),
                systemicCorruptionSliced = maxOf(localStats.systemicCorruptionSliced, (userDoc?.getLong("systemicCorruptionSliced") ?: 0L).toInt()),
                otherViolationsSliced = maxOf(localStats.otherViolationsSliced, (userDoc?.getLong("otherViolationsSliced") ?: 0L).toInt()),
                updatedAt = System.currentTimeMillis()
            )

            localStats = mergedStats
            database.userStatsDao().insertOrUpdate(mergedStats)

            // If local had higher progress or remote was missing, sync to Firestore
            if (mergedMaxUnlocked > remoteMaxUnlocked || mergedOverallBest > remoteOverallBest || mergedLvl1Best > remoteLvl1Best) {
                syncUserStatsToFirestore(mergedStats)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Sync user stats error: ${e.message}")
        }

        Pair(localStats, localStats.overallBestScore)
    }

    suspend fun getTop100Leaderboard(): LeaderboardResult = withContext(Dispatchers.IO) {
        var failureError: String? = null
        try {
            val snapshot = suspendCancellableCoroutine { continuation ->
                try {
                    getFirestore().collection(COLLECTION_LEADERBOARD)
                        .orderBy("bestScore", Query.Direction.DESCENDING)
                        .limit(100)
                        .get()
                        .addOnSuccessListener { querySnapshot ->
                            if (continuation.isActive) continuation.resume(querySnapshot)
                        }
                        .addOnFailureListener { exception ->
                            failureError = "Server Offline"
                            Log.e(TAG, "Firestore getTop100Leaderboard failed: ${exception.message}", exception)
                            if (continuation.isActive) continuation.resume(null)
                        }
                } catch (e: Exception) {
                    failureError = "Server Offline"
                    Log.e(TAG, "Firestore query invocation exception: ${e.message}", e)
                    if (continuation.isActive) continuation.resume(null)
                }
            }

            if (snapshot != null) {
                if (!snapshot.isEmpty) {
                    var currentRank = 1
                    val items = snapshot.documents.map { doc ->
                        val username = doc.getString("username") ?: doc.id
                        val score = (doc.getLong("bestScore") ?: 0L).toInt()
                        val tier = (doc.getLong("highestTierReached") ?: doc.getLong("bestScoreLevel") ?: 0L).toInt()
                        val level = (doc.getLong("bestScoreLevel") ?: 1L).toInt()
                        val diff = doc.getString("difficultyEffective") ?: doc.getString("difficulty") ?: "Auto"
                        val updated = doc.getLong("updatedAt") ?: System.currentTimeMillis()
                        LeaderboardItem(
                            rank = currentRank++,
                            username = username,
                            bestScore = score,
                            highestTierReached = tier,
                            bestScoreLevel = level,
                            difficulty = diff,
                            updatedAt = updated
                        )
                    }

                    // Cache in local Room DB
                    val cacheEntities = items.map {
                        CachedLeaderboardEntry(
                            rank = it.rank,
                            username = it.username,
                            bestScore = it.bestScore,
                            highestTierReached = it.highestTierReached,
                            bestScoreLevel = it.bestScoreLevel,
                            difficulty = it.difficulty,
                            updatedAt = it.updatedAt
                        )
                    }
                    database.cachedLeaderboardDao().replaceAll(cacheEntities)

                    LeaderboardResult(entries = items, isOffline = false)
                } else {
                    // Firestore query succeeded, but database collection is currently empty
                    LeaderboardResult(entries = emptyList(), isOffline = false)
                }
            } else {
                // Fallback to room cache with captured error
                val cached = loadFromCache(isOfflineFallback = true)
                cached.copy(errorMessage = failureError ?: "Server Offline")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to fetch remote leaderboard: ${e.message}", e)
            val cached = loadFromCache(isOfflineFallback = true)
            cached.copy(errorMessage = "Server Offline")
        }
    }

    private suspend fun loadFromCache(isOfflineFallback: Boolean): LeaderboardResult {
        val cached = database.cachedLeaderboardDao().getAllDirect()
        val items = if (cached.isNotEmpty()) {
            cached.map {
                LeaderboardItem(
                    rank = it.rank,
                    username = it.username,
                    bestScore = it.bestScore,
                    highestTierReached = it.highestTierReached,
                    bestScoreLevel = it.bestScoreLevel,
                    difficulty = it.difficulty,
                    updatedAt = it.updatedAt
                )
            }
        } else {
            // If cache from remote is empty, show local player stats
            val localStats = database.userStatsDao().getAllStatsDirect()
            var rank = 1
            localStats.filter { it.overallBestScore > 0 }.map {
                LeaderboardItem(
                    rank = rank++,
                    username = it.username,
                    bestScore = it.overallBestScore,
                    highestTierReached = it.highestTierReached,
                    bestScoreLevel = 1,
                    difficulty = "Normal",
                    updatedAt = System.currentTimeMillis()
                )
            }
        }
        return LeaderboardResult(entries = items, isOffline = isOfflineFallback)
    }

    suspend fun recordGameResult(
        username: String,
        levelNumber: Int,
        difficulty: String,
        score: Int,
        stars: Int,
        violationsSliced: Int,
        trapsAvoided: Int,
        trapsSliced: Int,
        maxComboStreak: Int,
        tierReached: Int = 0,
        categoryBreakdown: Map<ComplianceCategory, Int> = emptyMap()
    ): UserStats = withContext(Dispatchers.IO) {
        val existing = database.userStatsDao().getStatsDirect(username) ?: UserStats(username = username)

        val newLevel1Best = if (levelNumber == 1) maxOf(existing.level1Best, score) else existing.level1Best
        val newLevel1Stars = if (levelNumber == 1) maxOf(existing.level1Stars, stars) else existing.level1Stars

        val newLevel2Best = if (levelNumber == 2) maxOf(existing.level2Best, score) else existing.level2Best
        val newLevel2Stars = if (levelNumber == 2) maxOf(existing.level2Stars, stars) else existing.level2Stars

        val newLevel3Best = if (levelNumber == 3) maxOf(existing.level3Best, score) else existing.level3Best
        val newLevel3Stars = if (levelNumber == 3) maxOf(existing.level3Stars, stars) else existing.level3Stars

        val newLevel4Best = if (levelNumber == 4) maxOf(existing.level4Best, score) else existing.level4Best
        val newLevel4Stars = if (levelNumber == 4) maxOf(existing.level4Stars, stars) else existing.level4Stars

        // Calculate max unlocked level based on performance and existing progression
        var calculatedMaxUnlocked = maxOf(existing.maxUnlockedLevel, levelNumber)
        if (newLevel1Best >= 1200) calculatedMaxUnlocked = maxOf(calculatedMaxUnlocked, 2)
        if (newLevel1Best >= 1200 && newLevel2Best >= 2500) calculatedMaxUnlocked = maxOf(calculatedMaxUnlocked, 3)
        if (newLevel1Best >= 1200 && newLevel2Best >= 2500 && newLevel3Best >= 5000) calculatedMaxUnlocked = maxOf(calculatedMaxUnlocked, 4)

        val isNewOverallBest = score > existing.overallBestScore
        val newOverallBest = if (isNewOverallBest) score else existing.overallBestScore
        val newBestLevel = if (isNewOverallBest) levelNumber else existing.bestScoreLevel
        val newBestDiff = if (isNewOverallBest) difficulty else existing.bestScoreDifficulty
        val newHighestTier = maxOf(existing.highestTierReached, tierReached)

        val briberyHits = categoryBreakdown[ComplianceCategory.BRIBERY] ?: 0
        val fraudHits = categoryBreakdown[ComplianceCategory.FRAUD] ?: 0
        val amlHits = categoryBreakdown[ComplianceCategory.MONEY_LAUNDERING] ?: 0
        val dataBreachHits = categoryBreakdown[ComplianceCategory.DATA_BREACH] ?: 0
        val systemicHits = categoryBreakdown[ComplianceCategory.SYSTEMIC_CORRUPTION] ?: 0
        val insiderHits = categoryBreakdown[ComplianceCategory.INSIDER_TRADING] ?: 0
        val conflictHits = categoryBreakdown[ComplianceCategory.CONFLICT_OF_INTEREST] ?: 0
        val embezzleHits = categoryBreakdown[ComplianceCategory.EMBEZZLEMENT] ?: 0
        val otherHits = insiderHits + conflictHits + embezzleHits

        val updatedStats = existing.copy(
            overallBestScore = newOverallBest,
            highestTierReached = newHighestTier,
            bestScoreLevel = newBestLevel,
            bestScoreDifficulty = newBestDiff,
            gamesPlayed = existing.gamesPlayed + 1,
            totalViolationsSliced = existing.totalViolationsSliced + violationsSliced,
            totalTrapsAvoided = existing.totalTrapsAvoided + trapsAvoided,
            totalTrapsSliced = existing.totalTrapsSliced + trapsSliced,
            bestComboStreak = maxOf(existing.bestComboStreak, maxComboStreak),
            briberySliced = existing.briberySliced + briberyHits,
            fraudSliced = existing.fraudSliced + fraudHits,
            moneyLaunderingSliced = existing.moneyLaunderingSliced + amlHits,
            dataBreachSliced = existing.dataBreachSliced + dataBreachHits,
            systemicCorruptionSliced = existing.systemicCorruptionSliced + systemicHits,
            otherViolationsSliced = existing.otherViolationsSliced + otherHits,
            level1Best = newLevel1Best,
            level1Stars = newLevel1Stars,
            level2Best = newLevel2Best,
            level2Stars = newLevel2Stars,
            level3Best = newLevel3Best,
            level3Stars = newLevel3Stars,
            level4Best = newLevel4Best,
            level4Stars = newLevel4Stars,
            maxUnlockedLevel = calculatedMaxUnlocked,
            updatedAt = System.currentTimeMillis()
        )

        database.userStatsDao().insertOrUpdate(updatedStats)

        // Record individual session
        val totalInteractions = violationsSliced + trapsSliced
        val accuracy = if (totalInteractions > 0) {
            ((violationsSliced.toFloat() / totalInteractions) * 100).toInt()
        } else 100

        try {
            database.gameSessionDao().insertSession(
                com.example.data.local.GameSessionRecord(
                    username = username,
                    levelNumber = levelNumber,
                    missionName = "Mission $levelNumber",
                    score = score,
                    violationsSliced = violationsSliced,
                    trapsAvoided = trapsAvoided,
                    trapsSliced = trapsSliced,
                    accuracyPercent = accuracy,
                    timestamp = System.currentTimeMillis()
                )
            )
        } catch (_: Exception) { }

        // Immediately backup all stats and level progression to Firestore
        syncUserStatsToFirestore(updatedStats)

        updatedStats
    }

    suspend fun getRecentSessions(username: String, limit: Int = 20): List<com.example.data.local.GameSessionRecord> = withContext(Dispatchers.IO) {
        try {
            database.gameSessionDao().getRecentSessionsDirect(username)
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun getRecentSessionsFlow(username: String): Flow<List<com.example.data.local.GameSessionRecord>> {
        return database.gameSessionDao().getRecentSessions(username)
    }

    suspend fun syncUserStatsToFirestore(stats: UserStats) = withContext(Dispatchers.IO) {
        val cleanUser = stats.username.trim()
        if (cleanUser.isBlank()) return@withContext

        try {
            val statsMap = hashMapOf<String, Any>(
                "username" to cleanUser,
                "avatarId" to stats.avatarId.toLong(),
                "maxUnlockedLevel" to stats.calculateMaxUnlockedLevel().toLong(),
                "overallBestScore" to stats.overallBestScore.toLong(),
                "highestTierReached" to stats.highestTierReached.toLong(),
                "bestScoreLevel" to stats.bestScoreLevel.toLong(),
                "bestScoreDifficulty" to stats.bestScoreDifficulty,
                "gamesPlayed" to stats.gamesPlayed.toLong(),
                "totalViolationsSliced" to stats.totalViolationsSliced.toLong(),
                "totalTrapsAvoided" to stats.totalTrapsAvoided.toLong(),
                "totalTrapsSliced" to stats.totalTrapsSliced.toLong(),
                "bestComboStreak" to stats.bestComboStreak.toLong(),
                "briberySliced" to stats.briberySliced.toLong(),
                "fraudSliced" to stats.fraudSliced.toLong(),
                "moneyLaunderingSliced" to stats.moneyLaunderingSliced.toLong(),
                "dataBreachSliced" to stats.dataBreachSliced.toLong(),
                "systemicCorruptionSliced" to stats.systemicCorruptionSliced.toLong(),
                "otherViolationsSliced" to stats.otherViolationsSliced.toLong(),
                "level1Best" to stats.level1Best.toLong(),
                "level1Stars" to stats.level1Stars.toLong(),
                "level2Best" to stats.level2Best.toLong(),
                "level2Stars" to stats.level2Stars.toLong(),
                "level3Best" to stats.level3Best.toLong(),
                "level3Stars" to stats.level3Stars.toLong(),
                "level4Best" to stats.level4Best.toLong(),
                "level4Stars" to stats.level4Stars.toLong(),
                "updatedAt" to System.currentTimeMillis()
            )

            // 1. Sync to users collection in Firestore
            getFirestore().collection(COLLECTION_USERS)
                .document(cleanUser.lowercase())
                .set(statsMap, SetOptions.merge())

            // 2. Sync to leaderboard collection in Firestore
            val leaderboardMap = hashMapOf<String, Any>(
                "username" to cleanUser,
                "bestScore" to stats.overallBestScore.toLong(),
                "highestTierReached" to stats.highestTierReached.toLong(),
                "maxUnlockedLevel" to stats.calculateMaxUnlockedLevel().toLong(),
                "level1Best" to stats.level1Best.toLong(),
                "level2Best" to stats.level2Best.toLong(),
                "level3Best" to stats.level3Best.toLong(),
                "level4Best" to stats.level4Best.toLong(),
                "difficultyEffective" to "Auto",
                "bestScoreLevel" to stats.bestScoreLevel.toLong(),
                "difficulty" to stats.bestScoreDifficulty,
                "avatarId" to stats.avatarId.toLong(),
                "updatedAt" to System.currentTimeMillis()
            )
            getFirestore().collection(COLLECTION_LEADERBOARD)
                .document(cleanUser)
                .set(leaderboardMap, SetOptions.merge())
                .addOnSuccessListener {
                    Log.d(TAG, "Successfully synced user stats and levels to Firestore for $cleanUser")
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Failed to sync user stats to Firestore: ${e.message}")
                }
        } catch (e: Exception) {
            Log.w(TAG, "Firestore syncUserStats error: ${e.message}")
        }
    }

    suspend fun syncPersonalBestToFirestore(
        username: String,
        bestScore: Int,
        tierReached: Int = 0,
        level: Int = 1,
        difficulty: String = "Auto"
    ) = withContext(Dispatchers.IO) {
        if (username.isBlank() || bestScore <= 0) return@withContext
        try {
            val docData = hashMapOf(
                "username" to username,
                "bestScore" to bestScore.toLong(),
                "highestTierReached" to tierReached.toLong(),
                "difficultyEffective" to "Auto",
                "bestScoreLevel" to level.toLong(),
                "difficulty" to difficulty,
                "updatedAt" to System.currentTimeMillis()
            )
            getFirestore().collection(COLLECTION_LEADERBOARD)
                .document(username)
                .set(docData, SetOptions.merge())
                .addOnSuccessListener {
                    Log.d(TAG, "Successfully synced personal best to Firestore for $username")
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Failed to sync personal best to Firestore: ${e.message}")
                }
        } catch (e: Exception) {
            Log.w(TAG, "Firestore sync error: ${e.message}")
        }
    }
}
