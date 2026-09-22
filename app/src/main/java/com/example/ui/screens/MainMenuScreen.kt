package com.example.ui.screens

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.ui.zIndex
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.GlossaryEntry
import com.example.data.GlossarySection
import com.example.R
import com.example.data.AvatarHelper
import com.example.ui.components.ComplianceChatbotPopup
import com.example.ui.components.ComplianceComicDialog
import com.example.ui.components.LanguageDropdownMenu
import com.example.ui.theme.CoralPrimary
import com.example.ui.theme.GoldSecondary

@Composable
fun MainMenuScreen(
    currentUser: String?,
    userAvatarId: Int = 1,
    highScore: Int,
    currentLanguage: String,
    isAudioMuted: Boolean,
    shouldShowComic: Boolean = false,
    onComicDismissed: () -> Unit = {},
    onStartShift: () -> Unit,
    onOpenLeaderboard: () -> Unit,
    onOpenGlossary: () -> Unit,
    onOpenProfile: () -> Unit = {},
    onToggleLanguage: () -> Unit,
    onSelectLanguage: ((String) -> Unit)? = null,
    onToggleAudioMute: () -> Unit,
    onLogout: () -> Unit,
    onPauseMusic: () -> Unit = {},
    onResumeMusic: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "menu_anim")

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_button"
    )

    var showChatPopup by remember { mutableStateOf(false) }
    var showComicDialog by remember(shouldShowComic) { mutableStateOf(shouldShowComic) }
    LaunchedEffect(shouldShowComic) {
        if (shouldShowComic) showComicDialog = true
    }
    val coroutineScope = rememberCoroutineScope()
    val chatIconRotation = remember { Animatable(0f) }

    val sliceTrail = remember { mutableStateListOf<Offset>() }
    var startButtonBounds by remember { mutableStateOf<Rect?>(null) }
    var hasTriggeredStartShift by remember { mutableStateOf(false) }

    fun checkSliceStartShift(p1: Offset, p2: Offset) {
        if (hasTriggeredStartShift) return
        val length = kotlin.math.hypot(p2.x - p1.x, p2.y - p1.y)
        if (length < 30f) return

        val bounds = startButtonBounds ?: return
        if (lineIntersectsRectMenu(p1, p2, bounds)) {
            hasTriggeredStartShift = true
            coroutineScope.launch {
                delay(100)
                onStartShift()
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
                            checkSliceStartShift(lastPos, currentPos)
                        }
                        sliceTrail.add(currentPos)
                        if (sliceTrail.size > 14) sliceTrail.removeAt(0)
                    },
                    onDragEnd = {
                        coroutineScope.launch { delay(100); sliceTrail.clear() }
                    },
                    onDragCancel = { sliceTrail.clear() }
                )
            }
    ) {
        val isLandscape = maxWidth > maxHeight

        // 1. Scenic Main Menu Background Art
        Image(
            painter = painterResource(id = R.drawable.bg_main_menu),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // 2. Main Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = if (isLandscape) 18.dp else 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            MainMenuTopBar(
                currentUser = currentUser, userAvatarId = userAvatarId, currentLanguage = currentLanguage,
                isAudioMuted = isAudioMuted, onOpenProfile = onOpenProfile, onOpenComic = { showComicDialog = true },
                onOpenLeaderboard = onOpenLeaderboard, onOpenGlossary = onOpenGlossary,
                onToggleLanguage = onToggleLanguage, onSelectLanguage = onSelectLanguage,
                onToggleAudioMute = onToggleAudioMute, onLogout = onLogout
            )

            if (isLandscape) {
                Row(
                    modifier = Modifier.weight(1f).fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1.05f).fillMaxHeight(),
                        verticalArrangement = Arrangement.Bottom,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.weight(1f))
                        HighScoreBadge(highScore = highScore)
                        Spacer(modifier = Modifier.height(10.dp))
                        StartShiftGlowingButton(
                            onClick = onStartShift, pulseScale = pulseScale, isLandscape = true,
                            currentLanguage = currentLanguage, onPositioned = { startButtonBounds = it }
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                    Box(modifier = Modifier.weight(1.05f).fillMaxHeight(), contentAlignment = Alignment.Center) {
                        ComplianceRulesTable(currentLanguage = currentLanguage, modifier = Modifier.fillMaxWidth())
                    }
                }
            } else {
                Column(
                    modifier = Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    Spacer(modifier = Modifier.height(160.dp))
                    HighScoreBadge(highScore = highScore)
                    StartShiftGlowingButton(
                        onClick = onStartShift, pulseScale = pulseScale, isLandscape = false,
                        currentLanguage = currentLanguage, onPositioned = { startButtonBounds = it }
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    ComplianceRulesTable(currentLanguage = currentLanguage, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }

       // 4. Floating AI Assistant Button (Vertical: Icon on top, Text below)
        val chatInteractionSource = remember { MutableInteractionSource() }
        
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(end = 16.dp, bottom = 12.dp)
                .clickable(
                    interactionSource = chatInteractionSource, 
                    indication = null
                ) {
                    coroutineScope.launch {
                        launch {
                            chatIconRotation.animateTo(chatIconRotation.value + 360f, tween(450, easing = FastOutSlowInEasing))
                        }
                        delay(220)
                        showChatPopup = true
                    }
                },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Ikon Chatbot Bundar Utama di Atas
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .shadow(8.dp, CircleShape, spotColor = Color(0x6600E5FF))
                    .clip(CircleShape)
                    .background(Color(0xEE07121E))
                    .border(1.5.dp, Brush.horizontalGradient(listOf(Color(0xFF00E5FF), GoldSecondary)), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_chatbot), 
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer { rotationZ = chatIconRotation.value },
                    contentScale = ContentScale.Crop
                )
            }

            // Teks Label di Bawah Lingkaran dengan Bayangan agar Jelas Terbaca
            val aiAssistantLabel = when (currentLanguage.lowercase()) {
                "ja" -> "AIアシスタント"
                "in", "id" -> "Asisten AI"
                else -> stringResource(R.string.menu_ai_assistant)
            }
            Text(
                text = aiAssistantLabel, 
                color = Color(0xFFE0F7FA), 
                fontSize = 10.sp, 
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                style = androidx.compose.ui.text.TextStyle(
                    shadow = androidx.compose.ui.graphics.Shadow(
                        color = Color.Black,
                        offset = Offset(1f, 1f),
                        blurRadius = 3f
                    )
                )
            )
        }

        // 5. Slice Trail Overlay
        if (sliceTrail.size > 1) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val path = Path().apply {
                    moveTo(sliceTrail.first().x, sliceTrail.first().y)
                    for (i in 1 until sliceTrail.size) lineTo(sliceTrail[i].x, sliceTrail[i].y)
                }
                drawPath(path = path, color = CoralPrimary.copy(alpha = 0.85f), style = Stroke(width = 12f, cap = StrokeCap.Round, join = StrokeJoin.Round))
                drawPath(path = path, color = Color.White, style = Stroke(width = 4.5f, cap = StrokeCap.Round, join = StrokeJoin.Round))
            }
        }
        if (showChatPopup) ComplianceChatbotPopup(onDismiss = { showChatPopup = false })
        if (showComicDialog) ComplianceComicDialog(onDismiss = { showComicDialog = false; onComicDismissed() })
    }
}

