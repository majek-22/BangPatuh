package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.R
import com.example.audio.MusicManager
import com.example.audio.SoundManager
import com.example.data.AuthRepository
import com.example.data.AuthResult
import com.example.data.ComplianceCategory
import com.example.data.GameDifficulty
import com.example.data.GameItem
import com.example.data.LeaderboardItem
import com.example.data.LeaderboardRepository
import com.example.data.LevelConfig
import com.example.data.LocaleManager
import com.example.data.SessionManager
import com.example.data.SlicedCategoryRecord
import com.example.data.local.AppDatabase
import com.example.data.local.GameSessionRecord
import com.example.data.local.UserStats
import com.example.engine.GameEngine
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class GamePhase {
    SPLASH,
    LOGIN_REGISTER,
    MENU,
    LEVEL_SELECT,
    GLOSSARY,
    LEADERBOARD,
    PROFILE,
    PLAYING,
    RESULT
}

data class FruitNinjaComboBurst(
    val id: Long,
    val count: Int,
    val x: Float,
    val y: Float
)

data class GameUiState(
    val phase: GamePhase = GamePhase.SPLASH,
    val currentUser: String? = null,
    val userAvatarId: Int = 1,
    val currentLanguage: String = "en",
    val isAudioMuted: Boolean = false,
    val selectedLevel: LevelConfig = LevelConfig.ALL_LEVELS.first(),
    val selectedDifficulty: GameDifficulty = GameDifficulty.NORMAL,
    val score: Int = 0,
    val highScore: Int = 0,
    val isNewHighScore: Boolean = false,
    val lives: Int = 3,
    val baseLives: Int = 3,
    val shieldBonusLives: Int = 0,
    val comboMultiplier: Int = 1,
    val comboStreak: Int = 0,
    val timeRemaining: Float = 60f,
    val isPaused: Boolean = false,
    val feedbackMessage: String? = null,
    val feedbackIsPositive: Boolean = true,
    val screenShakeIntensity: Float = 0f,
    val flashOverlayColor: Long? = null,
    val slowMoFactor: Float = 1.0f,
    val slicedCategoriesSummary: List<SlicedCategoryRecord> = emptyList(),
    val trapsAvoided: Int = 0,
    val trapsSliced: Int = 0,
    val starsEarned: Int = 0,
    val userStats: UserStats? = null,
    val recentSessions: List<GameSessionRecord> = emptyList(),
    val leaderboardEntries: List<LeaderboardItem> = emptyList(),
    val isLeaderboardOffline: Boolean = false,
    val leaderboardErrorMessage: String? = null,
    val isLeaderboardLoading: Boolean = false,
    val authErrorMessage: String? = null,
    val debugOverlayEnabled: Boolean = false,
    val readyCountdown: Float = 0f,
    val isGameOverBannerShowing: Boolean = false,
    val isFreezeActive: Boolean = false,
    val freezeTimeRemaining: Float = 0f,
    val freezeBonusHits: Int = 0,
    val activeComboBurst: FruitNinjaComboBurst? = null,
    val shouldShowTrailer: Boolean = false,
    val shouldShowComic: Boolean = false
)

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getInstance(application)
    val sessionManager = SessionManager(application)
    val authRepository = AuthRepository(database, sessionManager, application)
    val leaderboardRepository = LeaderboardRepository(database, application)

    val soundManager = SoundManager(application)
    val musicManager = MusicManager(application)
    val engine = GameEngine()

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private var slowMoTimer: Float = 0f
    private var flashTimer: Float = 0f
    private var shakeTimer: Float = 0f
    private var feedbackToastTimer: Float = 0f
    private var sessionCheckJob: Job? = null
    private var targetPhaseAfterSplash: GamePhase = GamePhase.LOGIN_REGISTER
    private var isMusicSpedUp: Boolean = false

    init {
        setupEngineCallbacks()
        checkInitialSession()
        observePreferences()
    }

    private fun checkInitialSession() {
        sessionCheckJob = viewModelScope.launch {
            val validUsername = sessionManager.getInitialValidSession()
            if (!validUsername.isNullOrBlank()) {
                val stats = leaderboardRepository.getUserStats(validUsername)
                val sessions = leaderboardRepository.getRecentSessions(validUsername, 10)
                _uiState.value = _uiState.value.copy(
                    currentUser = validUsername,
                    userStats = stats,
                    userAvatarId = stats?.avatarId ?: 1,
                    recentSessions = sessions,
                    highScore = stats?.overallBestScore ?: 0,
                    shouldShowComic = true,
                    authErrorMessage = null
                )
                targetPhaseAfterSplash = GamePhase.MENU
            } else {
                _uiState.value = _uiState.value.copy(
                    currentUser = null,
                    userStats = null,
                    highScore = 0,
                    shouldShowComic = false,
                    authErrorMessage = null
                )
                targetPhaseAfterSplash = GamePhase.LOGIN_REGISTER
            }
        }
    }

    fun onSplashFinished() {
        if (_uiState.value.phase == GamePhase.SPLASH) {
            viewModelScope.launch {
                sessionCheckJob?.join()
                _uiState.value = _uiState.value.copy(
                    phase = targetPhaseAfterSplash,
                    shouldShowComic = if (targetPhaseAfterSplash == GamePhase.MENU) true else _uiState.value.shouldShowComic
                )
                if (targetPhaseAfterSplash == GamePhase.MENU) {
                    musicManager.playMenuTheme()
                }
            }
        }
    }

    private fun observePreferences() {
        viewModelScope.launch {
            sessionManager.isAudioMuted.collect { muted ->
                soundManager.isMuted = muted
                musicManager.setMuted(muted)
                _uiState.value = _uiState.value.copy(isAudioMuted = muted)
            }
        }
        viewModelScope.launch {
            sessionManager.appLanguage.collect { lang ->
                LocaleManager.setLocale(lang)
                engine.currentLanguage = lang
                _uiState.value = _uiState.value.copy(currentLanguage = lang)
            }
        }
    }

    private fun setupEngineCallbacks() {
        engine.onViolationSliced = { item, points, multiplier ->
            val justHit4x = multiplier >= 4 && _uiState.value.comboMultiplier < 4
            if (justHit4x) {
                soundManager.playSfx("combo-4x")
            }
            soundManager.playSfx("slice-hit")

            slowMoTimer = 0.10f
            flashTimer = 0.08f

            val newBurst = if (justHit4x && _uiState.value.activeComboBurst == null) {
                FruitNinjaComboBurst(
                    id = System.currentTimeMillis(),
                    count = 4,
                    x = item.x,
                    y = item.y
                )
            } else {
                _uiState.value.activeComboBurst
            }

            _uiState.value = _uiState.value.copy(
                score = engine.score,
                comboMultiplier = multiplier,
                comboStreak = engine.comboStreak,
                slowMoFactor = 0.30f,
                flashOverlayColor = 0x55FFFFFF,
                activeComboBurst = newBurst
            )
        }

        engine.onTrapSliced = { item ->
            soundManager.playSfx("trap-hit")
            if (isMusicSpedUp) {
                isMusicSpedUp = false
                musicManager.setPlaybackSpeed(1.0f)
            }
            shakeTimer = 0.25f
            flashTimer = 0.16f
            feedbackToastTimer = 2.0f

            val context = getApplication<Application>()
            val msg = context.getString(R.string.alert_trap_sliced)

            _uiState.value = _uiState.value.copy(
                score = engine.score,
                comboMultiplier = 1,
                comboStreak = 0,
                trapsSliced = engine.trapsSlicedCount,
                screenShakeIntensity = 12f,
                flashOverlayColor = 0x66FF7043,
                feedbackMessage = msg,
                feedbackIsPositive = false
            )
        }

        engine.onTrapAvoided = { _ ->
            _uiState.value = _uiState.value.copy(
                trapsAvoided = engine.trapsAvoidedCount
            )
        }

        engine.onWrongSlice = { item ->
            soundManager.playSfx("wrong-slice")
            if (isMusicSpedUp) {
                isMusicSpedUp = false
                musicManager.setPlaybackSpeed(1.0f)
            }
            shakeTimer = 0.35f
            flashTimer = 0.22f
            feedbackToastTimer = 2.5f

            val context = getApplication<Application>()
            val itemName = context.getString(item.category.displayNameRes)
            val msg = context.getString(R.string.alert_wrong_slice_msg, itemName)

            _uiState.value = _uiState.value.copy(
                lives = engine.lives,
                baseLives = engine.baseLives,
                shieldBonusLives = engine.shieldBonusLives,
                comboMultiplier = 1,
                comboStreak = 0,
                screenShakeIntensity = 18f,
                flashOverlayColor = 0x77E14B5A,
                feedbackMessage = msg,
                feedbackIsPositive = false
            )
        }

        engine.onViolationMissed = { item ->
            soundManager.playSfx("wrong-slice")
            if (isMusicSpedUp) {
                isMusicSpedUp = false
                musicManager.setPlaybackSpeed(1.0f)
            }
            shakeTimer = 0.25f
            flashTimer = 0.18f
            feedbackToastTimer = 2.0f

            val context = getApplication<Application>()
            val msg = context.getString(R.string.alert_missed_violation)

            _uiState.value = _uiState.value.copy(
                lives = engine.lives,
                baseLives = engine.baseLives,
                shieldBonusLives = engine.shieldBonusLives,
                comboMultiplier = 1,
                comboStreak = 0,
                screenShakeIntensity = 14f,
                flashOverlayColor = 0x66E14B5A,
                feedbackMessage = msg,
                feedbackIsPositive = false
            )
        }

        engine.onShieldSliced = { _ ->
            soundManager.playSfx("shield-bonus")
            flashTimer = 0.18f
            feedbackToastTimer = 2.0f

            val context = getApplication<Application>()
            val msg = context.getString(R.string.alert_shield_gained)

            _uiState.value = _uiState.value.copy(
                score = engine.score,
                lives = engine.lives,
                baseLives = engine.baseLives,
                shieldBonusLives = engine.shieldBonusLives,
                flashOverlayColor = if (engine.shieldBonusLives > 0) 0x5500E5FF else 0x55FF3B30,
                feedbackMessage = msg,
                feedbackIsPositive = true
            )
        }

        engine.onFreezeStarted = {
            soundManager.playSfx("freeze-start")
            flashTimer = 0.35f
            _uiState.value = _uiState.value.copy(
                isFreezeActive = true,
                freezeTimeRemaining = 5.0f,
                freezeBonusHits = 0,
                flashOverlayColor = 0x5500E5FF
            )
        }

        engine.onFreezeBonusSliced = { _, totalSlices, _ ->
            soundManager.playSfx("coin-bonus")
            shakeTimer = 0.08f
            _uiState.value = _uiState.value.copy(
                score = engine.score,
                freezeBonusHits = totalSlices
            )
        }

        engine.onFreezeEnded = { totalSlices, totalBonusPoints ->
            soundManager.playSfx("shield-bonus")
            flashTimer = 0.25f
            feedbackToastTimer = 3.5f

            val context = getApplication<Application>()
            val msg = context.getString(R.string.freeze_bonus_complete, totalBonusPoints, totalSlices)

            _uiState.value = _uiState.value.copy(
                isFreezeActive = false,
                freezeTimeRemaining = 0f,
                score = engine.score,
                flashOverlayColor = 0x44FFD700,
                feedbackMessage = msg,
                feedbackIsPositive = true
            )
        }

        engine.onComboTriggered = { count, hitX, hitY ->
            if (count >= 4) {
                soundManager.playSfx("combo-4x")
                _uiState.value = _uiState.value.copy(
                    activeComboBurst = FruitNinjaComboBurst(
                        id = System.currentTimeMillis(),
                        count = count,
                        x = hitX,
                        y = hitY
                    )
                )
            }
        }

        engine.onGameOver = { finalScore ->
            if (isMusicSpedUp) {
                isMusicSpedUp = false
                musicManager.setPlaybackSpeed(1.0f)
            }
            soundManager.playSfx("game-over")
            handleGameOver(finalScore)
        }
    }

    fun dismissComboBurst(id: Long) {
        if (_uiState.value.activeComboBurst?.id == id) {
            _uiState.value = _uiState.value.copy(activeComboBurst = null)
        }
    }

    // =========================================================================
    // AUTHENTICATION & SESSION
    // =========================================================================

    fun login(username: String, pass: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(authErrorMessage = null)
            when (val result = authRepository.login(username, pass)) {
                is AuthResult.Success -> {
                    val (stats, allTimeBest) = leaderboardRepository.fetchAndSyncUserStats(result.username)
                    val sessions = leaderboardRepository.getRecentSessions(result.username, 10)
                    delay(350L) // Allow keyboard dismissal animation to complete cleanly
                    _uiState.value = _uiState.value.copy(
                        currentUser = result.username,
                        userStats = stats,
                        userAvatarId = stats.avatarId,
                        recentSessions = sessions,
                        highScore = allTimeBest,
                        phase = GamePhase.MENU,
                        shouldShowComic = true,
                        authErrorMessage = null
                    )
                    musicManager.playMenuTheme()
                }
                is AuthResult.Error -> {
                    _uiState.value = _uiState.value.copy(authErrorMessage = result.message)
                }
            }
        }
    }

    fun register(username: String, pass: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(authErrorMessage = null)
            when (val result = authRepository.register(username, pass)) {
                is AuthResult.Success -> {
                    val (stats, allTimeBest) = leaderboardRepository.fetchAndSyncUserStats(result.username)
                    val sessions = leaderboardRepository.getRecentSessions(result.username, 10)
                    delay(350L) // Allow keyboard dismissal animation to complete cleanly
                    _uiState.value = _uiState.value.copy(
                        currentUser = result.username,
                        userStats = stats,
                        userAvatarId = stats.avatarId,
                        recentSessions = sessions,
                        highScore = allTimeBest,
                        phase = GamePhase.MENU,
                        shouldShowComic = true,
                        authErrorMessage = null
                    )
                    musicManager.playMenuTheme()
                }
                is AuthResult.Error -> {
                    _uiState.value = _uiState.value.copy(authErrorMessage = result.message)
                }
            }
        }
    }

    suspend fun isUsernameTaken(username: String): Boolean {
        return authRepository.isUsernameTaken(username)
    }

    suspend fun resetPassword(username: String, newPass: String): AuthResult {
        return authRepository.resetPassword(username, newPass)
    }

    fun onTrailerDismissed() {
        _uiState.value = _uiState.value.copy(shouldShowTrailer = false)
    }

    fun onComicDismissed() {
        _uiState.value = _uiState.value.copy(shouldShowComic = false)
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            musicManager.stop()
            _uiState.value = _uiState.value.copy(
                currentUser = null,
                userStats = null,
                phase = GamePhase.LOGIN_REGISTER,
                shouldShowComic = false,
                authErrorMessage = null
            )
        }
    }

    // =========================================================================
    // LANGUAGE & AUDIO CONTROLS
    // =========================================================================

    fun setLanguage(lang: String, onLanguageChanged: (() -> Unit)? = null) {
        viewModelScope.launch {
            sessionManager.setLanguage(lang)
            LocaleManager.setLocale(lang)
            engine.currentLanguage = lang
            _uiState.value = _uiState.value.copy(currentLanguage = lang)
            onLanguageChanged?.invoke()
        }
    }

    fun toggleLanguage(onLanguageChanged: (() -> Unit)? = null) {
        val current = _uiState.value.currentLanguage
        val next = when (current) {
            "in", "id" -> "en"
            "en" -> "ja"
            else -> "in"
        }
        setLanguage(next, onLanguageChanged)
    }

    fun toggleAudioMute() {
        val newMuted = !_uiState.value.isAudioMuted
        viewModelScope.launch {
            sessionManager.setAudioMuted(newMuted)
            soundManager.isMuted = newMuted
            musicManager.setMuted(newMuted)
            _uiState.value = _uiState.value.copy(isAudioMuted = newMuted)
        }
    }

    fun toggleDebugOverlay() {
        val next = !_uiState.value.debugOverlayEnabled
        engine.showDebugOverlay = next
        _uiState.value = _uiState.value.copy(debugOverlayEnabled = next)
    }

    // =========================================================================
    // NAVIGATION
    // =========================================================================

    private var previousPhaseBeforeGlossary: GamePhase = GamePhase.MENU

    fun openGlossary() {
        previousPhaseBeforeGlossary = _uiState.value.phase
        navigateTo(GamePhase.GLOSSARY)
    }

    fun closeGlossary() {
        navigateTo(previousPhaseBeforeGlossary)
    }

    fun navigateTo(phase: GamePhase) {
        if (phase == GamePhase.GLOSSARY && _uiState.value.phase != GamePhase.GLOSSARY) {
            previousPhaseBeforeGlossary = _uiState.value.phase
        }
        _uiState.value = _uiState.value.copy(phase = phase)
        when (phase) {
            GamePhase.PLAYING -> musicManager.playGameplayTheme()
            GamePhase.SPLASH, GamePhase.MENU, GamePhase.LEVEL_SELECT, GamePhase.GLOSSARY, GamePhase.LEADERBOARD, GamePhase.PROFILE, GamePhase.RESULT -> {
                musicManager.playMenuTheme()
            }
            GamePhase.LOGIN_REGISTER -> musicManager.stop()
        }

        if (phase == GamePhase.LEADERBOARD || phase == GamePhase.PROFILE) {
            refreshLeaderboard()
        }
    }

    fun selectDifficulty(difficulty: GameDifficulty) {
        _uiState.value = _uiState.value.copy(selectedDifficulty = difficulty)
    }

    // =========================================================================
    // GAMEPLAY
    // =========================================================================

    fun startMission(level: LevelConfig, difficulty: GameDifficulty = GameDifficulty.NORMAL) {
        _uiState.value = _uiState.value.copy(
            selectedLevel = level,
            selectedDifficulty = difficulty
        )
        engine.resetGame(level, difficulty)
        slowMoTimer = 0f
        flashTimer = 0f
        shakeTimer = 0f
        feedbackToastTimer = 0f
        isMusicSpedUp = false
        musicManager.setPlaybackSpeed(1.0f)

        _uiState.value = _uiState.value.copy(
            phase = GamePhase.PLAYING,
            score = 0,
            lives = engine.lives,
            baseLives = engine.baseLives,
            shieldBonusLives = engine.shieldBonusLives,
            comboMultiplier = 1,
            comboStreak = 0,
            timeRemaining = engine.timeRemainingSeconds,
            trapsAvoided = 0,
            trapsSliced = 0,
            starsEarned = 0,
            isPaused = false,
            feedbackMessage = null,
            screenShakeIntensity = 0f,
            flashOverlayColor = null,
            slowMoFactor = 1.0f,
            isNewHighScore = false,
            readyCountdown = 3.0f,
            isGameOverBannerShowing = false
        )
        musicManager.playGameplayTheme()
    }

    fun pauseGame() {
        if (_uiState.value.phase == GamePhase.PLAYING) {
            _uiState.value = _uiState.value.copy(isPaused = true)
            musicManager.pause()
        }
    }

    fun resumeGame() {
        if (_uiState.value.phase == GamePhase.PLAYING) {
            _uiState.value = _uiState.value.copy(isPaused = false)
            musicManager.resume()
        }
    }

    fun returnToMenu() {
        engine.resetGame()
        isMusicSpedUp = false
        musicManager.setPlaybackSpeed(1.0f)
        _uiState.value = _uiState.value.copy(
            phase = GamePhase.MENU,
            isPaused = false,
            readyCountdown = 0f,
            isGameOverBannerShowing = false,
            feedbackMessage = null
        )
        musicManager.playMenuTheme()
    }

    fun setScreenDimensions(width: Float, height: Float) {
        engine.updateScreenDimensions(width, height)
    }

    suspend fun checkUsernameTaken(username: String): Boolean {
        return authRepository.isUsernameTaken(username)
    }

    fun onSliceStart() {
        if (_uiState.value.readyCountdown > 0f || _uiState.value.isGameOverBannerShowing) return
        engine.startStroke()
    }

    fun onSliceEnd() {
        engine.endStroke()
    }

    fun onSliceSegment(x1: Float, y1: Float, x2: Float, y2: Float): Int {
        if (_uiState.value.isPaused || _uiState.value.phase != GamePhase.PLAYING || _uiState.value.readyCountdown > 0f || _uiState.value.isGameOverBannerShowing) return 0
        return engine.processSliceSegment(x1, y1, x2, y2)
    }

    fun updateFrame(deltaRealSeconds: Float) {
        if (_uiState.value.phase != GamePhase.PLAYING || _uiState.value.isPaused) return

        // 1. Handle Ready Countdown (3 seconds before items start)
        if (_uiState.value.readyCountdown > 0f) {
            val updatedCountdown = (_uiState.value.readyCountdown - deltaRealSeconds).coerceAtLeast(0f)
            _uiState.value = _uiState.value.copy(readyCountdown = updatedCountdown)
            return
        }

        // 2. Handle Game Over Banner pause
        if (_uiState.value.isGameOverBannerShowing) {
            var currentShake = 0f
            if (shakeTimer > 0f) {
                shakeTimer -= deltaRealSeconds
                currentShake = (shakeTimer / 0.35f) * 18f
            }
            _uiState.value = _uiState.value.copy(screenShakeIntensity = currentShake)
            return
        }

        var currentSlowMo = 1.0f
        if (slowMoTimer > 0f) {
            slowMoTimer -= deltaRealSeconds
            currentSlowMo = 0.30f
        }

        val effectiveDt = deltaRealSeconds * currentSlowMo
        engine.update(effectiveDt)

        var currentFlash = _uiState.value.flashOverlayColor
        if (flashTimer > 0f) {
            flashTimer -= deltaRealSeconds
            if (flashTimer <= 0f) {
                currentFlash = null
            }
        }

        var currentShake = 0f
        if (shakeTimer > 0f) {
            shakeTimer -= deltaRealSeconds
            currentShake = (shakeTimer / 0.35f) * 18f
        }

        var currentFeedback = _uiState.value.feedbackMessage
        if (feedbackToastTimer > 0f) {
            feedbackToastTimer -= deltaRealSeconds
            if (feedbackToastTimer <= 0f) {
                currentFeedback = null
            }
        }

        _uiState.value = _uiState.value.copy(
            score = engine.score,
            lives = engine.lives,
            baseLives = engine.baseLives,
            shieldBonusLives = engine.shieldBonusLives,
            comboMultiplier = engine.comboMultiplier,
            comboStreak = engine.comboStreak,
            timeRemaining = engine.timeRemainingSeconds,
            trapsAvoided = engine.trapsAvoidedCount,
            trapsSliced = engine.trapsSlicedCount,
            slowMoFactor = currentSlowMo,
            flashOverlayColor = currentFlash,
            screenShakeIntensity = currentShake,
            feedbackMessage = currentFeedback,
            isFreezeActive = engine.isFreezeActive,
            freezeTimeRemaining = engine.freezeTimer,
            freezeBonusHits = engine.freezeBonusHits
        )
    }

    private fun handleGameOver(finalScore: Int) {
        if (isMusicSpedUp) {
            isMusicSpedUp = false
            musicManager.setPlaybackSpeed(1.0f)
        }
        val level = _uiState.value.selectedLevel
        val difficulty = _uiState.value.selectedDifficulty
        val stars = level.calculateStars(finalScore)
        val user = _uiState.value.currentUser ?: "Guest"

        val recap = engine.getSlicedCategoriesSummary()
        val categoryBreakdown = mutableMapOf<ComplianceCategory, Int>()
        for (item in recap) {
            categoryBreakdown[item.category] = item.count
        }

        // 1. Trigger GAME OVER banner display
        shakeTimer = 0.40f
        _uiState.value = _uiState.value.copy(
            isGameOverBannerShowing = true,
            screenShakeIntensity = 16f
        )

        viewModelScope.launch {
            val updatedStats = leaderboardRepository.recordGameResult(
                username = user,
                levelNumber = level.levelNumber,
                difficulty = difficulty.id,
                score = finalScore,
                stars = stars,
                violationsSliced = engine.totalViolationsSlicedCount,
                trapsAvoided = engine.trapsAvoidedCount,
                trapsSliced = engine.trapsSlicedCount,
                maxComboStreak = engine.maxComboStreak,
                tierReached = engine.highestTierReachedInRound,
                categoryBreakdown = categoryBreakdown
            )
            val sessions = leaderboardRepository.getRecentSessions(user, 10)

            val currentHigh = _uiState.value.highScore
            val isNewHigh = finalScore > currentHigh

            // Display "GAME OVER" banner for 2.2 seconds before transitioning to Shift Report
            delay(2200L)

            _uiState.value = _uiState.value.copy(
                phase = GamePhase.RESULT,
                score = finalScore,
                highScore = maxOf(currentHigh, finalScore),
                isNewHighScore = isNewHigh,
                starsEarned = stars,
                slicedCategoriesSummary = recap,
                trapsAvoided = engine.trapsAvoidedCount,
                trapsSliced = engine.trapsSlicedCount,
                timeRemaining = engine.elapsedTimeSeconds,
                userStats = updatedStats,
                userAvatarId = updatedStats?.avatarId ?: 1,
                recentSessions = sessions,
                isPaused = false,
                isGameOverBannerShowing = false,
                feedbackMessage = null,
                flashOverlayColor = null,
                screenShakeIntensity = 0f
            )
            musicManager.playMenuTheme()
        }
    }

    // =========================================================================
    // LEADERBOARD & STATS
    // =========================================================================

    fun refreshLeaderboard() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLeaderboardLoading = true)
            val result = leaderboardRepository.getTop100Leaderboard()
            val user = _uiState.value.currentUser
            val userStats = if (user != null) leaderboardRepository.getUserStats(user) else null
            val sessions = if (user != null) leaderboardRepository.getRecentSessions(user, 10) else emptyList()

            _uiState.value = _uiState.value.copy(
                leaderboardEntries = result.entries,
                isLeaderboardOffline = result.isOffline,
                leaderboardErrorMessage = result.errorMessage,
                isLeaderboardLoading = false,
                userStats = userStats,
                userAvatarId = userStats?.avatarId ?: 1,
                recentSessions = sessions
            )
        }
    }

    fun getRankRes(score: Int): Int {
        return when {
            score >= 2000 -> R.string.rank_director
            score >= 1200 -> R.string.rank_senior_officer
            score >= 800 -> R.string.rank_risk_lead
            score >= 450 -> R.string.rank_officer
            score >= 200 -> R.string.rank_auditor
            else -> R.string.rank_intern
        }
    }

    override fun onCleared() {
        super.onCleared()
        soundManager.release()
        musicManager.release()
    }
}
