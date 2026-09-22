package com.example.data

import android.content.Context
import android.util.Log
import com.example.ComplianceApplication
import com.example.data.local.AppDatabase
import com.example.data.local.UserAccount
import com.example.data.local.UserStats
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.security.MessageDigest
import kotlin.coroutines.resume

sealed class AuthResult {
    data class Success(val username: String) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

class AuthRepository(
    private val database: AppDatabase,
    private val sessionManager: SessionManager,
    private val context: Context? = null
) {
    companion object {
        private const val TAG = "AuthRepo"
        private const val COLLECTION_USERS = "users"
        private const val COLLECTION_LEADERBOARD = "leaderboard"
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

    /**
     * Hash password with SHA-256 for local demonstration storage.
     * Note: In production enterprise applications, use Credential Manager,
     * salted PBKDF2, bcrypt, or Firebase Auth.
     */
    fun hashPassword(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest(password.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    suspend fun isUsernameTaken(username: String): Boolean = withContext(Dispatchers.IO) {
        val cleanUser = username.trim()
        if (cleanUser.isBlank()) return@withContext false

        // 1. Check local Room DB
        val local = database.userAccountDao().getByUsername(cleanUser)
        if (local != null) return@withContext true

        // 2. Check Firebase Firestore
        try {
            val doc = withTimeoutOrNull(3000L) {
                suspendCancellableCoroutine<DocumentSnapshot?> { continuation ->
                    getFirestore().collection(COLLECTION_USERS).document(cleanUser.lowercase()).get()
                        .addOnSuccessListener { if (continuation.isActive) continuation.resume(it) }
                        .addOnFailureListener { ex ->
                            Log.w(TAG, "isUsernameTaken remote check failed: ${ex.message}")
                            if (continuation.isActive) continuation.resume(null)
                        }
                }
            }
            if (doc != null && doc.exists()) {
                return@withContext true
            }
        } catch (e: Exception) {
            Log.w(TAG, "Remote isUsernameTaken exception: ${e.message}")
        }
        false
    }

    suspend fun register(username: String, password: String): AuthResult = withContext(Dispatchers.IO) {
        val cleanUser = username.trim()
        if (cleanUser.isBlank() || password.isBlank()) {
            return@withContext AuthResult.Error("Username and password cannot be empty")
        }
        if (cleanUser.length < 3 || cleanUser.length > 10) {
            return@withContext AuthResult.Error("Username must be 3-10 characters")
        }
        if (!cleanUser.all { it.isLetterOrDigit() }) {
            return@withContext AuthResult.Error("Username can only contain letters and numbers")
        }

        // 1. Check local Room DB
        val existingLocal = database.userAccountDao().getByUsername(cleanUser)
        if (existingLocal != null) {
            return@withContext AuthResult.Error("Username already taken")
        }

        // 2. Check remote Firestore
        try {
            val remoteDoc = withTimeoutOrNull(4000L) {
                suspendCancellableCoroutine<DocumentSnapshot?> { continuation ->
                    getFirestore().collection(COLLECTION_USERS).document(cleanUser.lowercase()).get()
                        .addOnSuccessListener { if (continuation.isActive) continuation.resume(it) }
                        .addOnFailureListener { if (continuation.isActive) continuation.resume(null) }
                }
            }
            if (remoteDoc != null && remoteDoc.exists()) {
                return@withContext AuthResult.Error("Username already taken")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Remote check during register exception: ${e.message}")
        }

        val hash = hashPassword(password)
        val randomAvatarId = (1..10).random()
        val newAccount = UserAccount(username = cleanUser, passwordHash = hash, avatarId = randomAvatarId)

        // 3. Save to Firebase Firestore: store username, password, passwordHash, avatarId, and level stats
        try {
            val userData = hashMapOf<String, Any>(
                "username" to cleanUser,
                "password" to password,
                "passwordHash" to hash,
                "avatarId" to randomAvatarId,
                "maxUnlockedLevel" to 1L,
                "level1Best" to 0L,
                "level1Stars" to 0L,
                "level2Best" to 0L,
                "level2Stars" to 0L,
                "level3Best" to 0L,
                "level3Stars" to 0L,
                "level4Best" to 0L,
                "level4Stars" to 0L,
                "overallBestScore" to 0L,
                "highestTierReached" to 0L,
                "createdAt" to System.currentTimeMillis(),
                "updatedAt" to System.currentTimeMillis()
            )
            withTimeoutOrNull(4000L) {
                suspendCancellableCoroutine<Boolean> { continuation ->
                    getFirestore().collection(COLLECTION_USERS).document(cleanUser.lowercase())
                        .set(userData, SetOptions.merge())
                        .addOnSuccessListener { if (continuation.isActive) continuation.resume(true) }
                        .addOnFailureListener { ex ->
                            Log.e(TAG, "Failed to save user to Firestore: ${ex.message}", ex)
                            if (continuation.isActive) continuation.resume(false)
                        }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Firestore register write exception: ${e.message}", e)
        }

        // 4. Save to local Room DB
        try {
            database.userAccountDao().insert(newAccount)
            val initialStats = UserStats(
                username = cleanUser,
                avatarId = randomAvatarId,
                maxUnlockedLevel = 1
            )
            database.userStatsDao().insertOrUpdate(initialStats)
            sessionManager.saveSession(cleanUser)
            AuthResult.Success(cleanUser)
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Failed to create account")
        }
    }

    suspend fun login(username: String, password: String): AuthResult = withContext(Dispatchers.IO) {
        val cleanUser = username.trim()
        if (cleanUser.isBlank() || password.isBlank()) {
            return@withContext AuthResult.Error("Username and password cannot be empty")
        }

        val hash = hashPassword(password)
        val localAccount = database.userAccountDao().getByUsername(cleanUser)

        // 1. If local account exists and matches, fast-path login with background Firestore backup sync
        if (localAccount != null && localAccount.passwordHash == hash) {
            val stats = database.userStatsDao().getStatsDirect(localAccount.username)
            val fallbackAvatarId = if (localAccount.avatarId in 1..10) localAccount.avatarId else ((kotlin.math.abs(localAccount.username.hashCode()) % 10) + 1)
            val currentStats = if (stats == null) {
                UserStats(username = localAccount.username, avatarId = fallbackAvatarId)
            } else if (stats.avatarId !in 1..10) {
                stats.copy(avatarId = fallbackAvatarId)
            } else {
                stats
            }
            database.userStatsDao().insertOrUpdate(currentStats)
            sessionManager.saveSession(localAccount.username)

            // Ensure credentials and level progress are backed up in Firebase Firestore
            try {
                val syncData = hashMapOf<String, Any>(
                    "username" to localAccount.username,
                    "password" to password,
                    "passwordHash" to hash,
                    "avatarId" to fallbackAvatarId,
                    "maxUnlockedLevel" to currentStats.calculateMaxUnlockedLevel().toLong(),
                    "level1Best" to currentStats.level1Best.toLong(),
                    "level1Stars" to currentStats.level1Stars.toLong(),
                    "level2Best" to currentStats.level2Best.toLong(),
                    "level2Stars" to currentStats.level2Stars.toLong(),
                    "level3Best" to currentStats.level3Best.toLong(),
                    "level3Stars" to currentStats.level3Stars.toLong(),
                    "level4Best" to currentStats.level4Best.toLong(),
                    "level4Stars" to currentStats.level4Stars.toLong(),
                    "overallBestScore" to currentStats.overallBestScore.toLong(),
                    "highestTierReached" to currentStats.highestTierReached.toLong(),
                    "updatedAt" to System.currentTimeMillis()
                )
                getFirestore().collection(COLLECTION_USERS).document(localAccount.username.lowercase())
                    .set(syncData, SetOptions.merge())
            } catch (e: Exception) {
                Log.w(TAG, "Background sync to Firestore on login failed: ${e.message}")
            }

            return@withContext AuthResult.Success(localAccount.username)
        }

        // 2. Local account not found (e.g. user uninstalled & reinstalled the app!) OR local password mismatch:
        // Query Firebase Firestore!
        var remoteDoc: DocumentSnapshot? = null
        try {
            remoteDoc = withTimeoutOrNull(5000L) {
                suspendCancellableCoroutine { continuation ->
                    getFirestore().collection(COLLECTION_USERS).document(cleanUser.lowercase()).get()
                        .addOnSuccessListener { if (continuation.isActive) continuation.resume(it) }
                        .addOnFailureListener { ex ->
                            Log.w(TAG, "Firestore user lookup on login failed: ${ex.message}")
                            if (continuation.isActive) continuation.resume(null)
                        }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Firestore login query exception: ${e.message}")
        }

        if (remoteDoc != null && remoteDoc.exists()) {
            val remoteUser = remoteDoc.getString("username") ?: cleanUser
            val remotePassword = remoteDoc.getString("password")
            val remoteHash = remoteDoc.getString("passwordHash")
            val remoteAvatar = (remoteDoc.getLong("avatarId") ?: 1L).toInt()

            val isPasswordValid = (remotePassword != null && remotePassword == password) ||
                    (remoteHash != null && remoteHash == hash)

            if (isPasswordValid) {
                val effectiveAvatar = if (remoteAvatar in 1..10) remoteAvatar else 1
                val accountToSave = UserAccount(
                    username = remoteUser,
                    passwordHash = remoteHash ?: hash,
                    avatarId = effectiveAvatar
                )

                // Restore user account in local Room DB
                try {
                    if (localAccount == null) {
                        database.userAccountDao().insert(accountToSave)
                    } else {
                        database.userAccountDao().updatePassword(remoteUser, remoteHash ?: hash)
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Failed inserting restored account to Room: ${e.message}")
                }

                // Restore UserStats from Firestore: levels, scores, stars, and max unlocked level
                val remoteLvl1Best = (remoteDoc.getLong("level1Best") ?: 0L).toInt()
                val remoteLvl1Stars = (remoteDoc.getLong("level1Stars") ?: 0L).toInt()
                val remoteLvl2Best = (remoteDoc.getLong("level2Best") ?: 0L).toInt()
                val remoteLvl2Stars = (remoteDoc.getLong("level2Stars") ?: 0L).toInt()
                val remoteLvl3Best = (remoteDoc.getLong("level3Best") ?: 0L).toInt()
                val remoteLvl3Stars = (remoteDoc.getLong("level3Stars") ?: 0L).toInt()
                val remoteLvl4Best = (remoteDoc.getLong("level4Best") ?: 0L).toInt()
                val remoteLvl4Stars = (remoteDoc.getLong("level4Stars") ?: 0L).toInt()
                val remoteOverallBest = (remoteDoc.getLong("overallBestScore") ?: (remoteDoc.getLong("bestScore") ?: 0L)).toInt()
                val remoteHighestTier = (remoteDoc.getLong("highestTierReached") ?: 0L).toInt()
                val remoteMaxUnlocked = (remoteDoc.getLong("maxUnlockedLevel") ?: 1L).toInt()

                var effectiveMaxUnlocked = maxOf(1, remoteMaxUnlocked)
                if (remoteLvl1Best >= 1200) effectiveMaxUnlocked = maxOf(effectiveMaxUnlocked, 2)
                if (remoteLvl1Best >= 1200 && remoteLvl2Best >= 2500) effectiveMaxUnlocked = maxOf(effectiveMaxUnlocked, 3)
                if (remoteLvl1Best >= 1200 && remoteLvl2Best >= 2500 && remoteLvl3Best >= 5000) effectiveMaxUnlocked = maxOf(effectiveMaxUnlocked, 4)

                // Also check leaderboard document in case the user played earlier builds
                var lbBestScore = 0
                var lbHighestTier = 0
                var lbMaxUnlocked = 1
                try {
                    val lbDoc = withTimeoutOrNull(2000L) {
                        suspendCancellableCoroutine<DocumentSnapshot?> { cont ->
                            getFirestore().collection(COLLECTION_LEADERBOARD).document(remoteUser).get()
                                .addOnSuccessListener { if (cont.isActive) cont.resume(it) }
                                .addOnFailureListener { if (cont.isActive) cont.resume(null) }
                        }
                    }
                    if (lbDoc != null && lbDoc.exists()) {
                        lbBestScore = (lbDoc.getLong("bestScore") ?: 0L).toInt()
                        lbHighestTier = (lbDoc.getLong("highestTierReached") ?: 0L).toInt()
                        lbMaxUnlocked = (lbDoc.getLong("maxUnlockedLevel") ?: 1L).toInt()
                    }
                } catch (_: Exception) {}

                effectiveMaxUnlocked = maxOf(effectiveMaxUnlocked, lbMaxUnlocked)
                val finalOverallBest = maxOf(remoteOverallBest, lbBestScore)
                val finalHighestTier = maxOf(remoteHighestTier, lbHighestTier)

                val localExistingStats = database.userStatsDao().getStatsDirect(remoteUser)
                val mergedLvl1Best = maxOf(localExistingStats?.level1Best ?: 0, remoteLvl1Best)
                val mergedLvl2Best = maxOf(localExistingStats?.level2Best ?: 0, remoteLvl2Best)
                val mergedLvl3Best = maxOf(localExistingStats?.level3Best ?: 0, remoteLvl3Best)
                val mergedLvl4Best = maxOf(localExistingStats?.level4Best ?: 0, remoteLvl4Best)

                if (mergedLvl1Best >= 1200) effectiveMaxUnlocked = maxOf(effectiveMaxUnlocked, 2)
                if (mergedLvl1Best >= 1200 && mergedLvl2Best >= 2500) effectiveMaxUnlocked = maxOf(effectiveMaxUnlocked, 3)
                if (mergedLvl1Best >= 1200 && mergedLvl2Best >= 2500 && mergedLvl3Best >= 5000) effectiveMaxUnlocked = maxOf(effectiveMaxUnlocked, 4)

                val restoredStats = (localExistingStats ?: UserStats(username = remoteUser)).copy(
                    username = remoteUser,
                    avatarId = effectiveAvatar,
                    overallBestScore = maxOf(localExistingStats?.overallBestScore ?: 0, finalOverallBest),
                    highestTierReached = maxOf(localExistingStats?.highestTierReached ?: 0, finalHighestTier),
                    bestScoreLevel = (remoteDoc.getLong("bestScoreLevel") ?: 1L).toInt(),
                    bestScoreDifficulty = remoteDoc.getString("bestScoreDifficulty") ?: "Auto",
                    gamesPlayed = maxOf(localExistingStats?.gamesPlayed ?: 0, (remoteDoc.getLong("gamesPlayed") ?: 0L).toInt()),
                    totalViolationsSliced = maxOf(localExistingStats?.totalViolationsSliced ?: 0, (remoteDoc.getLong("totalViolationsSliced") ?: 0L).toInt()),
                    totalTrapsAvoided = maxOf(localExistingStats?.totalTrapsAvoided ?: 0, (remoteDoc.getLong("totalTrapsAvoided") ?: 0L).toInt()),
                    totalTrapsSliced = maxOf(localExistingStats?.totalTrapsSliced ?: 0, (remoteDoc.getLong("totalTrapsSliced") ?: 0L).toInt()),
                    bestComboStreak = maxOf(localExistingStats?.bestComboStreak ?: 0, (remoteDoc.getLong("bestComboStreak") ?: 0L).toInt()),
                    briberySliced = maxOf(localExistingStats?.briberySliced ?: 0, (remoteDoc.getLong("briberySliced") ?: 0L).toInt()),
                    fraudSliced = maxOf(localExistingStats?.fraudSliced ?: 0, (remoteDoc.getLong("fraudSliced") ?: 0L).toInt()),
                    moneyLaunderingSliced = maxOf(localExistingStats?.moneyLaunderingSliced ?: 0, (remoteDoc.getLong("moneyLaunderingSliced") ?: 0L).toInt()),
                    dataBreachSliced = maxOf(localExistingStats?.dataBreachSliced ?: 0, (remoteDoc.getLong("dataBreachSliced") ?: 0L).toInt()),
                    systemicCorruptionSliced = maxOf(localExistingStats?.systemicCorruptionSliced ?: 0, (remoteDoc.getLong("systemicCorruptionSliced") ?: 0L).toInt()),
                    otherViolationsSliced = maxOf(localExistingStats?.otherViolationsSliced ?: 0, (remoteDoc.getLong("otherViolationsSliced") ?: 0L).toInt()),
                    level1Best = mergedLvl1Best,
                    level1Stars = maxOf(localExistingStats?.level1Stars ?: 0, remoteLvl1Stars),
                    level2Best = mergedLvl2Best,
                    level2Stars = maxOf(localExistingStats?.level2Stars ?: 0, remoteLvl2Stars),
                    level3Best = mergedLvl3Best,
                    level3Stars = maxOf(localExistingStats?.level3Stars ?: 0, remoteLvl3Stars),
                    level4Best = mergedLvl4Best,
                    level4Stars = maxOf(localExistingStats?.level4Stars ?: 0, remoteLvl4Stars),
                    maxUnlockedLevel = maxOf(localExistingStats?.maxUnlockedLevel ?: 1, effectiveMaxUnlocked),
                    updatedAt = System.currentTimeMillis()
                )

                database.userStatsDao().insertOrUpdate(restoredStats)

                // If remote document didn't have password field yet, write it now
                if (remotePassword == null) {
                    try {
                        getFirestore().collection(COLLECTION_USERS).document(remoteUser.lowercase())
                            .set(mapOf("password" to password, "passwordHash" to hash, "updatedAt" to System.currentTimeMillis()), SetOptions.merge())
                    } catch (e: Exception) { /* ignore */ }
                }

                sessionManager.saveSession(remoteUser)
                return@withContext AuthResult.Success(remoteUser)
            } else {
                return@withContext AuthResult.Error("Incorrect username or password")
            }
        }

        // 3. Fallback: if remote was unreachable (remoteDoc == null) and localAccount is null
        if (remoteDoc == null && localAccount == null) {
            return@withContext AuthResult.Error("Server Offline: Tidak dapat terhubung ke Firebase untuk memeriksa akun.")
        }

        AuthResult.Error("Incorrect username or password")
    }

    suspend fun resetPassword(username: String, newPass: String): AuthResult = withContext(Dispatchers.IO) {
        val cleanUser = username.trim()
        if (cleanUser.isBlank() || newPass.isBlank()) {
            return@withContext AuthResult.Error("Username and new password cannot be empty")
        }
        if (newPass.length < 4) {
            return@withContext AuthResult.Error("Password must be at least 4 characters")
        }

        val newHash = hashPassword(newPass)
        var userFound = false
        var targetUsername = cleanUser

        // Check local Room DB
        val localAccount = database.userAccountDao().getByUsername(cleanUser)
        if (localAccount != null) {
            database.userAccountDao().updatePassword(localAccount.username, newHash)
            userFound = true
            targetUsername = localAccount.username
        }

        // Check & update in Firebase Firestore
        try {
            val doc = withTimeoutOrNull(4000L) {
                suspendCancellableCoroutine<DocumentSnapshot?> { continuation ->
                    getFirestore().collection(COLLECTION_USERS).document(cleanUser.lowercase()).get()
                        .addOnSuccessListener { if (continuation.isActive) continuation.resume(it) }
                        .addOnFailureListener { if (continuation.isActive) continuation.resume(null) }
                }
            }
            if (doc != null && doc.exists()) {
                val remoteUser = doc.getString("username") ?: cleanUser
                val remoteAvatar = (doc.getLong("avatarId") ?: 1L).toInt()
                targetUsername = remoteUser
                userFound = true

                // Update in Firestore
                val updateData = hashMapOf<String, Any>(
                    "password" to newPass,
                    "passwordHash" to newHash,
                    "updatedAt" to System.currentTimeMillis()
                )
                getFirestore().collection(COLLECTION_USERS).document(cleanUser.lowercase())
                    .set(updateData, SetOptions.merge())

                // Also restore/update in local Room DB if not present
                if (localAccount == null) {
                    database.userAccountDao().insert(
                        UserAccount(
                            username = remoteUser,
                            passwordHash = newHash,
                            avatarId = remoteAvatar
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "resetPassword Firestore update failed: ${e.message}")
        }

        if (!userFound) {
            return@withContext AuthResult.Error("User '$cleanUser' not found")
        }

        AuthResult.Success(targetUsername)
    }

    suspend fun logout() {
        sessionManager.clearSession()
    }
}
