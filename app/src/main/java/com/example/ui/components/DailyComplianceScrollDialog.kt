package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.notification.DailyComplianceMessage
import com.example.notification.DailyComplianceRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DailyComplianceScrollDialog(
    isOpen: Boolean,
    currentLanguage: String = "id",
    initialMessageId: Int? = null,
    onDismiss: () -> Unit,
    onComplyConfirmed: () -> Unit = {}
) {
    if (!isOpen) return

    val context = LocalContext.current
    val todayMessage = remember(initialMessageId) {
        if (initialMessageId != null && initialMessageId > 0) {
            DailyComplianceRepository.getMessageById(initialMessageId)
        } else {
            DailyComplianceRepository.getTodayMessage()
        }
    }

    var currentMessageIndex by remember {
        val initialIdx = DailyComplianceRepository.ALL_MESSAGES.indexOfFirst { it.id == todayMessage.id }
        mutableIntStateOf(if (initialIdx >= 0) initialIdx else 0)
    }

    val activeMessage = DailyComplianceRepository.ALL_MESSAGES[currentMessageIndex]

    // Check if custom bg_notif image exists in resources (uploaded by user or asset)
    val bgNotifResId = remember {
        val id = context.resources.getIdentifier("bg_notif", "drawable", context.packageName)
        if (id != 0) id else null
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xD0030B14))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {} // Consume click inside dialog
                    )
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                ScrollParchmentCard(
                    message = activeMessage,
                    currentLanguage = currentLanguage,
                    bgNotifResId = bgNotifResId,
                    onDismiss = onDismiss,
                    onComply = {
                        DailyComplianceRepository.markTodayRead(context)
                        onComplyConfirmed()
                        onDismiss()
                    }
                )
            }
        }
    }
}

