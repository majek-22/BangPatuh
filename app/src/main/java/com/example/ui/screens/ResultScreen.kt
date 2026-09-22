package com.example.ui.screens

import android.app.Activity
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.R
import com.example.data.ComplianceCategory
import com.example.data.GameDifficulty
import com.example.data.LevelConfig
import com.example.data.SlicedCategoryRecord
import com.example.ui.theme.GoldSecondary

@Composable
fun ResultScreen(
    level: LevelConfig,
    difficulty: GameDifficulty,
    score: Int,
    stars: Int,
    highScore: Int,
    isNewHighScore: Boolean,
    rankRes: Int,
    trapsAvoided: Int,
    trapsSliced: Int,
    slicedSummary: List<SlicedCategoryRecord>,
    elapsedSeconds: Float = 0f,
    currentLanguage: String = "en",
    onToggleLanguage: () -> Unit = {},
    onSelectLanguage: ((String) -> Unit)? = null,
    onOpenRules: () -> Unit = {},
    onPlayAgain: () -> Unit,
    onSelectLevel: () -> Unit,
    onReturnToMenu: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val window = (context as? Activity)?.window

    DisposableEffect(window) {
        if (window != null) {
            val insetsController = WindowCompat.getInsetsController(window, window.decorView)
            insetsController.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            insetsController.hide(WindowInsetsCompat.Type.navigationBars())
            onDispose { }
        } else {
            onDispose { }
        }
    }

    val isIndonesian = currentLanguage == "in" || currentLanguage == "id"
    val isJa = currentLanguage.equals("ja", ignoreCase = true)
    val secondsInt = elapsedSeconds.toInt().coerceAtLeast(0)
    val minutes = secondsInt / 60
    val secs = secondsInt % 60
    val timeSurvivedFormatted = String.format("%02d:%02d", minutes, secs)

    val rankName = when (rankRes) {
        R.string.rank_intern -> if (isJa) "コンプライアンス実習生" else if (isIndonesian) "Magang Kepatuhan" else "Compliance Intern"
        R.string.rank_auditor -> if (isJa) "ジュニア監査役" else if (isIndonesian) "Auditor Muda" else "Junior Auditor"
        R.string.rank_officer -> if (isJa) "コンプライアンス担当官" else if (isIndonesian) "Petugas Kepatuhan" else "Compliance Officer"
        R.string.rank_senior_officer -> if (isJa) "シニア・コンプライアンス担当官" else if (isIndonesian) "Petugas Kepatuhan Senior" else "Senior Compliance Officer"
        R.string.rank_risk_lead -> if (isJa) "統括リスクリーダー" else if (isIndonesian) "Ketua Manajemen Risiko" else "Senior Risk Lead"
        R.string.rank_director -> if (isJa) "最高コンプライアンス責任者 (CCO)" else if (isIndonesian) "Direktur Kepatuhan Utama" else "Chief Compliance Director"
        else -> stringResource(rankRes)
    }.uppercase()

    // Determine recap items: if slicedSummary is non-empty, use it. Otherwise, show standard violation categories
    val recapCategories = if (slicedSummary.isNotEmpty()) {
        slicedSummary
    } else {
        listOf(
            SlicedCategoryRecord(ComplianceCategory.BRIBERY, 0),
            SlicedCategoryRecord(ComplianceCategory.MONEY_LAUNDERING, 0),
            SlicedCategoryRecord(ComplianceCategory.DATA_BREACH, 0),
            SlicedCategoryRecord(ComplianceCategory.FRAUD, 0),
            SlicedCategoryRecord(ComplianceCategory.SYSTEMIC_CORRUPTION, 0)
        )
    }

    BoxWithConstraints(
        modifier = modifier.fillMaxSize()
    ) {
        val screenMaxWidth = maxWidth
        val isLandscape = maxWidth > maxHeight

        // 1. Scenery Background
        Image(
            painter = painterResource(id = level.backgroundRes),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // 2. Deep dark vignette overlay matching Screen 3
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xEE0A0F1D),
                            Color(0xF5060810),
                            Color(0xF80A0B14)
                        )
                    )
                )
        )

        // 3. Main Result Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = if (isLandscape) 20.dp else 16.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // TOP HEADER: Laurel Shield Badge, Rank Title, Huge Score & Close Button
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Left: Survived time & Stars
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(top = 4.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0x3300E5FF),
                        border = BorderStroke(1.dp, Color(0x6600E5FF))
                    ) {
                        val survivedLabel = when (currentLanguage.lowercase()) {
                            "ja" -> "生存時間: $timeSurvivedFormatted"
                            "in", "id" -> "BERTAHAN: $timeSurvivedFormatted"
                            else -> "SURVIVED: $timeSurvivedFormatted"
                        }
                        Text(
                            text = survivedLabel,
                            color = Color(0xFF80D8FF),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    // 5 Stars
                    Row {
                        for (i in 1..5) {
                            val earned = i <= stars
                            Icon(
                                imageVector = if (earned) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = null,
                                tint = if (earned) GoldSecondary else Color(0x44FFFFFF),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                // Center: Golden Laurel Shield Emblem, Rank Name, Huge Golden Score (Centered relative to full screen width)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                    modifier = Modifier.align(Alignment.TopCenter)
                ) {
                    // Golden Laurel Wreath & Shield Emblem (Screen 3)
                    Image(
                        painter = painterResource(id = R.drawable.ic_laurel_shield),
                        contentDescription = null,
                        modifier = Modifier.size(if (isLandscape) 42.dp else 48.dp)
                    )

                    // Officer Rank Title
                    Text(
                        text = rankName,
                        color = GoldSecondary,
                        fontSize = if (isLandscape) 14.sp else 16.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.8.sp,
                        textAlign = TextAlign.Center,
                        style = TextStyle(
                            shadow = Shadow(
                                color = Color(0xAAFFD54F),
                                offset = Offset(0f, 0f),
                                blurRadius = 10f
                            )
                        )
                    )

                    // Giant Golden Score
                    Text(
                        text = String.format("%,d", score),
                        fontSize = if (isLandscape) 36.sp else 42.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        textAlign = TextAlign.Center,
                        style = TextStyle(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFFFFF9C4),
                                    GoldSecondary,
                                    Color(0xFFFFA000)
                                )
                            ),
                            shadow = Shadow(
                                color = Color(0xCC000000),
                                offset = Offset(3f, 4f),
                                blurRadius = 8f
                            )
                        )
                    )
                }

                // Right: Exit (X) circle button
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 4.dp)
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color(0x66000000))
                        .border(1.2.dp, Color(0x55FFFFFF), CircleShape)
                        .clickable(onClick = onReturnToMenu)
                        .testTag("return_menu_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Return to Menu",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // SUBTITLE: "— Your Shift Recap —" with decorative gold wing lines
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(60.dp)
                        .height(1.dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color.Transparent, Color(0x88FFD54F))
                            )
                        )
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = stringResource(R.string.result_shift_recap),
                    color = Color(0xFFFFD54F),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.width(10.dp))
                Box(
                    modifier = Modifier
                        .width(60.dp)
                        .height(1.dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0x88FFD54F), Color.Transparent)
                            )
                        )
                )
            }

            // CARDS DISPLAY: Side-by-side Carousel / Row in Landscape or Scroll in Portrait
            if (isLandscape) {
                val listState = rememberLazyListState()
                val totalCardsWidth = (recapCategories.size * 232).dp
                val availableWidth = screenMaxWidth - 40.dp
                val isScrollable by remember {
                    derivedStateOf {
                        totalCardsWidth > availableWidth ||
                        listState.canScrollForward ||
                        listState.canScrollBackward
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    LazyRow(
                        state = listState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        horizontalArrangement = if (isScrollable) Arrangement.Start else Arrangement.Center,
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        items(recapCategories) { item ->
                            ShiftRecapCard(
                                category = item.category,
                                count = item.count,
                                currentLanguage = currentLanguage,
                                isLandscape = true,
                                modifier = Modifier
                                    .width(220.dp)
                                    .padding(horizontal = 6.dp)
                            )
                        }
                    }
                }
            } else {
                // Portrait Layout
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (item in recapCategories) {
                        ShiftRecapCard(
                            category = item.category,
                            count = item.count,
                            currentLanguage = currentLanguage,
                            isLandscape = false,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // BOTTOM ACTION BAR: Big Glowing "Play Again" Pill & "Main Menu" Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 2.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val actionButtonHeight = 44.dp
                val actionButtonShape = RoundedCornerShape(22.dp)

                // Main Menu Secondary Button
                OutlinedButton(
                    onClick = onReturnToMenu,
                    modifier = Modifier
                        .height(actionButtonHeight)
                        .padding(end = 6.dp),
                    shape = actionButtonShape,
                    border = BorderStroke(1.2.dp, Color(0x6664B5F6)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color(0x1A1E88E5)
                    ),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = null,
                        tint = Color(0xFF90CAF9),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    val menuButtonLabel = when (currentLanguage.lowercase()) {
                        "ja" -> "メインメニュー"
                        "in", "id" -> "MENU UTAMA"
                        else -> "MAIN MENU"
                    }
                    Text(
                        text = menuButtonLabel,
                        color = Color(0xFF90CAF9),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }

                // Play Again Button (Localized 3 images: English, Indonesian, Japanese, matching Start Shift button style)
                val playAgainDrawable = when (currentLanguage.lowercase()) {
                    "ja" -> R.drawable.btn_play_again_jp
                    "in", "id" -> R.drawable.btn_play_again_id
                    else -> R.drawable.btn_play_again_eg
                }

                val playAgainLabel = when (currentLanguage.lowercase()) {
                    "ja" -> "もう一度プレイ"
                    "in", "id" -> "MAIN LAGI"
                    else -> "PLAY AGAIN"
                }

                val playAgainInteractionSource = remember { MutableInteractionSource() }
                val isPlayAgainPressed by playAgainInteractionSource.collectIsPressedAsState()
                val isPlayAgainHovered by playAgainInteractionSource.collectIsHoveredAsState()

                val targetPlayAgainInteractiveScale = when {
                    isPlayAgainPressed -> 1.07f
                    isPlayAgainHovered -> 1.04f
                    else -> 1.0f
                }

                val playAgainScale by animateFloatAsState(
                    targetValue = targetPlayAgainInteractiveScale,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    ),
                    label = "play_again_scale"
                )

                Image(
                    painter = painterResource(id = playAgainDrawable),
                    contentDescription = playAgainLabel,
                    modifier = Modifier
                        .scale(playAgainScale)
                        .height(actionButtonHeight)
                        .padding(start = 6.dp)
                        .clickable(
                            interactionSource = playAgainInteractionSource,
                            indication = null,
                            onClick = onPlayAgain
                        )
                        .pointerInput(onPlayAgain) {
                            detectDragGestures(
                                onDragStart = { onPlayAgain() },
                                onDrag = { _, _ -> }
                            )
                        }
                        .testTag("play_again_button"),
                    contentScale = ContentScale.Fit 
                )
            }
        }
    }
}

