package com.example

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsAnimationCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.GameScreen
import com.example.ui.screens.IconGlossaryScreen
import com.example.ui.screens.LeaderboardScreen
import com.example.ui.screens.LevelSelectScreen
import com.example.ui.screens.LoginRegisterScreen
import com.example.ui.screens.MainMenuScreen
import com.example.ui.screens.ResultScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.ComplianceSlicerTheme
import com.example.ui.viewmodel.GamePhase
import com.example.ui.viewmodel.GameViewModel

class MainActivity : AppCompatActivity() {

    private var isImeAnimationRunning = false
    private var lastImeHideTimestamp = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ComplianceApplication.ensureFirebaseInitialized(application)
        enableEdgeToEdge()
        setupInsetsAnimationListener()
        hideSystemNavigationBar()
        setContent {
            ComplianceSlicerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = androidx.compose.ui.graphics.Color.White
                ) {
                    ComplianceSlicerApp()
                }
            }
        }
    }

    private fun setupInsetsAnimationListener() {
        val decorView = window?.decorView ?: return
        ViewCompat.setWindowInsetsAnimationCallback(
            decorView,
            object : WindowInsetsAnimationCompat.Callback(DISPATCH_MODE_CONTINUE_ON_SUBTREE) {
                override fun onPrepare(animation: WindowInsetsAnimationCompat) {
                    if ((animation.typeMask and WindowInsetsCompat.Type.ime()) != 0) {
                        isImeAnimationRunning = true
                    }
                }

                override fun onStart(
                    animation: WindowInsetsAnimationCompat,
                    bounds: WindowInsetsAnimationCompat.BoundsCompat
                ): WindowInsetsAnimationCompat.BoundsCompat {
                    if ((animation.typeMask and WindowInsetsCompat.Type.ime()) != 0) {
                        isImeAnimationRunning = true
                    }
                    return bounds
                }

                override fun onProgress(
                    insets: WindowInsetsCompat,
                    runningAnimations: MutableList<WindowInsetsAnimationCompat>
                ): WindowInsetsCompat {
                    return insets
                }

                override fun onEnd(animation: WindowInsetsAnimationCompat) {
                    if ((animation.typeMask and WindowInsetsCompat.Type.ime()) != 0) {
                        isImeAnimationRunning = false
                        lastImeHideTimestamp = System.currentTimeMillis()
                        // Allow IME close animation to fully settle (800ms) before re-hiding navigation bars
                        decorView.postDelayed({
                            if (!isDestroyed && !isFinishing && !isImeAnimationRunning) {
                                hideSystemNavigationBar()
                            }
                        }, 800L)
                    }
                }
            }
        )
    }

    override fun onResume() {
        super.onResume()
        decorViewPostSafeHide(500L)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            decorViewPostSafeHide(500L)
        }
    }

    private fun decorViewPostSafeHide(delayMs: Long) {
        window?.decorView?.postDelayed({
            if (!isDestroyed && !isFinishing && !isImeAnimationRunning) {
                hideSystemNavigationBar()
            }
        }, delayMs)
    }

    fun hideSystemNavigationBar() {
        if (isImeAnimationRunning) {
            return
        }
        // If IME just finished closing within the last 600ms, wait before hiding system bars
        if (System.currentTimeMillis() - lastImeHideTimestamp < 600L) {
            return
        }
        val window = window ?: return
        val decorView = window.decorView ?: return
        val rootInsets = ViewCompat.getRootWindowInsets(decorView)
        // If the soft keyboard (IME) is visible or animating, do NOT touch system bars!
        val isImeVisible = rootInsets?.isVisible(WindowInsetsCompat.Type.ime()) == true
        if (isImeVisible || isImeAnimationRunning) {
            return
        }

        val insetsController = WindowCompat.getInsetsController(window, decorView)
        insetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        // Only request hide if navigation bars or status bars are currently showing
        val areBarsVisible = rootInsets == null ||
            rootInsets.isVisible(WindowInsetsCompat.Type.navigationBars()) ||
            rootInsets.isVisible(WindowInsetsCompat.Type.statusBars())

        if (areBarsVisible) {
            insetsController.hide(WindowInsetsCompat.Type.systemBars())
        }
    }
}

