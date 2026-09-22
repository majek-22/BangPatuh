package com.example.ui.screens

import android.app.Activity
import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.BadgeShape
import com.example.data.ComplianceCategory
import com.example.data.FloatingPopup
import com.example.data.GameItem
import com.example.data.SliceTrailPoint
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.BackgroundGradientEnd
import com.example.ui.theme.BrandPrimary
import com.example.ui.theme.CardLegitimateEnd
import com.example.ui.theme.CardLegitimateStart
import com.example.ui.theme.CardViolationEnd
import com.example.ui.theme.CardViolationStart
import com.example.ui.theme.ComboBadgeStyle
import com.example.ui.theme.CoralPrimary
import com.example.ui.theme.CrimsonDanger
import com.example.ui.theme.CyanEnergy
import com.example.ui.theme.GlassBorderLight
import com.example.ui.theme.GlassWhite05
import com.example.ui.theme.GlassWhite10
import com.example.ui.theme.GlassWhite20
import com.example.ui.theme.GlassWhite40
import com.example.ui.theme.GoldSecondary
import com.example.ui.theme.HeaderBorderBottom
import com.example.ui.theme.HeaderGlassBg
import com.example.ui.theme.HudLabelStyle
import com.example.ui.theme.HudScoreDigitsStyle
import com.example.ui.theme.MintSuccess
import com.example.ui.theme.SheetContainerBg
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TimerDigitsStyle
import com.example.ui.viewmodel.GameUiState
import com.example.ui.viewmodel.GameViewModel
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun GameScreen(
    viewModel: GameViewModel,
    uiState: GameUiState,
    onOpenGlossary: () -> Unit,
    onReturnToMenu: () -> Unit,
    modifier: Modifier = Modifier
) {
    val iconPainters = rememberCategoryPainters()
    val sliceTrail = remember { mutableStateListOf<SliceTrailPoint>() }
    // Lingering slices created during freeze bonus mode (remain visible until freeze ends)
    val freezeSlices = remember { mutableStateListOf<FreezeSlash>() }

    // Clear lingering freeze slices automatically when freeze bonus mode expires
    LaunchedEffect(uiState.isFreezeActive) {
        if (!uiState.isFreezeActive) {
            freezeSlices.clear()
        }
    }

    val context = LocalContext.current
    val window = (context as? Activity)?.window

    // Immersive full-screen mode during active gameplay so notifications/bars don't interfere with slicing
    DisposableEffect(window) {
        if (window != null) {
            val insetsController = WindowCompat.getInsetsController(window, window.decorView)
            insetsController.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            insetsController.hide(WindowInsetsCompat.Type.systemBars())

            onDispose {
                insetsController.hide(WindowInsetsCompat.Type.navigationBars())
            }
        } else {
            onDispose { }
        }
    }

    val popupPaint = remember {
        Paint().apply {
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }
    }

    LaunchedEffect(uiState.isPaused) {
        if (!uiState.isPaused) {
            var lastFrameNanos = withFrameNanos { it }
            while (true) {
                withFrameNanos { frameTimeNanos ->
                    val dt = (frameTimeNanos - lastFrameNanos) / 1_000_000_000f
                    lastFrameNanos = frameTimeNanos
                    viewModel.updateFrame(dt)

                    val now = System.nanoTime()
                    val threshold = if (uiState.isFreezeActive) 350_000_000L else if (uiState.comboMultiplier >= 4) 280_000_000L else 220_000_000L
                    sliceTrail.removeAll { (now - it.timestampNanos) > threshold }
                }
            }
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "shake_anim")
    val shakeFactor by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(60, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shake"
    )

    val shakeOffsetX = (shakeFactor * uiState.screenShakeIntensity * 0.8f).toInt()
    val shakeOffsetY = (shakeFactor * uiState.screenShakeIntensity * 0.5f).toInt()

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .offset { IntOffset(shakeOffsetX, shakeOffsetY) }
    ) {
        val density = LocalDensity.current
        val widthPx = with(density) { maxWidth.toPx() }
        val heightPx = with(density) { maxHeight.toPx() }

        LaunchedEffect(widthPx, heightPx) {
            viewModel.setScreenDimensions(widthPx, heightPx)
        }

        // 1. FULLSCREEN GAMEPLAY BACKGROUND IMAGE (DYNAMIC PER MISSION)
        Image(
            painter = painterResource(id = uiState.selectedLevel.backgroundRes),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // FREEZE BONUS DARK ATMOSPHERE OVERLAY ("screen otomatis terlihat sedikit ada efek gelap")
        val freezeDarkAlpha by animateFloatAsState(
            targetValue = if (uiState.isFreezeActive) 0.58f else 0f,
            animationSpec = tween(350),
            label = "freezeDarkAlpha"
        )
        if (freezeDarkAlpha > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF020712).copy(alpha = freezeDarkAlpha * 0.70f),
                                Color(0xFF000308).copy(alpha = freezeDarkAlpha)
                            )
                        )
                    )
            )
        }

        // Safe-Zone Debug Overlay (Feature 3)
        if (uiState.debugOverlayEnabled) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val left = viewModel.engine.safeZoneLeft
                val right = viewModel.engine.safeZoneRight
                val top = viewModel.engine.safeZoneTop
                val bottom = viewModel.engine.safeZoneBottom

                drawRect(
                    color = Color(0x1800E5FF),
                    topLeft = Offset(left, top),
                    size = Size(right - left, bottom - top)
                )
                drawRect(
                    color = Color(0x8800E5FF),
                    topLeft = Offset(left, top),
                    size = Size(right - left, bottom - top),
                    style = Stroke(width = 2.5f)
                )
                drawLine(
                    color = Color(0xFFFFD54F),
                    start = Offset(0f, top),
                    end = Offset(size.width, top),
                    strokeWidth = 2f
                )
            }
        }

        // 2. ACTIVE FLYING ITEMS: Rendered as discrete Compose composables driven by SnapshotStateList
        for (item in viewModel.engine.activeItems) {
            key(item.id) {
                FlyingItemComposable(
                    item = item,
                    painter = iconPainters[item.category],
                    isCombo4xActive = uiState.comboMultiplier >= 4
                )
            }
        }

        // 3. FOREGROUND INTERACTIVE CANVAS (Slice Trail, Particles, Popups, Pointer Input)
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .testTag("playfield_canvas")
                .pointerInput(uiState.isPaused) {
                    if (!uiState.isPaused) {
                        var lastX = 0f
                        var lastY = 0f
                        detectDragGestures(
                            onDragStart = { offset ->
                                lastX = offset.x
                                lastY = offset.y
                                val now = System.nanoTime()
                                sliceTrail.clear()
                                sliceTrail.add(SliceTrailPoint(lastX, lastY, now))
                                viewModel.onSliceStart()
                            },
                            onDrag = { change, _ ->
                                change.consume()
                                val curX = change.position.x
                                val curY = change.position.y
                                val now = System.nanoTime()

                                sliceTrail.add(SliceTrailPoint(curX, curY, now))
                                viewModel.onSliceSegment(lastX, lastY, curX, curY)

                                // When freeze bonus mode is active, record slice cuts so they stay visible until freeze ends
                                if (uiState.isFreezeActive) {
                                    val dx = curX - lastX
                                    val dy = curY - lastY
                                    if (dx * dx + dy * dy > 4f) {
                                        freezeSlices.add(
                                            FreezeSlash(
                                                x1 = lastX,
                                                y1 = lastY,
                                                x2 = curX,
                                                y2 = curY,
                                                auraColor = if (freezeSlices.size % 2 == 0) Color(0xFF00E5FF) else Color(0xFFFF9800),
                                                midColor = Color(0xFFFFD700)
                                            )
                                        )
                                    }
                                }

                                lastX = curX
                                lastY = curY
                            },
                            onDragEnd = {
                                viewModel.onSliceEnd()
                            },
                            onDragCancel = {
                                viewModel.onSliceEnd()
                            }
                        )
                    }
                }
        ) {
            // Draw particle bursts
            val particles = viewModel.engine.particles.toList()
            for (p in particles) {
                drawCircle(
                    color = Color(p.color).copy(alpha = p.alpha),
                    radius = p.size,
                    center = Offset(p.x, p.y)
                )
            }

            // Draw lingering freeze bonus slices that stay visible until freeze time finishes
            if (freezeSlices.isNotEmpty()) {
                drawFreezeSlices(freezeSlices)
            }

            // Draw active slice trail (normal katana slice effect, cyan if freeze bonus active)
            drawSliceTrail(
                points = sliceTrail,
                isFreezeActive = uiState.isFreezeActive,
                isCombo4xActive = uiState.comboMultiplier >= 4
            )

            // Draw floating popups
            val popups = viewModel.engine.popups.toList()
            for (popup in popups) {
                drawFloatingPopup(popup, popupPaint)
            }
        }

        // 2. TOP HUD BAR (Time and Volume to the left of lives, no pause button, clean flat background)
        TopHudBar(
            levelNumber = uiState.selectedLevel.levelNumber,
            score = uiState.score,
            lives = uiState.lives,
            baseLives = uiState.baseLives,
            shieldBonusLives = uiState.shieldBonusLives,
            comboMultiplier = uiState.comboMultiplier,
            elapsedSeconds = uiState.timeRemaining,
            currentLanguage = uiState.currentLanguage,
            isAudioMuted = uiState.isAudioMuted,
            onToggleAudioMute = { viewModel.toggleAudioMute() },
            isPaused = uiState.isPaused,
            onTogglePause = { if (uiState.isPaused) viewModel.resumeGame() else viewModel.pauseGame() },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
        )

        // Pause overlay dialog
        if (uiState.isPaused) {
            AlertDialog(
                onDismissRequest = { viewModel.resumeGame() },
                title = {
                    Text(
                        text = stringResource(R.string.dialog_paused_title),
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(text = stringResource(R.string.menu_subtitle))
                },
                confirmButton = {
                    Button(
                        onClick = { viewModel.resumeGame() },
                        colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary)
                    ) {
                        Text(stringResource(R.string.dialog_resume))
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = onReturnToMenu
                    ) {
                        Text(stringResource(R.string.dialog_quit_to_menu))
                    }
                }
            )
        }

        // 3. FREEZE BONUS ACTIVE HUD
        AnimatedVisibility(
            visible = uiState.isFreezeActive,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 70.dp, start = 16.dp, end = 16.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xF2081C2E),
                border = BorderStroke(2.dp, Color(0xFF00E5FF)),
                shadowElevation = 12.dp
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val isIndonesian = uiState.currentLanguage == "in" || uiState.currentLanguage == "id"
                    val isJapanese = uiState.currentLanguage == "ja"
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = when {
                                isJapanese -> "❄️ フリーズボーナス！ ❄️"
                                isIndonesian -> "❄️ BONUS BEKU! ❄️"
                                else -> "❄️ FREEZE BONUS! ❄️"
                            },
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF00E5FF)
                            )
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF00E5FF).copy(alpha = 0.25f),
                            border = BorderStroke(1.dp, Color(0xFF00E5FF))
                        ) {
                            Text(
                                text = String.format(java.util.Locale.US, "%.1fs", uiState.freezeTimeRemaining),
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    val corruptorBonusText = when {
                        isJapanese -> "腐敗者を連撃せよ！ ${uiState.freezeBonusHits} 回連撃 (+${uiState.freezeBonusHits * 10} 点)"
                        isIndonesian -> "Tebas Corruptor Bebas! ${uiState.freezeBonusHits} Tebasan (+${uiState.freezeBonusHits * 10} Poin)"
                        else -> "Slash Corruptor Freely! ${uiState.freezeBonusHits} Slashes (+${uiState.freezeBonusHits * 10} Points)"
                    }
                    Text(
                        text = corruptorBonusText,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFD700)
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .width(200.dp)
                            .height(5.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color(0xFF0C2C4D))
                    ) {
                        val progress = (uiState.freezeTimeRemaining / 5.0f).coerceIn(0f, 1f)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progress)
                                .fillMaxHeight()
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFF00E5FF), Color(0xFFFFD700))
                                    )
                                )
                        )
                    }
                }
            }
        }

        // 4. FROST & DARK VIGNETTE BORDER WHEN FREEZE BONUS IS ACTIVE
        if (uiState.isFreezeActive) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color(0x66000511)
                            )
                        )
                    )
                    .border(
                        BorderStroke(
                            3.5.dp,
                            Brush.horizontalGradient(
                                listOf(Color(0xFF00E5FF), Color(0xFFFFD700), Color(0xFF00E5FF))
                            )
                        )
                    )
            )
        }

        // 5. SLICE ALERT / FEEDBACK BANNER
        AnimatedVisibility(
            visible = uiState.feedbackMessage != null && !uiState.isFreezeActive,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 76.dp, start = 20.dp, end = 20.dp)
        ) {
            uiState.feedbackMessage?.let { msg ->
                val bannerBorder = if (uiState.feedbackIsPositive) MintSuccess else CrimsonDanger
                val bannerBg = if (uiState.feedbackIsPositive) Color(0xEE143627) else Color(0xEE3F1218)

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = bannerBg,
                    border = BorderStroke(1.5.dp, bannerBorder),
                    shadowElevation = 8.dp
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Icon(
                            imageVector = if (uiState.feedbackIsPositive) Icons.Default.Favorite else Icons.Default.Warning,
                            contentDescription = null,
                            tint = bannerBorder,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = msg,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )
                    }
                }
            }
        }

        // FRUIT NINJA COMBO BURST OVERLAY (Exact arcade match to Fruit Ninja screenshot)
        uiState.activeComboBurst?.let { burst ->
            key(burst.id) {
                FruitNinjaComboOverlay(
                    burst = burst,
                    currentLanguage = uiState.currentLanguage,
                    onDismiss = {
                        viewModel.dismissComboBurst(burst.id)
                    }
                )
            }
        }

        // 4. FULLSCREEN IMPACT FLASH OVERLAY
        uiState.flashOverlayColor?.let { flashColor ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(flashColor))
            )
        }

        // 5. READY COUNTDOWN OVERLAY (3 Seconds Orange Banner)
        if (uiState.readyCountdown > 0f) {
            val countdownInt = (uiState.readyCountdown.toInt() + 1).coerceIn(1, 3)
            val readyTransition = rememberInfiniteTransition(label = "ready_pulse")
            val readyScale by readyTransition.animateFloat(
                initialValue = 0.94f,
                targetValue = 1.06f,
                animationSpec = infiniteRepeatable(
                    animation = tween(450, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "ready_scale"
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x66000000)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.scale(readyScale)
                ) {
                    val readyText = when (uiState.currentLanguage.lowercase()) {
                        "ja" -> "準備..."
                        "in", "id" -> "Bersiap..."
                        else -> stringResource(R.string.gameplay_ready)
                    }
                    Text(
                        text = readyText,
                        color = Color(0xFFFF9800), // Orange
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp,
                        style = androidx.compose.ui.text.TextStyle(
                            shadow = androidx.compose.ui.graphics.Shadow(
                                color = Color(0xCC000000),
                                offset = Offset(4f, 4f),
                                blurRadius = 14f
                            )
                        )
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(Color(0x33FF9800))
                            .border(2.dp, Color(0xFFFF9800), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$countdownInt",
                            color = Color(0xFFFFB74D),
                            fontSize = 26.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }

        // 6. GAME OVER BANNER (Red)
        if (uiState.isGameOverBannerShowing) {
            val goTransition = rememberInfiniteTransition(label = "game_over_pulse")
            val goScale by goTransition.animateFloat(
                initialValue = 0.95f,
                targetValue = 1.10f,
                animationSpec = infiniteRepeatable(
                    animation = tween(400, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "go_scale"
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x88000000)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.scale(goScale)
                ) {
                    val gameOverText = when (uiState.currentLanguage.lowercase()) {
                        "ja" -> "ゲームオーバー"
                        "in", "id" -> "PERMAINAN SELESAI"
                        else -> stringResource(R.string.gameplay_game_over)
                    }
                    Text(
                        text = gameOverText,
                        color = Color(0xFFFF2A2A), // Bold Red
                        fontSize = if (uiState.currentLanguage.lowercase() in listOf("in", "id")) 38.sp else 52.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = if (uiState.currentLanguage.lowercase() in listOf("in", "id")) 1.sp else 3.sp,
                        textAlign = TextAlign.Center,
                        style = androidx.compose.ui.text.TextStyle(
                            shadow = androidx.compose.ui.graphics.Shadow(
                                color = Color(0xFF000000),
                                offset = Offset(6f, 6f),
                                blurRadius = 18f
                            )
                        )
                    )
                }
            }
        }
    }
}

// =========================================================================
// CANVAS RENDERING HELPERS
// =========================================================================

@Composable
private fun FlyingItemComposable(
    item: GameItem,
    painter: Painter?,
    isCombo4xActive: Boolean = false,
    modifier: Modifier = Modifier
) {
    if (painter == null) return

    val categoryColor = when {
        item.category.isFreezeBonus -> Color(0xFF00E676)
        item.category.isBonus -> Color(0xFF00E676)
        item.category.isTrap -> Color(0xFFFF5252)
        item.isViolation -> Color(0xFFFFC857)
        else -> Color(0xFF64B5F6)
    }

    val discBgColor = when {
        item.category.isFreezeBonus -> Color(0xFF052F1A)
        item.category.isBonus -> Color(0xFF052F1A)
        item.category.isTrap -> Color(0xFF3E0E0E)
        item.isViolation -> Color(0xFF3E2805)
        else -> Color(0xFF0C2C4D)
    }

    if (!item.sliced) {
        val density = LocalDensity.current
        val diameterDp = with(density) { (item.radius * 2f).toDp() }

        Box(
            modifier = modifier
                .offset {
                    IntOffset(
                        (item.x - item.radius).toInt(),
                        (item.y - item.radius).toInt()
                    )
                }
                .size(diameterDp)
                .graphicsLayer {
                    alpha = item.alpha
                }
        ) {
            // Rotating vector token & neon aura
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        rotationZ = item.rotation
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val r = size.width / 2f

                    // 1. Radiant outer neon aura matching RULES category color
                    val auraColors = if (item.category.isFreezeBonus) {
                        listOf(
                            Color(0xFF00E676).copy(alpha = 0.85f),
                            Color(0xFFFFD700).copy(alpha = 0.45f),
                            Color.Transparent
                        )
                    } else {
                        listOf(
                            categoryColor.copy(alpha = 0.75f),
                            categoryColor.copy(alpha = 0.22f),
                            Color.Transparent
                        )
                    }

                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = auraColors,
                            radius = r * 1.55f,
                            center = Offset(r, r)
                        ),
                        radius = r * 1.55f,
                        center = Offset(r, r)
                    )

                    // 2. Vibrant colored background disc matching RULES (not pitch black!)
                    drawCircle(
                        color = discBgColor,
                        radius = r,
                        center = Offset(r, r)
                    )

                    // Inner soft highlight disc
                    drawCircle(
                        color = categoryColor.copy(alpha = 0.25f),
                        radius = r * 0.88f,
                        center = Offset(r, r)
                    )

                    // 3. Draw original vector icon in full colors centered inside the category disc
                    val iconDrawSize = if (item.category.isFreezeBonus) r * 1.76f else r * 1.52f
                    val iconPadding = (r * 2f - iconDrawSize) / 2f
                    clipPath(
                        path = Path().apply {
                            addOval(androidx.compose.ui.geometry.Rect(r - r * 0.90f, r - r * 0.90f, r + r * 0.90f, r + r * 0.90f))
                        }
                    ) {
                        withTransform({
                            translate(left = iconPadding, top = iconPadding)
                        }) {
                            with(painter) {
                                draw(size = Size(iconDrawSize, iconDrawSize))
                            }
                        }
                    }

                    // 4. Accessible, crisp rim border ring matching RULES
                    drawCircle(
                        color = categoryColor,
                        radius = r * 0.95f,
                        center = Offset(r, r),
                        style = Stroke(width = if (item.category.isFreezeBonus) 5f else 3.5f)
                    )
                }
            }

            // High-contrast floating pill label below token matching the Gameplay Rules title
            val iconName = androidx.compose.ui.res.stringResource(item.category.displayNameRes)

            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = 10.dp)
                    .wrapContentWidth(unbounded = true),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xF2060E1A),
                border = BorderStroke(1.dp, categoryColor.copy(alpha = 0.85f)),
                shadowElevation = 3.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.5.dp)
                ) {
                    // Category glow indicator pip
                    Box(
                        modifier = Modifier
                            .size(3.5.dp)
                            .clip(CircleShape)
                            .background(categoryColor)
                    )
                    Text(
                        text = iconName,
                        fontSize = 7.5.sp,
                        lineHeight = 9.sp,
                        fontWeight = FontWeight.Normal,
                        letterSpacing = 0.2.sp,
                        textAlign = TextAlign.Center,
                        color = Color.White,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }
    } else {
        // Sliced halves rendered on playfield
        Canvas(modifier = modifier.fillMaxSize()) {
            drawSlicedHalves(item, painter)
        }
    }
}

