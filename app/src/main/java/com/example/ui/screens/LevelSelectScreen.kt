package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.draw.shadow
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.GameDifficulty
import com.example.data.LevelConfig
import com.example.data.local.UserStats
import com.example.ui.theme.CoralPrimary
import com.example.ui.theme.GoldSecondary
import com.example.ui.theme.MintSuccess
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.hypot
import kotlin.math.roundToInt

@Composable
fun LevelSelectScreen(
    userStats: UserStats?,
    onStartLevel: (LevelConfig, GameDifficulty) -> Unit,
    onOpenGlossary: () -> Unit,
    onBackToMenu: () -> Unit,
    modifier: Modifier = Modifier,
    initialDifficulty: GameDifficulty = GameDifficulty.NORMAL,
    currentLanguage: String = "en"
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Determine initial selected level (highest unlocked level)
    val defaultLevelNumber = remember(userStats) {
        val highest = (1..4).lastOrNull { userStats?.isLevelUnlocked(it) ?: (it == 1) } ?: 1
        highest
    }
    var selectedLevelNumber by remember { mutableIntStateOf(defaultLevelNumber) }
    var selectedDifficulty by remember { mutableStateOf(initialDifficulty) }
    var lockedFeedbackMessage by remember { mutableStateOf<String?>(null) }

    val shakeOffset = remember { Animatable(0f) }
    val sliceTrail = remember { mutableStateListOf<Offset>() }
    val nodeBounds = remember { mutableMapOf<Int, Rect>() }
    var startButtonBounds by remember { mutableStateOf<Rect?>(null) }
    var hasTriggeredSlice by remember { mutableStateOf(false) }

    val selectedLevel = remember(selectedLevelNumber) {
        LevelConfig.getByLevel(selectedLevelNumber)
    }

    val totalStarsCollected = remember(userStats) {
        (1..4).sumOf { userStats?.getStarsForLevel(it) ?: 0 }
    }

    fun triggerShakeFeedback(message: String) {
        lockedFeedbackMessage = message
        scope.launch {
            shakeOffset.snapTo(0f)
            shakeOffset.animateTo(10f, animationSpec = tween(40))
            shakeOffset.animateTo(-10f, animationSpec = tween(40))
            shakeOffset.animateTo(7f, animationSpec = tween(40))
            shakeOffset.animateTo(-7f, animationSpec = tween(40))
            shakeOffset.animateTo(0f, animationSpec = tween(40))
            delay(1600)
            lockedFeedbackMessage = null
        }
    }

    fun getUnlockMessage(lvlNum: Int): String {
        val prevLevelNum = lvlNum - 1
        val prevBest = userStats?.getBestForLevel(prevLevelNum) ?: 0
        return when (lvlNum) {
            2 -> when (currentLanguage.lowercase()) {
                "ja" -> "セクター1で1,200pt以上必要です" + if (prevBest > 0) " (スコア: $prevBest/1200)" else ""
                "in", "id" -> "Selesaikan Sektor 1 dengan skor minimal 1.200" + if (prevBest > 0) " (Skor: $prevBest/1200)" else ""
                else -> "Complete Sector 1 with at least 1,200 points" + if (prevBest > 0) " (Score: $prevBest/1200)" else ""
            }
            3 -> when (currentLanguage.lowercase()) {
                "ja" -> "セクター2で2,500pt以上必要です" + if (prevBest > 0) " (スコア: $prevBest/2500)" else ""
                "in", "id" -> "Selesaikan Sektor 2 dengan skor minimal 2.500" + if (prevBest > 0) " (Skor: $prevBest/2500)" else ""
                else -> "Complete Sector 2 with at least 2,500 points" + if (prevBest > 0) " (Score: $prevBest/2500)" else ""
            }
            4 -> when (currentLanguage.lowercase()) {
                "ja" -> "セクター3で5,000pt以上必要です" + if (prevBest > 0) " (スコア: $prevBest/5000)" else ""
                "in", "id" -> "Selesaikan Sektor 3 dengan skor minimal 5.000" + if (prevBest > 0) " (Skor: $prevBest/5000)" else ""
                else -> "Complete Sector 3 with at least 5,000 points" + if (prevBest > 0) " (Score: $prevBest/5000)" else ""
            }
            else -> when (currentLanguage.lowercase()) {
                "ja" -> "セクター ${prevLevelNum} をクリアしてアンロック"
                "in", "id" -> "Selesaikan Misi ${prevLevelNum} untuk membuka"
                else -> "Complete Mission ${prevLevelNum} to unlock"
            }
        }
    }

    fun checkSliceHit(p1: Offset, p2: Offset) {
        if (hasTriggeredSlice) return
        val length = hypot(p2.x - p1.x, p2.y - p1.y)
        if (length < 30f) return

        // 1. Check if slice hits the tactical mission "Start" button
        startButtonBounds?.let { btnRect ->
            if (lineIntersectsRect(p1, p2, btnRect)) {
                val isUnlocked = userStats?.isLevelUnlocked(selectedLevel.levelNumber) ?: (selectedLevel.levelNumber == 1)
                if (isUnlocked) {
                    hasTriggeredSlice = true
                    scope.launch {
                        delay(120)
                        onStartLevel(selectedLevel, selectedDifficulty)
                    }
                } else {
                    triggerShakeFeedback(getUnlockMessage(selectedLevel.levelNumber))
                }
                return
            }
        }

        // 2. Check if slice hits map campaign level nodes
        for ((levelNum, rect) in nodeBounds) {
            if (lineIntersectsRect(p1, p2, rect)) {
                val lvl = LevelConfig.getByLevel(levelNum)
                val isUnlocked = userStats?.isLevelUnlocked(levelNum) ?: (levelNum == 1)
                if (isUnlocked) {
                    selectedLevelNumber = levelNum
                    hasTriggeredSlice = true
                    scope.launch {
                        delay(120)
                        onStartLevel(lvl, selectedDifficulty)
                    }
                } else {
                    triggerShakeFeedback(getUnlockMessage(levelNum))
                }
                return
            }
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        sliceTrail.clear()
                        sliceTrail.add(offset)
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        val currentPos = change.position
                        if (sliceTrail.isNotEmpty()) {
                            val lastPos = sliceTrail.last()
                            checkSliceHit(lastPos, currentPos)
                        }
                        sliceTrail.add(currentPos)
                        if (sliceTrail.size > 14) {
                            sliceTrail.removeAt(0)
                        }
                    },
                    onDragEnd = {
                        scope.launch {
                            delay(100)
                            sliceTrail.clear()
                        }
                    },
                    onDragCancel = {
                        sliceTrail.clear()
                    }
                )
            }
    ) {
        val isTabletLandscape = maxWidth > 650.dp
        val currentMission = LevelConfig.getByLevel(selectedLevelNumber)

        // Fullscreen atmospheric game artwork (dynamic per mission matching gameplay)
        Image(
            painter = painterResource(id = currentMission.backgroundRes),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Vignette & gradient lighting overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xEE09131F),
                            Color(0xCC0F1E32),
                            Color(0xF6070D16)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .offset { IntOffset(shakeOffset.value.roundToInt(), 0) }
        ) {
            // Top Navigation Bar
            CampaignTopBar(
                totalStars = totalStarsCollected,
                currentLanguage = currentLanguage,
                onBack = onBackToMenu,
                onOpenGlossary = onOpenGlossary
            )

            // Locked feedback toast
            if (lockedFeedbackMessage != null) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xEEFF5252)
                ) {
                    Text(
                        text = lockedFeedbackMessage ?: "",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 6.dp, horizontal = 12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            if (isTabletLandscape) {
                // Landscape Tablet Layout: Map on left, Briefing Dossier on right
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1.1f)
                            .fillMaxHeight()
                    ) {
                        CampaignRoadmapView(
                            levels = LevelConfig.ALL_LEVELS,
                            selectedLevelNumber = selectedLevelNumber,
                            userStats = userStats,
                            onSelectLevel = { lvl ->
                                val isUnlocked = userStats?.isLevelUnlocked(lvl.levelNumber) ?: (lvl.levelNumber == 1)
                                if (isUnlocked) {
                                    selectedLevelNumber = lvl.levelNumber
                                } else {
                                    triggerShakeFeedback(getUnlockMessage(lvl.levelNumber))
                                }
                            },
                            onPositionNode = { lvlNum, rect ->
                                nodeBounds[lvlNum] = rect
                            }
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    ) {
                        MissionDeploymentConsole(
                            level = selectedLevel,
                            userStats = userStats,
                            selectedDifficulty = selectedDifficulty,
                            currentLanguage = currentLanguage,
                            onDifficultyChange = { selectedDifficulty = it },
                            onStartButtonPositioned = { startButtonBounds = it },
                            onDeploy = {
                                val isUnlocked = userStats?.isLevelUnlocked(selectedLevel.levelNumber) ?: (selectedLevel.levelNumber == 1)
                                if (isUnlocked) {
                                    onStartLevel(selectedLevel, selectedDifficulty)
                                } else {
                                    triggerShakeFeedback(getUnlockMessage(selectedLevel.levelNumber))
                                }
                            }
                        )
                    }
                }
            } else {
                // Portrait Mobile Layout: Winding Map Track on top, Mission Briefing Hub below
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    // Campaign Map Road
                    Box(
                        modifier = Modifier
                            .weight(1.1f)
                            .fillMaxWidth()
                    ) {
                        CampaignRoadmapView(
                            levels = LevelConfig.ALL_LEVELS,
                            selectedLevelNumber = selectedLevelNumber,
                            userStats = userStats,
                            onSelectLevel = { lvl ->
                                val isUnlocked = userStats?.isLevelUnlocked(lvl.levelNumber) ?: (lvl.levelNumber == 1)
                                if (isUnlocked) {
                                    selectedLevelNumber = lvl.levelNumber
                                } else {
                                    triggerShakeFeedback(getUnlockMessage(lvl.levelNumber))
                                }
                            },
                            onPositionNode = { lvlNum, rect ->
                                nodeBounds[lvlNum] = rect
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Mission Briefing & Deployment Console
                    MissionDeploymentConsole(
                        level = selectedLevel,
                        userStats = userStats,
                        selectedDifficulty = selectedDifficulty,
                        currentLanguage = currentLanguage,
                        onDifficultyChange = { selectedDifficulty = it },
                        onStartButtonPositioned = { startButtonBounds = it },
                        onDeploy = {
                            val isUnlocked = userStats?.isLevelUnlocked(selectedLevel.levelNumber) ?: (selectedLevel.levelNumber == 1)
                            if (isUnlocked) {
                                onStartLevel(selectedLevel, selectedDifficulty)
                            } else {
                                triggerShakeFeedback(getUnlockMessage(selectedLevel.levelNumber))
                            }
                        }
                    )
                }
            }
        }

        // Draw Interactive Ninja Slice Trail
        if (sliceTrail.size >= 2) {
            androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                val path = Path().apply {
                    moveTo(sliceTrail.first().x, sliceTrail.first().y)
                    for (i in 1 until sliceTrail.size) {
                        lineTo(sliceTrail[i].x, sliceTrail[i].y)
                    }
                }
                drawPath(
                    path = path,
                    color = CoralPrimary.copy(alpha = 0.55f),
                    style = Stroke(width = 12f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
                drawPath(
                    path = path,
                    color = Color.White,
                    style = Stroke(width = 4f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
            }
        }
    }
}

/**
 * Backward compatibility overload for (LevelConfig) -> Unit caller
 */
@Composable
fun LevelSelectScreen(
    userStats: UserStats?,
    onStartLevel: (LevelConfig) -> Unit,
    onOpenGlossary: () -> Unit,
    onBackToMenu: () -> Unit,
    modifier: Modifier = Modifier,
    currentLanguage: String = "en"
) {
    LevelSelectScreen(
        userStats = userStats,
        onStartLevel = { lvl, _ -> onStartLevel(lvl) },
        onOpenGlossary = onOpenGlossary,
        onBackToMenu = onBackToMenu,
        modifier = modifier,
        currentLanguage = currentLanguage
    )
}

/**
 * Top Header with Total Stars and Rules Shortcut
 */
@Composable
private fun CampaignTopBar(
    totalStars: Int,
    currentLanguage: String = "en",
    onBack: () -> Unit,
    onOpenGlossary: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0x22FFFFFF))
                    .border(1.dp, Color(0x4464B5F6), CircleShape)
                    .testTag("level_select_back_btn")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                val titleText = when (currentLanguage.lowercase()) {
                    "ja" -> "作戦マップ"
                    "in", "id" -> "Peta Misi"
                    else -> "Mission Map"
                }
                val subtitleText = when (currentLanguage.lowercase()) {
                    "ja" -> "作戦セクターを選択して出動"
                    "in", "id" -> "Pilih sektor operasi investigasi"
                    else -> "Select an operational sector"
                }
                Text(
                    text = titleText,
                    color = TextPrimary,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = subtitleText,
                    color = Color(0xFF90CAF9),
                    fontSize = 11.sp
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            // Star Counter Pill
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0x33FFC857),
                border = androidx.compose.foundation.BorderStroke(1.dp, GoldSecondary.copy(alpha = 0.6f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = GoldSecondary,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$totalStars / 20 ★",
                        color = GoldSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Rules Shortcut Button
            IconButton(
                onClick = onOpenGlossary,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0x22FFC857))
                    .border(1.dp, GoldSecondary.copy(alpha = 0.5f), CircleShape)
                    .testTag("level_select_help_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.HelpOutline,
                    contentDescription = stringResource(R.string.menu_glossary),
                    tint = GoldSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/**
 * Visual Game World Roadmap connecting the 4 Campaign Levels
 */
@Composable
private fun CampaignRoadmapView(
    levels: List<LevelConfig>,
    selectedLevelNumber: Int,
    userStats: UserStats?,
    onSelectLevel: (LevelConfig) -> Unit,
    onPositionNode: (Int, Rect) -> Unit
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0x660B1524))
            .border(1.dp, Color(0x3364B5F6), RoundedCornerShape(20.dp))
            .padding(12.dp)
    ) {
        val availableWidth = maxWidth
        val availableHeight = maxHeight

        // Dynamic coordinate map for the 4 levels (staggered S-curve roadmap)
        // Level 1: Bottom Left (30%, 82%)
        // Level 2: Middle Right (70%, 58%)
        // Level 3: Middle Left (32%, 36%)
        // Level 4: Top Summit Center (50%, 14%)
        val nodeOffsets = remember(availableWidth, availableHeight) {
            mapOf(
                1 to Offset(availableWidth.value * 0.28f, availableHeight.value * 0.80f),
                2 to Offset(availableWidth.value * 0.72f, availableHeight.value * 0.58f),
                3 to Offset(availableWidth.value * 0.30f, availableHeight.value * 0.35f),
                4 to Offset(availableWidth.value * 0.50f, availableHeight.value * 0.12f)
            )
        }

        // Draw connecting neon energy conduits between stage nodes
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            val p1 = Offset(nodeOffsets[1]!!.x.dp.toPx(), nodeOffsets[1]!!.y.dp.toPx())
            val p2 = Offset(nodeOffsets[2]!!.x.dp.toPx(), nodeOffsets[2]!!.y.dp.toPx())
            val p3 = Offset(nodeOffsets[3]!!.x.dp.toPx(), nodeOffsets[3]!!.y.dp.toPx())
            val p4 = Offset(nodeOffsets[4]!!.x.dp.toPx(), nodeOffsets[4]!!.y.dp.toPx())

            val path = Path().apply {
                moveTo(p1.x, p1.y)
                cubicTo(
                    p1.x + 80f, p1.y - 40f,
                    p2.x - 80f, p2.y + 40f,
                    p2.x, p2.y
                )
                cubicTo(
                    p2.x - 80f, p2.y - 40f,
                    p3.x + 80f, p3.y + 40f,
                    p3.x, p3.y
                )
                cubicTo(
                    p3.x, p3.y - 40f,
                    p4.x - 40f, p4.y + 40f,
                    p4.x, p4.y
                )
            }

            // Outer glow path
            drawPath(
                path = path,
                color = Color(0x4464B5F6),
                style = Stroke(
                    width = 8f,
                    cap = StrokeCap.Round,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 15f), 0f)
                )
            )
            // Inner core energy path
            drawPath(
                path = path,
                color = Color(0x9990CAF9),
                style = Stroke(
                    width = 3f,
                    cap = StrokeCap.Round,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 15f), 0f)
                )
            )
        }

        // Render the 4 Level Nodes
        levels.forEach { level ->
            val offset = nodeOffsets[level.levelNumber] ?: Offset(0f, 0f)
            val isUnlocked = userStats?.isLevelUnlocked(level.levelNumber) ?: (level.levelNumber == 1)
            val isSelected = level.levelNumber == selectedLevelNumber
            val stars = userStats?.getStarsForLevel(level.levelNumber) ?: 0

            Box(
                modifier = Modifier
                    .offset(
                        x = (offset.x - 42f).dp,
                        y = (offset.y - 42f).dp
                    )
                    .onGloballyPositioned { coords ->
                        onPositionNode(level.levelNumber, coords.boundsInRoot())
                    }
            ) {
                CampaignLevelNode(
                    level = level,
                    isUnlocked = isUnlocked,
                    isSelected = isSelected,
                    stars = stars,
                    onClick = { onSelectLevel(level) }
                )
            }
        }
    }
}