// =========================================================================
// SHIFT RECAP CARD COMPONENT (Screen 3 in image.png)
// =========================================================================

@Composable
private fun ShiftRecapCard(
    category: ComplianceCategory,
    count: Int,
    currentLanguage: String,
    isLandscape: Boolean,
    modifier: Modifier = Modifier
) {
    val isId = currentLanguage == "in" || currentLanguage == "id"
    val displayName = category.getDisplayName(currentLanguage)
    val explanation = category.getExplanation(currentLanguage)

    Surface(
        modifier = modifier
            .shadow(12.dp, RoundedCornerShape(16.dp), spotColor = Color(0xFFFF3B30)),
        shape = RoundedCornerShape(16.dp),
        color = Color(0x88240A10),
        border = BorderStroke(1.2.dp, Color(0x55FF3B30))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Glowing Squircle Badge for Category Icon
            Surface(
                modifier = Modifier.size(38.dp),
                shape = RoundedCornerShape(10.dp),
                color = Color(0x44000000),
                border = BorderStroke(1.2.dp, Color(0xFFFF5252))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0x33FF3B30)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = category.iconRes),
                        contentDescription = null,
                        modifier = Modifier.size(26.dp),
                        contentScale = ContentScale.Fit
                    )
                }
            }

            // Category Title
            Text(
                text = displayName,
                color = Color(0xFFFF7043),
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
                maxLines = 1
            )

            // Educational Principle Description
            Text(
                text = explanation,
                color = Color(0xFFCFD8DC),
                fontSize = if (isLandscape) 10.sp else 11.sp,
                lineHeight = 14.sp,
                textAlign = TextAlign.Center,
                maxLines = if (isLandscape) 3 else 4
            )

            // Times neutralized pill (if sliced > 0)
            if (count > 0) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0x55000000),
                    border = BorderStroke(1.dp, GoldSecondary.copy(alpha = 0.7f))
                ) {
                    val countSuffix = when {
                        currentLanguage.lowercase() == "ja" -> "回阻止"
                        isId -> "ditebas"
                        else -> "sliced"
                    }
                    Text(
                        text = "×$count $countSuffix",
                        color = GoldSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}