private fun DrawScope.drawSlicedHalves(
    item: GameItem,
    painter: Painter
) {
    val rad = item.sliceAngle * (PI.toFloat() / 180f)
    val dirX = cos(rad)
    val dirY = sin(rad)
    val normX = -sin(rad)
    val normY = cos(rad)

    val half1Center = Offset(
        item.x + item.half1OffsetX,
        item.y + item.half1OffsetY
    )
    val half2Center = Offset(
        item.x + item.half2OffsetX,
        item.y + item.half2OffsetY
    )

    drawSlicedHalf(
        center = half1Center,
        rotation = item.rotation + item.halfRotation1,
        radius = item.radius,
        alpha = item.alpha,
        painter = painter,
        category = item.category,
        isFirstHalf = true,
        sliceAngle = item.sliceAngle,
        dirX = dirX,
        dirY = dirY,
        normX = normX,
        normY = normY,
        isCombo4x = false
    )

    drawSlicedHalf(
        center = half2Center,
        rotation = item.rotation + item.halfRotation2,
        radius = item.radius,
        alpha = item.alpha,
        painter = painter,
        category = item.category,
        isFirstHalf = false,
        sliceAngle = item.sliceAngle,
        dirX = dirX,
        dirY = dirY,
        normX = normX,
        normY = normY,
        isCombo4x = false
    )
}