@Composable
private fun HighScoreBadge(highScore: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.background(Color(0x55000000), RoundedCornerShape(12.dp)).border(1.dp, Color(0x33FFD54F), RoundedCornerShape(12.dp)).padding(horizontal = 14.dp, vertical = 4.dp)
    ) {
        Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = GoldSecondary, modifier = Modifier.size(15.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = stringResource(R.string.menu_high_score_label).uppercase(), color = Color(0xFFB0BEC5), fontSize = 10.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = String.format("%,d", highScore), color = GoldSecondary, fontSize = 15.sp, fontWeight = FontWeight.Black)
    }
}

// =========================================================================
// TOP BAR COMPONENT
// =========================================================================
@Composable
private fun Modifier.menuScaleAnimation(interactionSource: MutableInteractionSource, hoverScale: Float = 1.08f, pressScale: Float = 1.15f): Modifier {
    val isPressed by interactionSource.collectIsPressedAsState()
    val isHovered by interactionSource.collectIsHoveredAsState()
    val animatedScale by animateFloatAsState(targetValue = if (isPressed) pressScale else if (isHovered) hoverScale else 1.0f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow), label = "menu_btn_scale")
    return this.graphicsLayer { scaleX = animatedScale; scaleY = animatedScale }
}

@Composable
private fun MainMenuTopBar(
    currentUser: String?, userAvatarId: Int, currentLanguage: String, isAudioMuted: Boolean,
    onOpenProfile: () -> Unit, onOpenComic: () -> Unit, onOpenLeaderboard: () -> Unit, onOpenGlossary: () -> Unit,
    onToggleLanguage: () -> Unit, onSelectLanguage: ((String) -> Unit)?, onToggleAudioMute: () -> Unit, onLogout: () -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        val profileInteraction = remember { MutableInteractionSource() }
        Surface(
            shape = RoundedCornerShape(14.dp), color = Color(0xDD091522),
            border = BorderStroke(1.dp, Brush.horizontalGradient(listOf(Color(0x8800E5FF), Color(0x3300E5FF)))),
            shadowElevation = 6.dp, modifier = Modifier.menuScaleAnimation(profileInteraction, hoverScale = 1.04f, pressScale = 1.08f)
        ) {
            Row(
                modifier = Modifier.height(38.dp).clip(RoundedCornerShape(14.dp)).clickable(interactionSource = profileInteraction, indication = null, onClick = onOpenProfile).padding(start = 5.dp, end = 12.dp),
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val avatarRes = AvatarHelper.getAvatarRes(userAvatarId, currentUser ?: "")
                Image(painter = painterResource(id = avatarRes), contentDescription = null, modifier = Modifier.size(28.dp).clip(CircleShape).border(1.2.dp, Color(0xFF00E5FF), CircleShape))
                Text(text = currentUser?.ifBlank { "Player" } ?: "Player", color = Color(0xFFE0F7FA), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Surface(shape = RoundedCornerShape(14.dp), color = Color(0xDD091522), border = BorderStroke(1.dp, Brush.horizontalGradient(listOf(Color(0x5500E5FF), Color(0x3364B5F6), Color(0x44FFD54F)))), shadowElevation = 6.dp) {
                Row(modifier = Modifier.padding(horizontal = 4.dp, vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
                    val comicInteraction = remember { MutableInteractionSource() }
                    Box(modifier = Modifier.size(34.dp).menuScaleAnimation(comicInteraction).clip(RoundedCornerShape(8.dp)).clickable(interactionSource = comicInteraction, indication = null, onClick = onOpenComic), contentAlignment = Alignment.Center) { Icon(Icons.Default.MenuBook, contentDescription = null, tint = Color(0xFF00E5FF), modifier = Modifier.size(17.dp)) }
                    Box(modifier = Modifier.width(1.dp).height(16.dp).background(Color(0x26FFFFFF)))
                    val leaderboardInteraction = remember { MutableInteractionSource() }
                    Box(modifier = Modifier.size(34.dp).menuScaleAnimation(leaderboardInteraction).clip(RoundedCornerShape(8.dp)).clickable(interactionSource = leaderboardInteraction, indication = null, onClick = onOpenLeaderboard), contentAlignment = Alignment.Center) { Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = GoldSecondary, modifier = Modifier.size(17.dp)) }
                    Box(modifier = Modifier.width(1.dp).height(16.dp).background(Color(0x26FFFFFF)))
                    val glossaryInteraction = remember { MutableInteractionSource() }
                    Box(modifier = Modifier.size(34.dp).menuScaleAnimation(glossaryInteraction).clip(RoundedCornerShape(8.dp)).clickable(interactionSource = glossaryInteraction, indication = null, onClick = onOpenGlossary), contentAlignment = Alignment.Center) { Icon(Icons.Default.Book, contentDescription = null, tint = Color(0xFF64B5F6), modifier = Modifier.size(17.dp)) }
                }
            }
            Surface(shape = RoundedCornerShape(14.dp), color = Color(0xDD091522), border = BorderStroke(1.dp, Brush.horizontalGradient(listOf(Color(0x33FFFFFF), Color(0x44FF5252)))), shadowElevation = 6.dp) {
                Row(modifier = Modifier.padding(horizontal = 4.dp, vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
                    val langInteraction = remember { MutableInteractionSource() }
                    var showLanguageMenu by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.height(34.dp).menuScaleAnimation(langInteraction).clip(RoundedCornerShape(8.dp)).clickable(interactionSource = langInteraction, indication = null) { showLanguageMenu = true; onToggleLanguage() }.padding(horizontal = 7.dp), contentAlignment = Alignment.Center) {
                        Crossfade(targetState = currentLanguage, animationSpec = tween(250), label = "") { lang ->
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                Text(text = "🌐", fontSize = 11.sp)
                                Text(text = when (lang.lowercase()) { "ja" -> "JA"; "in", "id" -> "ID"; else -> "EN" }, color = GoldSecondary, fontSize = 11.sp, fontWeight = FontWeight.Black)
                            }
                        }
                        LanguageDropdownMenu(expanded = showLanguageMenu, currentLanguage = currentLanguage, onDismissRequest = { showLanguageMenu = false }, onLanguageSelected = { showLanguageMenu = false; onSelectLanguage?.invoke(it) })
                    }
                    Box(modifier = Modifier.width(1.dp).height(16.dp).background(Color(0x26FFFFFF)))
                    val audioInteraction = remember { MutableInteractionSource() }
                    Box(modifier = Modifier.size(34.dp).menuScaleAnimation(audioInteraction).clip(RoundedCornerShape(8.dp)).clickable(interactionSource = audioInteraction, indication = null, onClick = onToggleAudioMute), contentAlignment = Alignment.Center) { Icon(if (isAudioMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp, contentDescription = null, tint = if (isAudioMuted) Color(0xFFFF5252) else Color(0xFF00E676), modifier = Modifier.size(17.dp)) }
                    Box(modifier = Modifier.width(1.dp).height(16.dp).background(Color(0x26FFFFFF)))
                    val logoutInteraction = remember { MutableInteractionSource() }
                    Box(modifier = Modifier.size(34.dp).menuScaleAnimation(logoutInteraction).clip(RoundedCornerShape(8.dp)).clickable(interactionSource = logoutInteraction, indication = null, onClick = onLogout), contentAlignment = Alignment.Center) { Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, tint = Color(0xFFFF5252), modifier = Modifier.size(17.dp)) }
                }
            }
        }
    }
}