/**
 * Sleek Circular 3D Stage Emblem Node on the Roadmap
 */
@Composable
private fun CampaignLevelNode(
    level: LevelConfig,
    isUnlocked: Boolean,
    isSelected: Boolean,
    stars: Int,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_active_node")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isSelected) 1.08f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "active_pulse"
    )

    val isBoss = level.levelNumber == 4
    val accentColor = when {
        isBoss -> CoralPrimary
        isSelected -> GoldSecondary
        isUnlocked -> Color(0xFF64B5F6)
        else -> Color(0xFF607D8B)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .testTag("mission_card_${level.levelNumber}")
    ) {
        // Main Emblem Orb
        Box(
            modifier = Modifier
                .size(60.dp)
                .scale(pulseScale)
                .clip(CircleShape)
                .background(
                    if (isUnlocked) Brush.radialGradient(
                        listOf(accentColor.copy(alpha = 0.45f), Color(0xEE0D1B2A))
                    ) else Brush.radialGradient(
                        listOf(Color(0x3337474F), Color(0xEE101827))
                    )
                )
                .border(
                    width = if (isSelected) 2.5.dp else 1.5.dp,
                    color = accentColor,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            // Full-color 3D illustration badge
            Image(
                painter = painterResource(id = level.iconRes),
                contentDescription = stringResource(level.nameRes),
                modifier = Modifier
                    .size(50.dp)
                    .alpha(if (isUnlocked) 1f else 0.35f),
                contentScale = ContentScale.Fit
            )

            // Lock Overlay if not yet cleared
            if (!isUnlocked) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(0xEE0A0E17))
                        .border(1.dp, Color(0x66FFFFFF), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Locked",
                        tint = Color(0xFFFFC857),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Level Tier Title placed directly above the Stars for clear readability
        val tierText = when (level.levelNumber) {
            1 -> stringResource(R.string.level_tier_1)
            2 -> stringResource(R.string.level_tier_2)
            3 -> stringResource(R.string.level_tier_3)
            else -> stringResource(R.string.level_tier_boss)
        }

        Surface(
            shape = RoundedCornerShape(10.dp),
            color = if (isSelected) accentColor else Color(0xEE0B1728),
            border = androidx.compose.foundation.BorderStroke(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) accentColor else accentColor.copy(alpha = 0.75f)
            ),
            shadowElevation = if (isSelected) 4.dp else 1.dp
        ) {
            Text(
                text = tierText,
                color = if (isSelected) Color(0xFF09131F) else Color.White,
                fontSize = 8.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.5.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.5.dp)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Stars Rating below Node (5 stars)
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (i in 1..5) {
                val earned = i <= stars
                Icon(
                    imageVector = if (earned) Icons.Default.Star else Icons.Default.StarBorder,
                    contentDescription = null,
                    tint = if (earned && isUnlocked) GoldSecondary else Color(0x44FFFFFF),
                    modifier = Modifier.size(11.dp)
                )
            }
        }
    }
}