@Composable
fun ComplianceSlicerApp(
    viewModel: GameViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    val activity = context as? MainActivity

    // Background music lifecycle & screen transition management
    LaunchedEffect(uiState.phase, uiState.isAudioMuted) {
        if (uiState.isAudioMuted) {
            viewModel.musicManager.setMuted(true)
        } else {
            viewModel.musicManager.setMuted(false)
            when (uiState.phase) {
                GamePhase.PLAYING -> viewModel.musicManager.playGameplayTrack()
                GamePhase.MENU, GamePhase.LEVEL_SELECT, GamePhase.GLOSSARY,
                GamePhase.LEADERBOARD, GamePhase.PROFILE, GamePhase.RESULT, GamePhase.LOGIN_REGISTER -> {
                    viewModel.musicManager.playMenuTrack()
                }
                GamePhase.SPLASH -> {
                    viewModel.musicManager.stop()
                }
            }
        }
    }

    // Background music lifecycle management
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    viewModel.musicManager.pause()
                }
                Lifecycle.Event.ON_RESUME -> {
                    if (!uiState.isAudioMuted) {
                        when (uiState.phase) {
                            GamePhase.PLAYING -> viewModel.musicManager.playGameplayTrack()
                            GamePhase.MENU, GamePhase.LEVEL_SELECT, GamePhase.GLOSSARY,
                            GamePhase.LEADERBOARD, GamePhase.PROFILE, GamePhase.RESULT, GamePhase.LOGIN_REGISTER -> {
                                viewModel.musicManager.playMenuTrack()
                            }
                            GamePhase.SPLASH -> Unit
                        }
                    }
                }
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val onSelectLanguage: (String) -> Unit = { lang ->
        viewModel.setLanguage(lang) { activity?.recreate() }
    }

    Crossfade(
        targetState = uiState.phase,
        animationSpec = tween(300),
        label = "screen_transition",
        modifier = modifier.fillMaxSize()
    ) { phase ->
        when (phase) {
            GamePhase.SPLASH -> {
                SplashScreen(
                    onSplashFinished = { viewModel.onSplashFinished() }
                )
            }

            GamePhase.LOGIN_REGISTER -> {
                LoginRegisterScreen(
                    currentLanguage = uiState.currentLanguage,
                    isAudioMuted = uiState.isAudioMuted,
                    errorMessage = uiState.authErrorMessage,
                    onCheckUsernameTaken = { u -> viewModel.isUsernameTaken(u) },
                    onLogin = { u, p -> viewModel.login(u, p) },
                    onRegister = { u, p -> viewModel.register(u, p) },
                    onResetPassword = { u, p -> viewModel.resetPassword(u, p) },
                    onToggleLanguage = {},
                    onSelectLanguage = onSelectLanguage,
                    onToggleAudioMute = { viewModel.toggleAudioMute() }
                )
            }

            GamePhase.MENU -> {
                MainMenuScreen(
                    currentUser = uiState.currentUser,
                    userAvatarId = uiState.userAvatarId,
                    highScore = uiState.highScore,
                    currentLanguage = uiState.currentLanguage,
                    isAudioMuted = uiState.isAudioMuted,
                    shouldShowComic = uiState.shouldShowComic,
                    onComicDismissed = { viewModel.onComicDismissed() },
                    onStartShift = { viewModel.navigateTo(GamePhase.LEVEL_SELECT) },
                    onOpenLeaderboard = { viewModel.navigateTo(GamePhase.LEADERBOARD) },
                    onOpenGlossary = { viewModel.navigateTo(GamePhase.GLOSSARY) },
                    onOpenProfile = { viewModel.navigateTo(GamePhase.PROFILE) },
                    onToggleLanguage = {},
                    onSelectLanguage = onSelectLanguage,
                    onToggleAudioMute = { viewModel.toggleAudioMute() },
                    onLogout = { viewModel.logout() },
                    onPauseMusic = {
                        viewModel.musicManager.isTrailerPlaying = true
                        viewModel.musicManager.pause()
                    },
                    onResumeMusic = {
                        viewModel.musicManager.isTrailerPlaying = false
                        if (!uiState.isAudioMuted) viewModel.musicManager.resume()
                    }
                )
            }

            GamePhase.LEVEL_SELECT -> {
                LevelSelectScreen(
                    userStats = uiState.userStats,
                    onStartLevel = { level, diff -> viewModel.startMission(level, diff) },
                    onOpenGlossary = { viewModel.openGlossary() },
                    onBackToMenu = { viewModel.navigateTo(GamePhase.MENU) },
                    currentLanguage = uiState.currentLanguage
                )
            }

            GamePhase.GLOSSARY -> {
                IconGlossaryScreen(
                    currentLanguage = uiState.currentLanguage,
                    onBack = { viewModel.closeGlossary() }
                )
            }

            GamePhase.LEADERBOARD -> {
                LeaderboardScreen(
                    currentUser = uiState.currentUser,
                    userStats = uiState.userStats,
                    leaderboardEntries = uiState.leaderboardEntries,
                    recentSessions = uiState.recentSessions,
                    isOffline = uiState.isLeaderboardOffline,
                    isLoading = uiState.isLeaderboardLoading,
                    errorMessage = uiState.leaderboardErrorMessage,
                    initialTab = 0,
                    currentLanguage = uiState.currentLanguage,
                    onRefresh = { viewModel.refreshLeaderboard() },
                    onBack = { viewModel.navigateTo(GamePhase.MENU) }
                )
            }

            GamePhase.PROFILE -> {
                LeaderboardScreen(
                    currentUser = uiState.currentUser,
                    userStats = uiState.userStats,
                    leaderboardEntries = uiState.leaderboardEntries,
                    recentSessions = uiState.recentSessions,
                    isOffline = uiState.isLeaderboardOffline,
                    isLoading = uiState.isLeaderboardLoading,
                    errorMessage = uiState.leaderboardErrorMessage,
                    initialTab = 1,
                    currentLanguage = uiState.currentLanguage,
                    onRefresh = { viewModel.refreshLeaderboard() },
                    onBack = { viewModel.navigateTo(GamePhase.MENU) }
                )
            }

            GamePhase.PLAYING -> {
                GameScreen(
                    viewModel = viewModel,
                    uiState = uiState,
                    onOpenGlossary = { viewModel.openGlossary() },
                    onReturnToMenu = { viewModel.navigateTo(GamePhase.MENU) }
                )
            }

            GamePhase.RESULT -> {
                ResultScreen(
                    level = uiState.selectedLevel,
                    difficulty = uiState.selectedDifficulty,
                    score = uiState.score,
                    stars = uiState.starsEarned,
                    highScore = uiState.highScore,
                    isNewHighScore = uiState.isNewHighScore,
                    rankRes = viewModel.getRankRes(uiState.score),
                    trapsAvoided = uiState.trapsAvoided,
                    trapsSliced = uiState.trapsSliced,
                    slicedSummary = uiState.slicedCategoriesSummary,
                    elapsedSeconds = uiState.timeRemaining,
                    currentLanguage = uiState.currentLanguage,
                    onOpenRules = { viewModel.openGlossary() },
                    onPlayAgain = { viewModel.startMission(uiState.selectedLevel) },
                    onSelectLevel = { viewModel.navigateTo(GamePhase.LEVEL_SELECT) },
                    onReturnToMenu = { viewModel.navigateTo(GamePhase.MENU) }
                )
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    androidx.compose.material3.Text(text = "Hello $name!", modifier = modifier)
}