private fun DrawScope.drawSlicedHalf(
    center: Offset,
    rotation: Float,
    radius: Float,
    alpha: Float,
    painter: Painter,
    category: ComplianceCategory,
    isFirstHalf: Boolean,
    sliceAngle: Float,
    dirX: Float,
    dirY: Float,
    normX: Float,
    normY: Float,
    isCombo4x: Boolean = false
) {
    if (alpha <= 0f) return

    val clipExtent = radius * 3.5f
    val clipPath = Path().apply {
        if (isFirstHalf) {
            moveTo(center.x - dirX * clipExtent, center.y - dirY * clipExtent)
            lineTo(center.x + dirX * clipExtent, center.y + dirY * clipExtent)
            lineTo(center.x + dirX * clipExtent + normX * clipExtent, center.y + dirY * clipExtent + normY * clipExtent)
            lineTo(center.x - dirX * clipExtent + normX * clipExtent, center.y - dirY * clipExtent + normY * clipExtent)
            close()
        } else {
            moveTo(center.x - dirX * clipExtent, center.y - dirY * clipExtent)
            lineTo(center.x + dirX * clipExtent, center.y + dirY * clipExtent)
            lineTo(center.x + dirX * clipExtent - normX * clipExtent, center.y + dirY * clipExtent - normY * clipExtent)
            lineTo(center.x - dirX * clipExtent - normX * clipExtent, center.y - dirY * clipExtent - normY * clipExtent)
            close()
        }
    }

    val categoryColor = when {
        category.isFreezeBonus -> Color(0xFF00E676)
        category.isBonus -> Color(0xFF00E676)
        category.isTrap -> Color(0xFFFF5252)
        category.isViolation -> Color(0xFFFFC857)
        else -> Color(0xFF64B5F6)
    }

    val discBgColor = when {
        category.isFreezeBonus -> Color(0xFF052F1A)
        category.isBonus -> Color(0xFF052F1A)
        category.isTrap -> Color(0xFF3E0E0E)
        category.isViolation -> Color(0xFF3E2805)
        else -> Color(0xFF0C2C4D)
    }

    // If Combo 4x: Fiery solar aura behind each severed half
    if (isCombo4x) {
        drawCircle(
            brush = Brush.radialGradient(
                listOf(
                    Color(0xFFFF3D00).copy(alpha = alpha * 0.75f),
                    Color(0xFFFFD700).copy(alpha = alpha * 0.45f),
                    Color.Transparent
                ),
                radius = radius * 1.55f,
                center = center
            ),
            radius = radius * 1.55f,
            center = center
        )
    }

    clipPath(clipPath) {
        withTransform({
            translate(left = center.x, top = center.y)
            rotate(degrees = rotation)
        }) {
            val r = radius

            // Colored backing disc matching RULES (or deep ember glow if Combo 4x)
            drawCircle(
                color = if (isCombo4x) Color(0xFF3A1200).copy(alpha = alpha) else discBgColor.copy(alpha = alpha),
                radius = r
            )

            // Inner soft highlight disc
            drawCircle(
                color = if (isCombo4x) Color(0xFFFF9100).copy(alpha = alpha * 0.35f) else categoryColor.copy(alpha = alpha * 0.25f),
                radius = r * 0.88f
            )

            // Draw vector icon in full original color
            val iconDrawSize = r * 1.52f
            val iconPadding = (r * 2f - iconDrawSize) / 2f
            withTransform({
                translate(left = -r + iconPadding, top = -r + iconPadding)
            }) {
                with(painter) {
                    draw(
                        size = Size(iconDrawSize, iconDrawSize),
                        alpha = alpha
                    )
                }
            }

            // High-contrast rim border ring (Fiery molten gold if Combo 4x)
            drawCircle(
                color = if (isCombo4x) Color(0xFFFFD700).copy(alpha = alpha) else categoryColor.copy(alpha = alpha),
                radius = r * 0.95f,
                style = Stroke(width = if (isCombo4x) 5f else 3.5f)
            )
        }
    }

    // Cut seam effect
    val cutLength = radius * 1.15f
    val pStart = Offset(center.x - dirX * cutLength, center.y - dirY * cutLength)
    val pEnd = Offset(center.x + dirX * cutLength, center.y + dirY * cutLength)

    if (isCombo4x) {
        // Multi-layer Blazing Plasma Katana Seam
        // 1. Broad outer fiery flare
        drawLine(
            color = Color(0xFFFF3D00).copy(alpha = alpha * 0.85f),
            start = pStart,
            end = pEnd,
            strokeWidth = 14f,
            cap = StrokeCap.Round
        )
        // 2. Molten gold core
        drawLine(
            color = Color(0xFFFFD700).copy(alpha = alpha * 0.95f),
            start = pStart,
            end = pEnd,
            strokeWidth = 7f,
            cap = StrokeCap.Round
        )
        // 3. Piercing white heat filament
        drawLine(
            color = Color.White.copy(alpha = alpha),
            start = pStart,
            end = pEnd,
            strokeWidth = 2.8f,
            cap = StrokeCap.Round
        )
        // 4. Sparkling star glints at both ends of the seam
        drawCircle(
            color = Color.White.copy(alpha = alpha),
            radius = 5.5f,
            center = pStart
        )
        drawCircle(
            color = Color(0xFFFFD700).copy(alpha = alpha * 0.9f),
            radius = 7f,
            center = pStart,
            style = Stroke(width = 2f)
        )
        drawCircle(
            color = Color.White.copy(alpha = alpha),
            radius = 5.5f,
            center = pEnd
        )
        drawCircle(
            color = Color(0xFFFFD700).copy(alpha = alpha * 0.9f),
            radius = 7f,
            center = pEnd,
            style = Stroke(width = 2f)
        )
    } else {
        // Standard laser cut glow along the slice seam
        drawLine(
            brush = Brush.linearGradient(
                listOf(
                    Color.White.copy(alpha = alpha * 0.95f),
                    Color(category.glowColor).copy(alpha = alpha * 0.75f)
                )
            ),
            start = pStart,
            end = pEnd,
            strokeWidth = 3.5f
        )
    }
}