/**
 * Tactical Mission Briefing & Deployment Console
 */
@Composable
private fun MissionDeploymentConsole(
    level: LevelConfig,
    userStats: UserStats?,
    selectedDifficulty: GameDifficulty,
    currentLanguage: String = "en",
    onDifficultyChange: (GameDifficulty) -> Unit,
    onStartButtonPositioned: ((Rect) -> Unit)? = null,
    onDeploy: () -> Unit
) {
    val isUnlocked = userStats?.isLevelUnlocked(level.levelNumber) ?: (level.levelNumber == 1)
    val bestScore = userStats?.getBestForLevel(level.levelNumber) ?: 0
    val stars = userStats?.getStarsForLevel(level.levelNumber) ?: 0
    val isBoss = level.levelNumber == 4

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.5.dp,
                if (isUnlocked) Color(0x4464B5F6) else Color(0x22FFFFFF),
                RoundedCornerShape(20.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = Color(0xEE0E1B2D)),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(14.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Sector Header & Name
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    val tierName = when (level.levelNumber) {
                        1 -> stringResource(R.string.level_tier_1)
                        2 -> stringResource(R.string.level_tier_2)
                        3 -> stringResource(R.string.level_tier_3)
                        else -> stringResource(R.string.level_tier_boss)
                    }
                    val sectorBadge = if (isBoss) {
                        when (currentLanguage.lowercase()) {
                            "ja" -> "最終セクター • $tierName"
                            "in", "id" -> "SEKTOR FINAL • $tierName"
                            else -> "FINAL SECTOR • $tierName"
                        }
                    } else {
                        when (currentLanguage.lowercase()) {
                            "ja" -> "セクター ${level.levelNumber} • $tierName"
                            "in", "id" -> "SEKTOR ${level.levelNumber} • $tierName"
                            else -> "SECTOR ${level.levelNumber} • $tierName"
                        }
                    }
                    Text(
                        text = sectorBadge,
                        color = if (isBoss) CoralPrimary else GoldSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    val levelTitle = when (level.levelNumber) {
                        1 -> when (currentLanguage.lowercase()) {
                            "ja" -> "基礎巡回・初期配属"
                            "in", "id" -> "Dasar & SOP Kerja"
                            else -> "Basics & SOP"
                        }
                        2 -> when (currentLanguage.lowercase()) {
                            "ja" -> "営業現場の規律"
                            "in", "id" -> "Integritas Penjualan"
                            else -> "Sales Integrity"
                        }
                        3 -> when (currentLanguage.lowercase()) {
                            "ja" -> "財務・データ監査"
                            "in", "id" -> "Kerahasiaan Data"
                            else -> "Data & Finance"
                        }
                        4 -> when (currentLanguage.lowercase()) {
                            "ja" -> "最終調査 - 総力戦"
                            "in", "id" -> "Investigasi Puncak"
                            else -> "Apex Investigation"
                        }
                        else -> stringResource(level.nameRes)
                    }
                    Text(
                        text = levelTitle,
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Best Score & Stars Display
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isUnlocked && stars > 0) {
                        Row(modifier = Modifier.padding(end = 6.dp)) {
                            for (i in 1..5) {
                                val earned = i <= stars
                                Icon(
                                    imageVector = if (earned) Icons.Default.Star else Icons.Default.StarBorder,
                                    contentDescription = null,
                                    tint = if (earned) GoldSecondary else Color(0x33FFFFFF),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                    if (isUnlocked && bestScore > 0) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0x22FFC857),
                            border = androidx.compose.foundation.BorderStroke(1.dp, GoldSecondary.copy(alpha = 0.5f))
                        ) {
                            val recordLabel = when (currentLanguage.lowercase()) {
                                "ja" -> "記録: $bestScore"
                                "in", "id" -> "Rekor: $bestScore"
                                else -> "Best: $bestScore"
                            }
                            Text(
                                text = recordLabel,
                                color = GoldSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Level Description
            val levelDesc = when (level.levelNumber) {
                1 -> when (currentLanguage.lowercase()) {
                    "ja" -> "コンプライアンスの基礎を学び、軽微な違反を迅速に検知してスライスせよ。"
                    "in", "id" -> "Pelajari dasar kepatuhan etika kerja, tangani pelanggaran SOP ringan dengan cepat."
                    else -> "Learn workplace compliance basics, identify minor SOP violations quickly."
                }
                2 -> when (currentLanguage.lowercase()) {
                    "ja" -> "不適切な販売手法や誇大広告の不正を暴き、営業倫理を死守せよ。"
                    "in", "id" -> "Bongkar praktik penjualan curang, manipulasi promosi, dan pertahankan etika."
                    else -> "Uncover unfair sales practices, promotional manipulation, and uphold ethics."
                }
                3 -> when (currentLanguage.lowercase()) {
                    "ja" -> "情報漏洩や贈賄・不正経費を阻止し、企業の信頼とデータを守り抜け。"
                    "in", "id" -> "Cegah kebocoran data rahasia, gratifikasi ilegal, dan manipulasi laporan."
                    else -> "Prevent confidential data leaks, illegal kickbacks, and financial fraud."
                }
                4 -> when (currentLanguage.lowercase()) {
                    "ja" -> "あらゆる重大違反が高速で襲来する！最高峰のコンプライアンス防衛戦！"
                    "in", "id" -> "Semua pelanggaran berat muncul berkecepatan tinggi! Ujian pamungkas kepatuhan!"
                    else -> "High-speed barrage of all critical violations! The ultimate integrity test!"
                }
                else -> stringResource(level.descRes)
            }
            Text(
                text = levelDesc,
                color = Color(0xFFB0BEC5),
                fontSize = 11.sp,
                lineHeight = 15.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Threat Intel: Mini icons of what violations to slice in this level
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val targetsLabel = when (currentLanguage.lowercase()) {
                    "ja" -> "対象となる違反行為:"
                    "in", "id" -> "Target Ancaman:"
                    else -> "Target Violations:"
                }
                Text(
                    text = targetsLabel,
                    color = Color(0xFF90CAF9),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    level.allowedViolations.forEach { cat ->
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF3E2805))
                                .border(1.dp, Color(0xFFFFC857).copy(alpha = 0.8f), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = cat.iconRes),
                                contentDescription = stringResource(cat.displayNameRes),
                                modifier = Modifier.size(20.dp),
                                contentScale = ContentScale.Fit
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Difficulty Selector (Normal vs Hard)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val diffLabelText = when (currentLanguage.lowercase()) {
                    "ja" -> "難易度:"
                    "in", "id" -> "Tingkat Kesulitan:"
                    else -> "Difficulty:"
                }
                Text(
                    text = diffLabelText,
                    color = Color(0xFFCFD8DC),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.weight(1f))

                GameDifficulty.entries.forEach { diff ->
                    val isSelected = selectedDifficulty == diff
                    val diffColor = if (diff == GameDifficulty.HARD) CoralPrimary else MintSuccess

                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onDifficultyChange(diff) },
                        color = if (isSelected) diffColor.copy(alpha = 0.25f) else Color(0x22FFFFFF),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) diffColor else Color(0x33FFFFFF)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        val diffText = when (diff) {
                            GameDifficulty.NORMAL -> when (currentLanguage.lowercase()) {
                                "ja" -> "ノーマル"
                                "in", "id" -> "Normal"
                                else -> "Normal"
                            }
                            GameDifficulty.HARD -> when (currentLanguage.lowercase()) {
                                "ja" -> "ハード (1.25倍)"
                                "in", "id" -> "Sulit (1.25x)"
                                else -> "Hard (1.25x)"
                            }
                        }
                        Text(
                            text = diffText,
                            color = if (isSelected) diffColor else Color(0xFFB0BEC5),
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Normal,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Deploy / Launch Button (Localized 3 images: English, Indonesian, Japanese matching Start Shift in MainMenu)
            if (isUnlocked) {
                val startDrawable = when (currentLanguage.lowercase()) {
                    "ja" -> R.drawable.btn_start_jp
                    "in", "id" -> R.drawable.btn_start_id
                    else -> R.drawable.btn_start_eg
                }

                val startLabel = when (currentLanguage.lowercase()) {
                    "ja" -> "スタート"
                    "in", "id" -> "MULAI"
                    else -> "START"
                }

                val startInteractionSource = remember { MutableInteractionSource() }
                val isStartPressed by startInteractionSource.collectIsPressedAsState()
                val isStartHovered by startInteractionSource.collectIsHoveredAsState()

                val targetStartInteractiveScale = when {
                    isStartPressed -> 1.05f
                    isStartHovered -> 1.03f
                    else -> 1.0f
                }

                val startInteractiveScale by animateFloatAsState(
                    targetValue = targetStartInteractiveScale,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    ),
                    label = "mission_start_btn_scale"
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .onGloballyPositioned { coordinates ->
                            onStartButtonPositioned?.invoke(coordinates.boundsInRoot())
                        }
                        .scale(startInteractiveScale)
                        .clickable(
                            interactionSource = startInteractionSource,
                            indication = null,
                            onClick = onDeploy
                        )
                        .testTag("dialog_deploy_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = startDrawable),
                        contentDescription = startLabel,
                        modifier = Modifier
                            .fillMaxWidth(0.92f)
                            .height(58.dp),
                        contentScale = ContentScale.Fit
                    )
                }
            } else {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0x3337474F),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x44FFFFFF))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = Color(0xFFFFC857),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        val prevLevelNum = level.levelNumber - 1
                        val prevBest = userStats?.getBestForLevel(prevLevelNum) ?: 0
                        val targetScore = when (level.levelNumber) {
                            2 -> 1200
                            3 -> 2500
                            4 -> 5000
                            else -> 0
                        }
                        val reqText = when (level.levelNumber) {
                            2 -> when (currentLanguage.lowercase()) {
                                "ja" -> "アンロック条件: セクター1で1,200pt以上"
                                "in", "id" -> "Selesaikan Sektor 1 dengan skor minimal 1.200"
                                else -> "Complete Sector 1 with at least 1,200 points"
                            }
                            3 -> when (currentLanguage.lowercase()) {
                                "ja" -> "アンロック条件: セクター2で2,500pt以上"
                                "in", "id" -> "Selesaikan Sektor 2 dengan skor minimal 2.500"
                                else -> "Complete Sector 2 with at least 2,500 points"
                            }
                            4 -> when (currentLanguage.lowercase()) {
                                "ja" -> "アンロック条件: セクター3で5,000pt以上"
                                "in", "id" -> "Selesaikan Sektor 3 dengan skor minimal 5.000"
                                else -> "Complete Sector 3 with at least 5,000 points"
                            }
                            else -> when (currentLanguage.lowercase()) {
                                "ja" -> "セクター ${prevLevelNum} をクリアしてアンロック"
                                "in", "id" -> "Selesaikan Misi ${prevLevelNum} untuk membuka"
                                else -> "Complete Mission ${prevLevelNum} to unlock"
                            }
                        }
                        Column {
                            Text(
                                text = reqText,
                                color = Color(0xFFCFD8DC),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (targetScore > 0) {
                                Spacer(modifier = Modifier.height(2.dp))
                                val progressLabel = when (currentLanguage.lowercase()) {
                                    "ja" -> "進捗: $prevBest / $targetScore pt"
                                    "in", "id" -> "Progres: $prevBest / $targetScore poin"
                                    else -> "Progress: $prevBest / $targetScore pts"
                                }
                                Text(
                                    text = progressLabel,
                                    color = GoldSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun lineIntersectsRect(p1: Offset, p2: Offset, rect: Rect): Boolean {
    if (rect.contains(p1) || rect.contains(p2)) return true
    val minX = minOf(p1.x, p2.x)
    val maxX = maxOf(p1.x, p2.x)
    val minY = minOf(p1.y, p2.y)
    val maxY = maxOf(p1.y, p2.y)

    if (maxX < rect.left || minX > rect.right || maxY < rect.top || minY > rect.bottom) {
        return false
    }

    return lineIntersectsLine(p1, p2, Offset(rect.left, rect.top), Offset(rect.right, rect.top)) ||
            lineIntersectsLine(p1, p2, Offset(rect.left, rect.bottom), Offset(rect.right, rect.bottom)) ||
            lineIntersectsLine(p1, p2, Offset(rect.left, rect.top), Offset(rect.left, rect.bottom)) ||
            lineIntersectsLine(p1, p2, Offset(rect.right, rect.top), Offset(rect.right, rect.bottom))
}

private fun lineIntersectsLine(a1: Offset, a2: Offset, b1: Offset, b2: Offset): Boolean {
    val d = (a2.x - a1.x) * (b2.y - b1.y) - (a2.y - a1.y) * (b2.x - b1.x)
    if (d == 0f) return false
    val u = ((b1.x - a1.x) * (b2.y - b1.y) - (b1.y - a1.y) * (b2.x - b1.x)) / d
    val v = ((b1.x - a1.x) * (a2.y - a1.y) - (b1.y - a1.y) * (a2.x - a1.x)) / d
    return (u in 0f..1f) && (v in 0f..1f)
}