// =========================================================================
// 4-CATEGORY DIRECTIVES 2x2 GRID (SLICE, PROTECT, AVOID, COLLECT)
// =========================================================================

@Composable
private fun ComplianceRulesTable(
    currentLanguage: String,
    modifier: Modifier = Modifier
) {
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val isLandscape = configuration.screenWidthDp > configuration.screenHeightDp
    
    val responsiveCardWidth = if (isLandscape) {
        (configuration.screenWidthDp * 0.23f).dp.coerceAtMost(220.dp)
    } else {
        // Ditarik menjadi 47% lebar layar agar mentok mendekati tepi
        (configuration.screenWidthDp * 0.47f).dp.coerceAtMost(210.dp) 
    }

    val violationEntries = remember { GlossaryEntry.ALL_ENTRIES.filter { it.section == GlossarySection.VIOLATIONS } }
    val trapEntries = remember { GlossaryEntry.ALL_ENTRIES.filter { it.section == GlossarySection.TRAPS } }
    val legitimateEntries = remember { GlossaryEntry.ALL_ENTRIES.filter { it.section == GlossarySection.LEGITIMATE } }
    val bonusEntries = remember { GlossaryEntry.ALL_ENTRIES.filter { it.section == GlossarySection.BONUS } }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        val howToPlayDrawable = when (currentLanguage.lowercase()) {
            "ja" -> R.drawable.title_how_to_play_jp
            "in", "id" -> R.drawable.title_how_to_play_id
            else -> R.drawable.title_how_to_play_en
        }
        
        // --- ANIMASI NAIK-TURUN UNTUK "HOW TO PLAY" ---
        val infiniteTransition = rememberInfiniteTransition(label = "how_to_play_anim")
        val floatYAnim by infiniteTransition.animateFloat(
            initialValue = -18f, // Posisi awal (sedikit lebih tinggi)
            targetValue = -12f,  // Posisi akhir (sedikit lebih rendah)
            animationSpec = infiniteRepeatable(
                animation = tween(1500, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "how_to_play_float"
        )
        // ----------------------------------------------
        
        Image(
            painter = painterResource(id = howToPlayDrawable),
            contentDescription = "How To Play",
            // Terapkan animasi floatYAnim pada offset Y
            modifier = Modifier.height(52.dp).offset(y = floatYAnim.dp).padding(bottom = 2.dp),
            contentScale = ContentScale.Fit
        )
        
        // BARIS 1: SLICE & PROTECT
        Row(modifier = Modifier.fillMaxWidth().offset(y = (-20).dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            NinjaMissionScrollCard(
                shortTitle = stringResource(id = R.string.short_title_slice),
                fullTitle = stringResource(R.string.menu_slice_violations_header).replace("—", "-"),
                headerIconRes = R.drawable.ic_crossed_katanas_gold,
                headerPlaqueColors = listOf(Color(0xFFFBC02D), Color(0xFFFFD54F), Color(0xFFF57F17)),
                bgImageRes = R.drawable.bg_ninja_scroll,
                items = violationEntries.map { RuleItemData(iconRes = it.iconRes, name = stringResource(it.nameRes)) },
                modifier = Modifier.width(responsiveCardWidth).padding(end = 6.dp)
            )
            NinjaMissionScrollCard(
                shortTitle = stringResource(id = R.string.short_title_protect), // <-- KOMA SUDAH DITAMBAHKAN
                fullTitle = stringResource(R.string.menu_protect_legitimate_header).replace("—", "-"),
                headerIconRes = R.drawable.ic_laurel_shield,
                headerPlaqueColors = listOf(Color(0xFF0052CC), Color(0xFF007AFF), Color(0xFF003D99)),
                bgImageRes = R.drawable.bg_ninja_scroll_blue,
                items = legitimateEntries.map { RuleItemData(iconRes = it.iconRes, name = stringResource(it.nameRes)) },
                modifier = Modifier.width(responsiveCardWidth).padding(start = 6.dp)
            )
        }

        // BARIS 2: AVOID & COLLECT
        Row(modifier = Modifier.fillMaxWidth().offset(y = (-20).dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            NinjaMissionScrollCard(
                shortTitle = stringResource(id = R.string.short_title_avoid),
                fullTitle = stringResource(R.string.menu_avoid_legitimate_header).replace("—", "-"),
                headerIconRes = R.drawable.ic_scroll_hazard,
                headerPlaqueColors = listOf(Color(0xFFFF0033), Color(0xFFFF4D6D), Color(0xFFCC0029)),
                bgImageRes = R.drawable.bg_ninja_scroll_crimson,
                items = trapEntries.map { RuleItemData(iconRes = it.iconRes, name = stringResource(it.nameRes)) },
                modifier = Modifier.width(responsiveCardWidth).padding(end = 6.dp)
            )
            NinjaMissionScrollCard(
                shortTitle = stringResource(id = R.string.short_title_collect),
                fullTitle = stringResource(R.string.menu_collect_bonus_header).replace("—", "-"),
                headerIconRes = R.drawable.ic_scroll_bonus_gem,
                headerPlaqueColors = listOf(Color(0xFF388E3C), Color(0xFF66BB6A), Color(0xFF1B5E20)),
                bgImageRes = R.drawable.bg_ninja_scroll_green,
                items = bonusEntries.map { RuleItemData(iconRes = it.iconRes, name = stringResource(it.nameRes)) },
                modifier = Modifier.width(responsiveCardWidth).padding(start = 6.dp)
            )
        }
    }
}

private data class RuleItemData(val iconRes: Int, val name: String)

@Composable
private fun NinjaMissionScrollCard(
    shortTitle: String, // Ditambahkan: Parameter untuk teks pendek
    fullTitle: String,  // Ditambahkan: Parameter untuk teks panjang
    headerIconRes: Int,
    headerPlaqueColors: List<Color>,
    items: List<RuleItemData>,
    modifier: Modifier = Modifier,
    bgImageRes: Int? = null
) {
    var showExpandedDialog by remember { mutableStateOf(false) }
    var animationStage by remember { mutableStateOf(0) }

    val scaleAnim by animateFloatAsState(targetValue = if (animationStage >= 1) 1f else 0.3f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow), label = "")
    val glowAlpha by animateFloatAsState(targetValue = if (animationStage >= 1) 1f else 0f, animationSpec = tween(500), label = "")
    val unrollAnim by animateFloatAsState(targetValue = if (animationStage >= 2) 1f else 0.25f, animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = 65f), label = "")

    LaunchedEffect(showExpandedDialog) {
        if (showExpandedDialog) {
            animationStage = 1
            delay(400)
            animationStage = 2
        } else {
            animationStage = 0
        }
    }

    Box(modifier = modifier.clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { showExpandedDialog = true }) {
        // MENGIRIM TEKS PENDEK ("SLICE") SAAT TERGULUNG
        RolledScrollThumbnail(headerTitle = shortTitle, headerIconRes = headerIconRes, headerPlaqueColors = headerPlaqueColors)
    }

    if (showExpandedDialog) {
        androidx.compose.ui.window.Dialog(onDismissRequest = { showExpandedDialog = false }, properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false, dismissOnClickOutside = true)) {
            Box(modifier = Modifier.fillMaxSize().clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { showExpandedDialog = false }, contentAlignment = Alignment.Center) {
                if (glowAlpha > 0f) {
                    Box(modifier = Modifier.size(500.dp).graphicsLayer { alpha = glowAlpha }.background(Brush.radialGradient(colors = listOf(Color(0xFFFFF9C4), Color(0x88FFD54F), Color.Transparent)), shape = CircleShape))
                }
                val config = androidx.compose.ui.platform.LocalConfiguration.current
                val dialogWidth = (config.screenWidthDp * 0.75f).coerceAtMost(320f).dp
                val dialogFullHeight = dialogWidth * (1402f / 1122f)

                Box(modifier = Modifier.width(dialogWidth).height(dialogFullHeight * unrollAnim).scale(scaleAnim).clip(androidx.compose.ui.graphics.RectangleShape)) {
                    Box(modifier = Modifier.width(dialogWidth).height(dialogFullHeight)) {
                        // MENGIRIM TEKS PANJANG ("SLICE - Violations") SAAT TERBUKA
                        NinjaScrollContentRenderer(cardWidth = dialogWidth, fullCardHeight = dialogFullHeight, headerTitle = fullTitle, headerIconRes = headerIconRes, items = items, bgImageRes = bgImageRes)
                    }
                }
            }
        }
    }
}