@Composable
private fun ScrollParchmentCard(
    message: DailyComplianceMessage,
    currentLanguage: String,
    bgNotifResId: Int?,
    onDismiss: () -> Unit,
    onComply: () -> Unit
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth(0.96f)
            .widthIn(max = 840.dp)
            .fillMaxHeight(0.92f),
        contentAlignment = Alignment.Center
    ) {
        val cardWidth = maxWidth
        val cardHeight = maxHeight
        val isLandscape = cardWidth > cardHeight

        // Background Scroll Canvas:
        // If user's uploaded bg_notif is present in drawable, render it; otherwise use bg_ninja_scroll as fallback
        if (bgNotifResId != null) {
            Image(
                painter = painterResource(id = bgNotifResId),
                contentDescription = "Background Scroll",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds
            )
        } else {
            // High-fidelity Procedural Ninja Scroll matching the attachment
            ProceduralNinjaScrollBackground(modifier = Modifier.fillMaxSize())
        }

        // Inner Scroll Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = cardHeight * 0.075f,
                    bottom = cardHeight * 0.085f,
                    start = cardWidth * 0.08f,
                    end = cardWidth * 0.08f
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Gold Plaque Header
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(if (isLandscape) 40.dp else 46.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFFFFDF79), Color(0xFFE5A713), Color(0xFFB87805))
                        )
                    )
                    .border(1.5.dp, Color(0xFF6B4505), RoundedCornerShape(6.dp))
                    .shadow(4.dp, RoundedCornerShape(6.dp)),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(horizontal = 12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = null,
                        tint = Color(0xFF3E1E02),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = when (currentLanguage.lowercase()) {
                            "en" -> "DAILY COMPLIANCE BRIEFING • 07:30 AM"
                            "ja" -> "毎朝のコンプライアンス訓示 • 午前7:30"
                            else -> "EDUKASI KEPATUHAN HARIAN • 07:30 PAGI"
                        },
                        color = Color(0xFF2C1400),
                        fontSize = if (isLandscape) 13.sp else 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 2. Main Parchment Body with Left Ninja Mascot & Right/Center Educational Content
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.Top
            ) {
                // Left Column: Cute Ninja Mascot & Category Icon (Only in landscape or larger width)
                if (isLandscape || cardWidth > 420.dp) {
                    Column(
                        modifier = Modifier
                            .width(if (isLandscape) 140.dp else 100.dp)
                            .fillMaxHeight(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Top
                    ) {
                        // Category Icon Pill
                        Box(
                            modifier = Modifier
                                .size(if (isLandscape) 64.dp else 52.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0x33B71C1C))
                                .border(1.2.dp, Color(0x77C62828), RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = message.iconRes),
                                contentDescription = null,
                                modifier = Modifier.size(if (isLandscape) 48.dp else 40.dp),
                                contentScale = ContentScale.Fit
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Chibi Mascot Image / Avatar
                        Image(
                            painter = painterResource(id = R.drawable.mascot_owl_transparent),
                            contentDescription = "Compliance Ninja Mascot",
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp),
                            contentScale = ContentScale.Fit
                        )

                        Text(
                            text = "Bang Patuh",
                            color = Color(0xFF5D4037),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Right Column: Scrollable educational content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Category & Date Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color(0xFFB71C1C),
                            shadowElevation = 2.dp
                        ) {
                            Text(
                                text = message.getLocalizedBadge(currentLanguage),
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                            )
                        }

                        val dateFormatted = remember {
                            val sdf = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
                            sdf.format(Date())
                        }
                        Text(
                            text = "🗓️ $dateFormatted • 07:30",
                            color = Color(0xFF5D4037),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Headline Title
                    Text(
                        text = message.getLocalizedTitle(currentLanguage),
                        color = Color(0xFF1B0000),
                        fontSize = if (isLandscape) 17.sp else 15.sp,
                        fontWeight = FontWeight.Black,
                        lineHeight = 21.sp
                    )

                    // Full Educational Message
                    Text(
                        text = message.getLocalizedFullMessage(currentLanguage),
                        color = Color(0xFF2E1C0C),
                        fontSize = if (isLandscape) 12.5.sp else 12.sp,
                        lineHeight = 17.sp,
                        fontWeight = FontWeight.Medium
                    )

                    // Golden Rules Card
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0x35E6B800))
                            .border(1.dp, Color(0x60B8860B), RoundedCornerShape(8.dp))
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = Color(0xFF795548),
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = when (currentLanguage.lowercase()) {
                                    "en" -> "GOLDEN COMPLIANCE RULES:"
                                    "ja" -> "コンプライアンス遵守の鉄則:"
                                    else -> "PRINSIP EMAS KEPATUHAN:"
                                },
                                color = Color(0xFF3E2723),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }

                        for (rule in message.getLocalizedGoldenRules(currentLanguage)) {
                            Row(
                                verticalAlignment = Alignment.Top,
                                modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF2E7D32),
                                    modifier = Modifier
                                        .size(14.dp)
                                        .padding(top = 2.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = rule,
                                    color = Color(0xFF1B1B1B),
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    // Sanksi & Bahaya Hukum Box
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0x28FF1744))
                            .border(1.dp, Color(0x55D50000), RoundedCornerShape(8.dp))
                            .padding(8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color(0xFFB71C1C),
                            modifier = Modifier
                                .size(16.dp)
                                .padding(top = 1.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = when (currentLanguage.lowercase()) {
                                    "en" -> "LEGAL & CORPORATE SANCTIONS:"
                                    "ja" -> "制裁および法的責任:"
                                    else -> "SANKSI & KONSEKUENSI HUKUM:"
                                },
                                color = Color(0xFFB71C1C),
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Black
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = message.getLocalizedConsequences(currentLanguage),
                                color = Color(0xFF3E2723),
                                fontSize = 11.sp,
                                lineHeight = 15.sp,
                                fontWeight = FontWeight.Normal
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // 3. Action Buttons Row: Close & "SAYA PAHAM & SIAP PATUH"
            Row(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .height(if (isLandscape) 42.dp else 46.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Secondary Close Button
                Surface(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0x2A3E2723),
                    border = BorderStroke(1.dp, Color(0x555D4037)),
                    modifier = Modifier.width(if (isLandscape) 110.dp else 90.dp).fillMaxHeight()
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = when (currentLanguage.lowercase()) {
                                "en" -> "Close"
                                "ja" -> "閉じる"
                                else -> "Tutup"
                            },
                            color = Color(0xFF3E2723),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Primary Comply Button
                val complyInteraction = remember { MutableInteractionSource() }
                val isPressed by complyInteraction.collectIsPressedAsState()
                val animatedScale by animateFloatAsState(
                    targetValue = if (isPressed) 0.96f else 1.0f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                    label = "btn_scale"
                )

                Surface(
                    onClick = onComply,
                    interactionSource = complyInteraction,
                    shape = RoundedCornerShape(10.dp),
                    color = Color.Transparent,
                    border = BorderStroke(1.2.dp, Color(0xFFFFD54F)),
                    shadowElevation = 6.dp,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .graphicsLayer {
                            scaleX = animatedScale
                            scaleY = animatedScale
                        }
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFFB71C1C), Color(0xFFD32F2F), Color(0xFFC62828))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = when (currentLanguage.lowercase()) {
                                    "en" -> "I UNDERSTAND & WILL COMPLY"
                                    "ja" -> "理解し、規範を遵守します"
                                    else -> "SAYA PAHAM & SIAP PATUH"
                                },
                                color = Color.White,
                                fontSize = if (isLandscape) 13.sp else 12.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }
            }
        }

        // Close 'X' Button at Top-Right
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 10.dp, end = 10.dp)
                .size(34.dp)
                .clip(CircleShape)
                .background(Color(0xDD3E2723))
                .border(1.2.dp, Color(0xFFFFD54F), CircleShape)
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close",
                tint = Color(0xFFFFD54F),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