private fun DrawScope.drawSliceTrail(
    points: List<SliceTrailPoint>,
    isFreezeActive: Boolean = false,
    isCombo4xActive: Boolean = false
) {
    if (points.size < 2) return

    val now = System.nanoTime()
    val maxAgeNanos = when {
        isCombo4xActive -> 460_000_000L
        isFreezeActive -> 350_000_000L
        else -> 220_000_000L
    }

    // Determine color palette based on active state:
    // Combo 4x: Legendary Solar Magma & Dragon Fire blade with radiant gold highlights
    // Freeze: Glacial cyber cyan & diamond aurora
    // Normal: Vibrant crimson katana with gold corona
    val outerColor = when {
        isCombo4xActive -> Color(0xFFFF1E00)
        isFreezeActive -> Color(0xFF00E5FF)
        else -> Color(0xFFFF3B30)
    }
    val midColor = when {
        isCombo4xActive -> Color(0xFFFF9D00)
        isFreezeActive -> Color(0xFF80D8FF)
        else -> Color(0xFFFF9800)
    }
    val innerColor = when {
        isCombo4xActive -> Color(0xFFFFEA00)
        isFreezeActive -> Color(0xFFE0F7FA)
        else -> Color(0xFFFFF59D)
    }

    // Multi-tier stroke widths (wider for Combo 4x and Freeze)
    val baseOuterWidth = when {
        isCombo4xActive -> 64f
        isFreezeActive -> 40f
        else -> 30f
    }
    val baseMidWidth = when {
        isCombo4xActive -> 34f
        isFreezeActive -> 20f
        else -> 14f
    }
    val baseInnerWidth = when {
        isCombo4xActive -> 16f
        isFreezeActive -> 10f
        else -> 7f
    }
    val baseCoreWidth = when {
        isCombo4xActive -> 8f
        isFreezeActive -> 5.5f
        else -> 4f
    }

    val pointCount = points.size
    for (i in 0 until pointCount - 1) {
        val p1 = points[i]
        val p2 = points[i + 1]

        val age = (now - p1.timestampNanos).coerceAtLeast(0L)
        val progress = 1.0f - (age.toFloat() / maxAgeNanos).coerceIn(0f, 1f)
        if (progress <= 0f) continue

        // Katana tapering profile along the stroke (crescent blade dynamics)
        val indexFraction = (i + 1).toFloat() / (pointCount - 1).coerceAtLeast(1)
        val taper = when {
            indexFraction > 0.85f -> 0.45f + (1f - indexFraction) * 3.6f
            else -> 0.2f + 0.8f * (indexFraction / 0.85f)
        }.coerceIn(0.15f, 1.0f)

        val strokeScale = progress * taper

        // COMBO 4X BONUS: Dancing flame envelope wave along the cutting edge
        if (isCombo4xActive) {
            val wavePhase = (now / 70_000_000L).toFloat() + i * 0.9f
            val flameOffset = kotlin.math.sin(wavePhase.toDouble()).toFloat() * 7f * progress
            drawLine(
                color = Color(0xFFFF3D00).copy(alpha = 0.45f * progress),
                start = Offset(p1.x + flameOffset, p1.y - flameOffset * 0.4f),
                end = Offset(p2.x + flameOffset, p2.y - flameOffset * 0.4f),
                strokeWidth = baseOuterWidth * 1.25f * strokeScale,
                cap = StrokeCap.Round
            )
        }

        // 1. Broad outer atmospheric bloom aura
        drawLine(
            color = outerColor.copy(alpha = (if (isCombo4xActive) 0.72f else if (isFreezeActive) 0.60f else 0.45f) * progress),
            start = Offset(p1.x, p1.y),
            end = Offset(p2.x, p2.y),
            strokeWidth = baseOuterWidth * strokeScale,
            cap = StrokeCap.Round
        )

        // 2. High-saturation energy corona streak
        drawLine(
            color = midColor.copy(alpha = (if (isCombo4xActive) 0.98f else 0.90f) * progress),
            start = Offset(p1.x, p1.y),
            end = Offset(p2.x, p2.y),
            strokeWidth = baseMidWidth * strokeScale,
            cap = StrokeCap.Round
        )

        // 3. Radiant inner incandescent blade body
        drawLine(
            color = innerColor.copy(alpha = 0.98f * progress),
            start = Offset(p1.x, p1.y),
            end = Offset(p2.x, p2.y),
            strokeWidth = baseInnerWidth * strokeScale,
            cap = StrokeCap.Round
        )

        // 4. Razor-sharp blazing white diamond core spine
        drawLine(
            color = Color.White.copy(alpha = 1.0f * progress),
            start = Offset(p1.x, p1.y),
            end = Offset(p2.x, p2.y),
            strokeWidth = baseCoreWidth * strokeScale,
            cap = StrokeCap.Round
        )

        // 5. Tactile micro slash sparks & rising fire embers along dynamic fast segments
        val dx = p2.x - p1.x
        val dy = p2.y - p1.y
        val distSq = dx * dx + dy * dy
        if (distSq > 80f) {
            val len = kotlin.math.sqrt(distSq).coerceAtLeast(1f)
            val perpX = -dy / len
            val perpY = dx / len
            val side = if (i % 2 == 0) 1f else -1f
            val sparkOffset = side * (baseMidWidth * 0.6f + (i % 4) * 3f)
            val sparkX = (p1.x + p2.x) / 2f + perpX * sparkOffset
            val sparkY = (p1.y + p2.y) / 2f + perpY * sparkOffset

            drawCircle(
                color = if (isCombo4xActive) Color(0xFFFFD700) else innerColor,
                radius = (if (isCombo4xActive) 4.2f else 2.5f) * progress,
                center = Offset(sparkX, sparkY)
            )

            // In Combo 4x mode, add secondary flying fire embers rising upwards
            if (isCombo4xActive && (i % 2 == 0)) {
                val riseDist = (1f - progress) * 28f
                val emberX = (p1.x + p2.x) / 2f - perpX * (sparkOffset * 0.8f)
                val emberY = (p1.y + p2.y) / 2f - riseDist
                drawCircle(
                    color = if (i % 4 == 0) Color(0xFFFF3D00) else Color(0xFFFFAB00),
                    radius = (2.6f + (i % 3) * 1.2f) * progress,
                    center = Offset(emberX, emberY)
                )
            }
        }
    }

    // 6. Brilliant razor katana glint & star flare at the cutting tip (player's finger touch position)
    val tip = points.last()
    val tipAge = (now - tip.timestampNanos).coerceAtLeast(0L)
    val tipProgress = 1.0f - (tipAge.toFloat() / maxAgeNanos).coerceIn(0f, 1f)
    if (tipProgress > 0.08f) {
        val glintRadius = if (isCombo4xActive) 38f else 18f
        val flareColor = if (isCombo4xActive) Color(0xFFFFD700) else outerColor

        // In Combo 4x: Expanding shockwave ring at blade tip
        if (isCombo4xActive) {
            drawCircle(
                color = Color(0xFFFF5722).copy(alpha = 0.45f * tipProgress),
                radius = glintRadius * 1.35f * tipProgress,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.5f)
            )
        }

        // Outer glow halo
        drawCircle(
            color = flareColor.copy(alpha = (if (isCombo4xActive) 0.68f else 0.55f) * tipProgress),
            radius = glintRadius * tipProgress,
            center = Offset(tip.x, tip.y)
        )

        // Diamond star flare cross lines (4-point star)
        val starArm = glintRadius * tipProgress
        // Horizontal arm
        drawLine(
            color = Color.White.copy(alpha = 0.98f * tipProgress),
            start = Offset(tip.x - starArm, tip.y),
            end = Offset(tip.x + starArm, tip.y),
            strokeWidth = if (isCombo4xActive) 4.2f else 2.2f,
            cap = StrokeCap.Round
        )
        // Vertical arm
        drawLine(
            color = Color.White.copy(alpha = 0.98f * tipProgress),
            start = Offset(tip.x, tip.y - starArm),
            end = Offset(tip.x, tip.y + starArm),
            strokeWidth = if (isCombo4xActive) 4.2f else 2.2f,
            cap = StrokeCap.Round
        )

        // Diagonal cross glints (Combo 4x has extra bright full 8-point sunburst)
        val diagArm = starArm * if (isCombo4xActive) 0.72f else 0.55f
        drawLine(
            color = (if (isCombo4xActive) Color(0xFFFFF9C4) else innerColor).copy(alpha = 0.92f * tipProgress),
            start = Offset(tip.x - diagArm, tip.y - diagArm),
            end = Offset(tip.x + diagArm, tip.y + diagArm),
            strokeWidth = if (isCombo4xActive) 2.6f else 1.6f,
            cap = StrokeCap.Round
        )
        drawLine(
            color = (if (isCombo4xActive) Color(0xFFFFF9C4) else innerColor).copy(alpha = 0.92f * tipProgress),
            start = Offset(tip.x - diagArm, tip.y + diagArm),
            end = Offset(tip.x + diagArm, tip.y - diagArm),
            strokeWidth = if (isCombo4xActive) 2.6f else 1.6f,
            cap = StrokeCap.Round
        )

        // Intense pure white core hot-spot
        drawCircle(
            color = Color.White,
            radius = (if (isCombo4xActive) 8.5f else 4.5f) * tipProgress,
            center = Offset(tip.x, tip.y)
        )
    }
}