@Composable
private fun RolledScrollThumbnail(
    headerTitle: String,
    headerIconRes: Int,
    headerPlaqueColors: List<Color>,
    modifier: Modifier = Modifier
) {
    val baseColor = headerPlaqueColors.firstOrNull() ?: Color(0xFF2C3E50)
    val midColor = headerPlaqueColors.getOrNull(1) ?: baseColor

    val randomPhaseOffset = remember { (0..360).random().toFloat() }
    val randomRollDuration = remember { (6000..8500).random() }
    val randomFloatDuration = remember { (1300..1900).random() }
    val randomFloatStartOffset = remember { (0..1000).random() }

    val infiniteTransition = rememberInfiniteTransition(label = "rolling_scroll")
    
    val rollAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(randomRollDuration, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wheel_spin"
    )
    
    val currentAngle = (rollAngle + randomPhaseOffset) % 360f
    
    val floatYAnim by infiniteTransition.animateFloat(
        initialValue = -2.5f,
        targetValue = 2.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(randomFloatDuration, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
            initialStartOffset = androidx.compose.animation.core.StartOffset(offsetMillis = randomFloatStartOffset)
        ),
        label = "float_anim"
    )

    val angleRad1 = currentAngle * (Math.PI / 180.0)
    val isLabel1Visible = Math.cos(angleRad1) >= 0 
    val label1YOffset = (Math.sin(angleRad1) * 19).toFloat().dp 
    val label1SquishY = Math.cos(angleRad1).toFloat().coerceAtLeast(0.001f)

    val angleRad2 = (currentAngle + 180f) * (Math.PI / 180.0)
    val isLabel2Visible = Math.cos(angleRad2) >= 0 
    val label2YOffset = (Math.sin(angleRad2) * 19).toFloat().dp 
    val label2SquishY = Math.cos(angleRad2).toFloat().coerceAtLeast(0.001f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(40.dp) // Sedikit ditambah tingginya agar lebih lega
            .offset(y = floatYAnim.dp),
        contentAlignment = Alignment.Center
    ) {
        // --- 1. GAGANG KAYU UTUH ---
        Box(
            modifier = Modifier
                .fillMaxWidth(0.95f) 
                .height(14.dp) 
                .clip(RoundedCornerShape(4.dp))
                .background(Brush.verticalGradient(colors = listOf(Color(0xFF9E775B), Color(0xFFD4A373), Color(0xFF6B4226))))
                .border(0.8.dp, Color(0xFF5C3A21), RoundedCornerShape(4.dp))
        )
        
        // --- 2. BADAN SILINDER KERTAS ---
        Box(
            modifier = Modifier
                .fillMaxWidth(0.82f) // Lebar kertas sedikit diperlebar
                .height(32.dp) // Tinggi kertas disesuaikan
                .shadow(4.dp, RoundedCornerShape(4.dp), spotColor = Color.Black)
                .clip(RoundedCornerShape(4.dp))
                .background(Brush.verticalGradient(colors = listOf(baseColor, midColor, baseColor)))
                .border(0.5.dp, Color(0x33FFFFFF), RoundedCornerShape(4.dp))
        ) {
            // Tali Pengikat Merah
            Row(modifier = Modifier.fillMaxHeight().padding(start = 10.dp), horizontalArrangement = Arrangement.spacedBy(1.dp)) {
                Box(modifier = Modifier.width(1.dp).fillMaxHeight().background(Color(0xFFD84315)))
                Box(modifier = Modifier.width(1.dp).fillMaxHeight().background(Color(0xFFD84315)))
            }
            
            // --- CETAK LABEL 1 (DIFIXING AGAR TIDAK KELUAR BOX) ---
            if (isLabel1Visible) {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset(y = label1YOffset)
                        .graphicsLayer { scaleY = label1SquishY }
                        .fillMaxWidth(0.90f) // Diperlebar dari 0.82f menjadi 0.90f agar muat
                        .height(20.dp) // Tinggi diperbesar dari 16.dp ke 20.dp
                        .shadow(2.dp, RoundedCornerShape(3.dp))
                        .clip(RoundedCornerShape(3.dp))
                        .background(Brush.verticalGradient(colors = listOf(Color(0xFFFFF176), Color(0xFFFFCA28), Color(0xFFFFB300))))
                        .border(1.dp, Color(0xFFB07D00), RoundedCornerShape(3.dp)) 
                        .padding(horizontal = 4.dp, vertical = 1.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically, 
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Image(
                            painter = painterResource(id = headerIconRes), 
                            contentDescription = null, 
                            modifier = Modifier
                                .size(11.dp),
                            contentScale = ContentScale.Fit
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = headerTitle, 
                            color = Color(0xFF4E342E), 
                            fontSize = 7.5.sp, 
                            fontWeight = FontWeight.Black, 
                            fontFamily = FontFamily.Serif, 
                            letterSpacing = 0.1.sp, 
                            maxLines = 1, 
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.offset(y = (-1.5).dp) // <-- Dinaikkan ke atas agar benar-benar pas di tengah (center)
                        )
                    }
                }
            }

            // --- CETAK LABEL 2 (DIFIXING AGAR TIDAK KELUAR BOX) ---
            if (isLabel2Visible) {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset(y = label2YOffset)
                        .graphicsLayer { scaleY = label2SquishY }
                        .fillMaxWidth(0.90f) 
                        .height(20.dp) 
                        .shadow(2.dp, RoundedCornerShape(3.dp))
                        .clip(RoundedCornerShape(3.dp))
                        .background(Brush.verticalGradient(colors = listOf(Color(0xFFFFF176), Color(0xFFFFCA28), Color(0xFFFFB300))))
                        .border(1.dp, Color(0xFFB07D00), RoundedCornerShape(3.dp)) 
                        .padding(horizontal = 4.dp, vertical = 1.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically, 
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Image(
                            painter = painterResource(id = headerIconRes), 
                            contentDescription = null, 
                            modifier = Modifier
                                .size(11.dp),
                            contentScale = ContentScale.Fit
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = headerTitle, 
                            color = Color(0xFF4E342E), 
                            fontSize = 7.5.sp, 
                            fontWeight = FontWeight.Black, 
                            fontFamily = FontFamily.Serif, 
                            letterSpacing = 0.1.sp, 
                            maxLines = 1, 
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.offset(y = (-1.5).dp) // <-- Dinaikkan ke atas agar benar-benar pas di tengah (center)
                        )
                    }
                }
            }

            // --- SHADOW OVERLAY 3D ---
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.55f),
                                Color.Transparent,
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.65f)
                            )
                        )
                    )
            )
        }
    }
}