/**
 * High-fidelity procedural ancient Japanese ninja scroll background.
 * Perfectly mirrors the aesthetic in the user's attachment:
 * - Mahogany top and bottom wooden scroll rods with wrapped red cord knots
 * - Hanging silk cord tassels on both sides
 * - Parchment textured central surface
 */
@Composable
private fun ProceduralNinjaScrollBackground(modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        // Parchment base
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 18.dp, horizontal = 12.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFFFBF4DF),
                            Color(0xFFF3E7C4),
                            Color(0xFFEBDAB0),
                            Color(0xFFF4E8CA)
                        )
                    )
                )
                .border(1.5.dp, Color(0xFF8D6E63), RoundedCornerShape(8.dp))
                .shadow(12.dp, RoundedCornerShape(8.dp))
        )

        // Top Wooden Roller Rod
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF8D4004), Color(0xFF4A1F02), Color(0xFF2A1000))
                        )
                    )
                    .border(1.dp, Color(0xFFBCAAA4), RoundedCornerShape(12.dp))
            )
        }

        // Bottom Wooden Roller Rod
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF8D4004), Color(0xFF4A1F02), Color(0xFF2A1000))
                        )
                    )
                    .border(1.dp, Color(0xFFBCAAA4), RoundedCornerShape(12.dp))
            )
        }

        // Left Red Cord & Tassel Decoration
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 6.dp, top = 2.dp)
                .size(24.dp)
                .clip(CircleShape)
                .background(Color(0xFFD50000))
                .border(1.5.dp, Color(0xFFFFD54F), CircleShape)
        )

        // Right Red Cord & Tassel Decoration
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 6.dp, top = 2.dp)
                .size(24.dp)
                .clip(CircleShape)
                .background(Color(0xFFD50000))
                .border(1.5.dp, Color(0xFFFFD54F), CircleShape)
        )
    }
}