/**
 * Data class representing persistent slice cuts etched during the freeze bonus corruptor round.
 */
private data class FreezeSlash(
    val x1: Float,
    val y1: Float,
    val x2: Float,
    val y2: Float,
    val auraColor: Color = Color(0xFF00E5FF),
    val midColor: Color = Color(0xFFFFD700)
)

/**
 * Renders lingering freeze bonus slice cuts across the screen that stay visible until the freeze mode timer finishes.
 */
private fun DrawScope.drawFreezeSlices(slices: List<FreezeSlash>) {
    for (slice in slices) {
        // 1. Large electric cyan / fiery neon outer aura
        drawLine(
            color = slice.auraColor.copy(alpha = 0.60f),
            start = Offset(slice.x1, slice.y1),
            end = Offset(slice.x2, slice.y2),
            strokeWidth = 36f,
            cap = StrokeCap.Round
        )

        // 2. Vibrant golden-orange mid laser slash
        drawLine(
            color = slice.midColor.copy(alpha = 0.88f),
            start = Offset(slice.x1, slice.y1),
            end = Offset(slice.x2, slice.y2),
            strokeWidth = 18f,
            cap = StrokeCap.Round
        )

        // 3. Piercing blazing white blade core
        drawLine(
            color = Color.White,
            start = Offset(slice.x1, slice.y1),
            end = Offset(slice.x2, slice.y2),
            strokeWidth = 8f,
            cap = StrokeCap.Round
        )
    }
}