@Composable
private fun NinjaScrollContentRenderer(
    cardWidth: Dp,
    fullCardHeight: Dp,
    headerTitle: String,
    headerIconRes: Int,
    items: List<RuleItemData>,
    bgImageRes: Int?
) {
    val scaleFactor = (cardWidth.value / 240f).coerceIn(0.7f, 1.8f)
    if (bgImageRes != null) {
        Image(
            painter = painterResource(id = bgImageRes),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds,
            alignment = Alignment.TopCenter
        )
        
        // Header Plaque
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    top = fullCardHeight * 0.130f,
                    start = cardWidth * 0.165f,
                    end = cardWidth * 0.165f
                )
                .height(fullCardHeight * 0.085f),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(id = headerIconRes),
                    contentDescription = null,
                    modifier = Modifier.size((14f * scaleFactor).dp),
                    contentScale = ContentScale.Fit
                )
                Spacer(modifier = Modifier.width((4.5f * scaleFactor).dp))
                Text(
                    text = headerTitle,
                    color = Color.White,
                    fontSize = (10.5f * scaleFactor).sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.4.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
  
        // Item List Container - Diseragamkan jarak dan posisinya dari atas
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    top = fullCardHeight * 0.245f,
                    start = cardWidth * 0.155f,
                    end = cardWidth * 0.155f,
                    bottom = fullCardHeight * 0.14f
                )
                .fillMaxHeight(), 
            verticalArrangement = Arrangement.spacedBy((4f * scaleFactor).dp, Alignment.Top) // <-- Menggunakan jarak tetap dan rata atas
        ) {
            for (item in items) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape((5f * scaleFactor).dp))
                        .background(Color(0x35FFF8EA))
                        .border(
                            (0.6f * scaleFactor).dp,
                            Color(0x38B08953),
                            RoundedCornerShape((5f * scaleFactor).dp)
                        )
                        .padding(
                            horizontal = (6f * scaleFactor).dp,
                            vertical = (4f * scaleFactor).dp // Padding vertikal sedikit disesuaikan agar proporsional
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = item.iconRes),
                        contentDescription = null,
                        modifier = Modifier.size((22f * scaleFactor).dp),
                        contentScale = ContentScale.Fit
                    )
                    Spacer(modifier = Modifier.width((6.5f * scaleFactor).dp))
                    Text(
                        text = item.name,
                        color = Color(0xFF1F160E),
                        fontSize = (9.5f * scaleFactor).sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.1.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun StartShiftGlowingButton(
    onClick: () -> Unit, pulseScale: Float, isLandscape: Boolean, currentLanguage: String, onPositioned: ((Rect) -> Unit)?, modifier: Modifier = Modifier
) {
    val buttonWidth = if (isLandscape) 234.dp else 260.dp
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val isHovered by interactionSource.collectIsHoveredAsState()
    val interactiveScale by animateFloatAsState(targetValue = if (isPressed) 1.07f else if (isHovered) 1.04f else 1.0f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow), label = "")
    val buttonDrawable = when (currentLanguage.lowercase()) { "ja" -> R.drawable.btn_start_shift_jp; "in", "id" -> R.drawable.btn_start_shift_id; else -> R.drawable.btn_start_shift_eg }
    Image(
        painter = painterResource(id = buttonDrawable), contentDescription = null,
        modifier = modifier.onGloballyPositioned { onPositioned?.invoke(it.boundsInRoot()) }.scale(pulseScale * interactiveScale).width(buttonWidth).clickable(interactionSource = interactionSource, indication = null, onClick = onClick).testTag("start_shift_button"),
        contentScale = ContentScale.Fit
    )
}