private fun DrawScope.drawFloatingPopup(popup: FloatingPopup, paint: Paint) {
    if (popup.alpha <= 0f) return

    paint.color = Color(popup.color).copy(alpha = popup.alpha).hashCode()
    paint.textSize = 28f * popup.scale

    drawContext.canvas.nativeCanvas.drawText(
        popup.text,
        popup.x,
        popup.y,
        paint
    )
}

// =========================================================================
// TOP HUD COMPOSABLE
// =========================================================================

@Composable
private fun TopHudBar(
    levelNumber: Int,
    score: Int,
    lives: Int,
    baseLives: Int,
    shieldBonusLives: Int,
    comboMultiplier: Int,
    elapsedSeconds: Float,
    currentLanguage: String = "en",
    isAudioMuted: Boolean,
    onToggleAudioMute: () -> Unit,
    isPaused: Boolean,
    onTogglePause: () -> Unit,
    modifier: Modifier = Modifier
) {
    val combo4xBadgeText = when {
        currentLanguage.equals("ja", ignoreCase = true) -> "コンボ X4"
        currentLanguage.equals("in", ignoreCase = true) || currentLanguage.equals("id", ignoreCase = true) -> "KOMBO X4"
        else -> "COMBO X4"
    }
    // High-contrast floating HUD bar matching Screen 2
    Box(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Close (X) button & Mute toggle
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Exit / Close button (X) inside dark glass circle
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color(0x66000000))
                        .border(1.2.dp, Color(0x55FFFFFF), CircleShape)
                        .clickable(onClick = onTogglePause)
                        .testTag("game_back_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.Close,
                        contentDescription = "Exit / Pause",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Volume Mute Toggle
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color(0x66000000))
                        .border(
                            1.2.dp,
                            if (isAudioMuted) Color(0x88FF5252) else Color(0x884FCB8F),
                            CircleShape
                        )
                        .clickable(onClick = onToggleAudioMute)
                        .testTag("hud_mute_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isAudioMuted) androidx.compose.material.icons.Icons.Default.VolumeOff else androidx.compose.material.icons.Icons.Default.VolumeUp,
                        contentDescription = "Mute",
                        tint = if (isAudioMuted) Color(0xFFFF5252) else MintSuccess,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Center: Big Score, 3 Glowing Hearts & Combo Pill
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Large Score
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0x55000000),
                    border = BorderStroke(1.dp, Color(0x33FFD54F))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = String.format("%,d", score),
                            style = TextStyle(
                                color = GoldSecondary,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp,
                                shadow = Shadow(
                                    color = Color(0xCC000000),
                                    offset = Offset(2f, 2f),
                                    blurRadius = 6f
                                )
                            ),
                            modifier = Modifier.testTag("game_score_display")
                        )
                    }
                }

                // Hearts display: 3 base red hearts + 1 extra blue shield heart (max 4)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.testTag("game_lives_container")
                ) {
                    for (i in 1..3) {
                        val isHeartActive = i <= baseLives
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isHeartActive) Color(0x33FF3B30) else Color(0x22000000)
                                )
                                .border(
                                    1.2.dp,
                                    if (isHeartActive) Color(0xFFFF3B30) else Color(0x44FFFFFF),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isHeartActive) androidx.compose.material.icons.Icons.Default.Favorite else androidx.compose.material.icons.Icons.Default.FavoriteBorder,
                                contentDescription = "Life $i",
                                tint = if (isHeartActive) Color(0xFFFF3B30) else Color(0x44FFFFFF),
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }

                    // 4th Bonus Shield Heart (Blue): granted when player collects "Compliance Shield" while having 3 full red lives (max 4)
                    AnimatedVisibility(
                        visible = shieldBonusLives > 0,
                        enter = fadeIn() + scaleIn(),
                        exit = fadeOut() + scaleOut()
                    ) {
                        val blueHeartColor = Color(0xFF00E5FF)
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color(0x3300E5FF))
                                .border(
                                    1.2.dp,
                                    blueHeartColor,
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = androidx.compose.material.icons.Icons.Default.Favorite,
                                contentDescription = "Compliance Shield Bonus Life",
                                tint = blueHeartColor,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }

                // Combo Badge next to lives:
                // - Shows "X2" when comboMultiplier == 2
                // - Shows "X3" when comboMultiplier == 3
                // - Shows "COMBO X4" ONLY when player reaches comboMultiplier >= 4
                if (comboMultiplier == 2) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xEEFF8F00),
                        border = BorderStroke(1.2.dp, Color(0xFFFFD54F)),
                        shadowElevation = 4.dp,
                        modifier = Modifier.testTag("game_combo_badge_x2")
                    ) {
                        Box(
                            modifier = Modifier
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFFE65100), Color(0xFFFF9800))
                                    )
                                )
                                .padding(horizontal = 7.dp, vertical = 2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "X2",
                                style = TextStyle(
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.5.sp,
                                    shadow = Shadow(
                                        color = Color(0xCC000000),
                                        offset = Offset(1f, 1f),
                                        blurRadius = 3f
                                    )
                                ),
                                modifier = Modifier.testTag("game_combo_badge")
                            )
                        }
                    }
                } else if (comboMultiplier == 3) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xDDF4511E),
                        border = BorderStroke(1.2.dp, Color(0xFFFFAB91)),
                        shadowElevation = 4.dp,
                        modifier = Modifier.testTag("game_combo_badge_x3")
                    ) {
                        Box(
                            modifier = Modifier
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFFBF360C), Color(0xFFFF5722))
                                    )
                                )
                                .padding(horizontal = 7.dp, vertical = 2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "X3",
                                style = TextStyle(
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.5.sp,
                                    shadow = Shadow(
                                        color = Color(0xCC000000),
                                        offset = Offset(1f, 1f),
                                        blurRadius = 3f
                                    )
                                ),
                                modifier = Modifier.testTag("game_combo_badge")
                            )
                        }
                    }
                } else if (comboMultiplier >= 4) {
                    val infiniteTransition = rememberInfiniteTransition(label = "combo_pulse")
                    val pulseScale by infiniteTransition.animateFloat(
                        initialValue = 1.0f,
                        targetValue = 1.08f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(450, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "combo4x_pulse"
                    )

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xEE8A0000),
                        border = BorderStroke(
                            1.5.dp,
                            Brush.horizontalGradient(
                                listOf(Color(0xFFFFD700), Color(0xFFFF5722), Color(0xFFFFD700))
                            )
                        ),
                        shadowElevation = 8.dp,
                        modifier = Modifier
                            .scale(pulseScale)
                            .shadow(10.dp, RoundedCornerShape(16.dp), spotColor = Color(0xFFFF3D00))
                    ) {
                        Box(
                            modifier = Modifier
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(
                                            Color(0xFFC62828),
                                            Color(0xFFE65100),
                                            Color(0xFFFF8F00)
                                        )
                                    )
                                )
                                .padding(horizontal = 9.dp, vertical = 3.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "🔥",
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = combo4xBadgeText,
                                    style = TextStyle(
                                        color = Color(0xFFFFFDE7),
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 0.8.sp,
                                        shadow = Shadow(
                                            color = Color(0xDD2A0000),
                                            offset = Offset(2f, 2f),
                                            blurRadius = 4f
                                        )
                                    ),
                                    modifier = Modifier.testTag("game_combo_badge")
                                )
                            }
                        }
                    }
                }
            }

            // Right: Digital Red Timer Capsule & Pause button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Digital Red Timer
                val secondsInt = elapsedSeconds.toInt().coerceAtLeast(0)
                val minutes = secondsInt / 60
                val secs = secondsInt % 60
                val timeString = String.format("%02d:%02d", minutes, secs)

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0x66180508),
                    border = BorderStroke(1.2.dp, Color(0x88FF3B30))
                ) {
                    Row(
                        modifier = Modifier
                            .height(32.dp)
                            .padding(horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.Timer,
                            contentDescription = null,
                            tint = Color(0xFFFF5252),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = timeString,
                            style = TextStyle(
                                color = Color(0xFFFF3B30),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp,
                                shadow = Shadow(
                                    color = Color(0xFFFF3B30),
                                    offset = Offset(0f, 0f),
                                    blurRadius = 8f
                                )
                            ),
                            modifier = Modifier.testTag("game_timer_display")
                        )
                    }
                }

                // Pause toggle
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color(0x66000000))
                        .border(1.2.dp, Color(0x8864B5F6), CircleShape)
                        .clickable(onClick = onTogglePause)
                        .testTag("hud_pause_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPaused) androidx.compose.material.icons.Icons.Default.PlayArrow else androidx.compose.material.icons.Icons.Default.Pause,
                        contentDescription = "Pause",
                        tint = Color(0xFF90CAF9),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

// =========================================================================
// CATEGORY PAINTERS CACHE
// =========================================================================

@Composable
private fun rememberCategoryPainters(): Map<ComplianceCategory, Painter> {
    val painters = mutableMapOf<ComplianceCategory, Painter>()
    for (category in ComplianceCategory.entries) {
        painters[category] = painterResource(id = category.iconRes)
    }
    return painters
}

// =========================================================================
// COMBO 4X BURST EFFECT (LARGE BOLD 3D GOLD TEXT + RADIANT SOLAR AURA)
// =========================================================================

@Composable
private fun FruitNinjaComboOverlay(
    burst: com.example.ui.viewmodel.FruitNinjaComboBurst,
    currentLanguage: String = "en",
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scaleAnim = remember { Animatable(0.2f) }
    val rotationAnim = remember { Animatable(-7f) }
    val alphaAnim = remember { Animatable(0f) }
    val floatYAnim = remember { Animatable(0f) }
    val auraScaleAnim = remember { Animatable(0.2f) }

    LaunchedEffect(burst.id) {
        scaleAnim.snapTo(0.2f)
        rotationAnim.snapTo(-7f)
        alphaAnim.snapTo(0f)
        floatYAnim.snapTo(0f)
        auraScaleAnim.snapTo(0.2f)

        // Stage 1: Explosive punchy bounce entrance (0 - 200ms)
        launch {
            alphaAnim.animateTo(1f, tween(90))
        }
        launch {
            auraScaleAnim.animateTo(
                targetValue = 1.25f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
            auraScaleAnim.animateTo(1.0f, tween(150))
        }
        launch {
            rotationAnim.animateTo(
                targetValue = -1.5f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
        }
        launch {
            scaleAnim.animateTo(
                targetValue = 1.25f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
            scaleAnim.animateTo(1.0f, tween(140))
        }

        // Gentle upward floating drift over hold period
        launch {
            floatYAnim.animateTo(-34f, tween(1300, easing = LinearOutSlowInEasing))
        }

        // Hold visible for player
        delay(1150)

        // Stage 2: Smooth exit fade-out & slight expand (1150ms - 1450ms)
        launch {
            scaleAnim.animateTo(1.18f, tween(300, easing = FastOutSlowInEasing))
        }
        launch {
            alphaAnim.animateTo(0f, tween(280))
        }
        delay(300)
        onDismiss()
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val density = LocalDensity.current
        val screenWidthPx = constraints.maxWidth.toFloat()
        val screenHeightPx = constraints.maxHeight.toFloat()

        // Clamp coordinates safely within screen boundaries so text is never clipped
        val marginXPx = with(density) { 170.dp.toPx() }
        val marginYPx = with(density) { 130.dp.toPx() }

        val posX = if (burst.x > 0f) {
            burst.x.coerceIn(marginXPx, (screenWidthPx - marginXPx).coerceAtLeast(marginXPx))
        } else {
            screenWidthPx / 2f
        }
        val posY = if (burst.y > 0f) {
            (burst.y - 15f).coerceIn(marginYPx, (screenHeightPx - marginYPx).coerceAtLeast(marginYPx))
        } else {
            screenHeightPx * 0.38f
        }

        val posXdp = with(density) { posX.toDp() }
        val posYdp = with(density) { posY.toDp() }

        Box(
            modifier = Modifier
                .offset(
                    x = posXdp - 170.dp,
                    y = posYdp - 65.dp + floatYAnim.value.dp
                )
                .size(340.dp, 130.dp)
                .graphicsLayer {
                    scaleX = scaleAnim.value
                    scaleY = scaleAnim.value
                    rotationZ = rotationAnim.value
                    alpha = alphaAnim.value
                },
            contentAlignment = Alignment.Center
        ) {
            // 1. Radiant Solar Sunburst Aura behind the text
            Combo4xSunburstAura(
                modifier = Modifier.fillMaxSize(),
                auraScale = auraScaleAnim.value
            )

            // 2. Big, Bold, Clean "COMBO X4" 3D Arcade Text (Localized for EN, ID/IN, JA)
            val displayText = when {
                currentLanguage.equals("ja", ignoreCase = true) -> "コンボ X4"
                currentLanguage.equals("in", ignoreCase = true) || currentLanguage.equals("id", ignoreCase = true) -> "KOMBO X4"
                else -> "COMBO X4"
            }
            FruitNinja3DText(
                text = displayText,
                fontSize = if (currentLanguage.equals("ja", ignoreCase = true)) 50.sp else 58.sp,
                letterSpacing = 2.sp
            )
        }
    }
}

@Composable
private fun Combo4xSunburstAura(
    modifier: Modifier = Modifier,
    auraScale: Float = 1f
) {
    Canvas(modifier = modifier) {
        val centerX = size.width / 2f
        val centerY = size.height / 2f
        val radius = (size.minDimension * 0.48f * auraScale).coerceAtLeast(10f)

        // 1. Soft radiant magma aura
        drawCircle(
            brush = Brush.radialGradient(
                listOf(
                    Color(0xD8FF6D00),
                    Color(0x88FFD700),
                    Color(0x22FFAB00),
                    Color.Transparent
                ),
                center = Offset(centerX, centerY),
                radius = radius
            ),
            radius = radius,
            center = Offset(centerX, centerY)
        )

        // 2. Starburst radiant sun rays
        val rayCount = 12
        for (i in 0 until rayCount) {
            val angle = (i * (360f / rayCount)) * (Math.PI / 180f)
            val rayLen = radius * (1.12f + (i % 3) * 0.22f)
            val rayEnd = Offset(
                centerX + (kotlin.math.cos(angle) * rayLen).toFloat(),
                centerY + (kotlin.math.sin(angle) * rayLen).toFloat()
            )
            drawLine(
                color = Color(0xFFFFD700).copy(alpha = 0.50f),
                start = Offset(centerX, centerY),
                end = rayEnd,
                strokeWidth = 3.2f,
                cap = StrokeCap.Round
            )
        }
    }
}

@Composable
private fun FruitNinja3DText(
    text: String,
    fontSize: androidx.compose.ui.unit.TextUnit,
    letterSpacing: androidx.compose.ui.unit.TextUnit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // 1. Deep bottom drop shadow (distance 6dp)
        Text(
            text = text,
            fontSize = fontSize,
            fontWeight = FontWeight.Black,
            letterSpacing = letterSpacing,
            color = Color(0xFF1E0A00),
            modifier = Modifier.offset(x = 1.dp, y = 6.dp)
        )

        // 2. Multi-tier solid 3D extrusion walls (5dp, 4dp, 3dp, 2dp, 1dp)
        Text(
            text = text,
            fontSize = fontSize,
            fontWeight = FontWeight.Black,
            letterSpacing = letterSpacing,
            color = Color(0xFF2E1100),
            modifier = Modifier.offset(y = 5.dp)
        )
        Text(
            text = text,
            fontSize = fontSize,
            fontWeight = FontWeight.Black,
            letterSpacing = letterSpacing,
            color = Color(0xFF421A00),
            modifier = Modifier.offset(y = 4.dp)
        )
        Text(
            text = text,
            fontSize = fontSize,
            fontWeight = FontWeight.Black,
            letterSpacing = letterSpacing,
            color = Color(0xFF592400),
            modifier = Modifier.offset(y = 3.dp)
        )
        Text(
            text = text,
            fontSize = fontSize,
            fontWeight = FontWeight.Black,
            letterSpacing = letterSpacing,
            color = Color(0xFF733000),
            modifier = Modifier.offset(y = 2.dp)
        )
        Text(
            text = text,
            fontSize = fontSize,
            fontWeight = FontWeight.Black,
            letterSpacing = letterSpacing,
            color = Color(0xFF8F3E00),
            modifier = Modifier.offset(y = 1.dp)
        )

        // 3. Dark outline rim surrounding the face (8 directions)
        listOf(
            Offset(-1.5f, 0f), Offset(1.5f, 0f), Offset(0f, -1.5f), Offset(0f, 1.5f),
            Offset(-1.2f, -1.2f), Offset(1.2f, -1.2f), Offset(-1.2f, 1.2f), Offset(1.2f, 1.2f)
        ).forEach { off ->
            Text(
                text = text,
                fontSize = fontSize,
                fontWeight = FontWeight.Black,
                letterSpacing = letterSpacing,
                color = Color(0xFF441C00),
                modifier = Modifier.offset(x = off.x.dp, y = off.y.dp)
            )
        }

        // 4. Bright Golden Face with Vertical Gradient & Top Chamfer Highlight
        Text(
            text = text,
            fontSize = fontSize,
            fontWeight = FontWeight.Black,
            letterSpacing = letterSpacing,
            style = TextStyle(
                brush = Brush.verticalGradient(
                    listOf(
                        Color(0xFFFFFFD6), // pure lemon-white specular highlight
                        Color(0xFFFFF176), // sunny bright gold
                        Color(0xFFFFD500), // rich golden yellow
                        Color(0xFFFFAB00), // warm amber gold
                        Color(0xFFFF6D00)  // deep burnt-orange base
                    )
                ),
                shadow = Shadow(
                    color = Color(0xCCFFFFFF),
                    offset = Offset(0f, -1.5f),
                    blurRadius = 2.5f
                )
            )
        )
    }
}