private fun lineIntersectsRectMenu(p1: Offset, p2: Offset, rect: Rect): Boolean {
    if (rect.contains(p1) || rect.contains(p2)) return true
    val minX = minOf(p1.x, p2.x); val maxX = maxOf(p1.x, p2.x); val minY = minOf(p1.y, p2.y); val maxY = maxOf(p1.y, p2.y)
    if (maxX < rect.left || minX > rect.right || maxY < rect.top || minY > rect.bottom) return false
    return lineIntersectsLineMenu(p1, p2, Offset(rect.left, rect.top), Offset(rect.right, rect.top)) || lineIntersectsLineMenu(p1, p2, Offset(rect.left, rect.bottom), Offset(rect.right, rect.bottom)) || lineIntersectsLineMenu(p1, p2, Offset(rect.left, rect.top), Offset(rect.left, rect.bottom)) || lineIntersectsLineMenu(p1, p2, Offset(rect.right, rect.top), Offset(rect.right, rect.bottom))
}

private fun lineIntersectsLineMenu(a1: Offset, a2: Offset, b1: Offset, b2: Offset): Boolean {
    val d = (a2.x - a1.x) * (b2.y - b1.y) - (a2.y - a1.y) * (b2.x - b1.x)
    if (d == 0f) return false
    val u = ((b1.x - a1.x) * (b2.y - b1.y) - (b1.y - a1.y) * (b2.x - b1.x)) / d
    val v = ((b1.x - a1.x) * (a2.y - a1.y) - (b1.y - a1.y) * (a2.x - a1.x)) / d
    return (u in 0f..1f) && (v in 0f..1f)
}