package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.AvatarHelper
import com.example.data.LevelConfig
import com.example.data.local.UserStats
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.BackgroundGradientEnd
import com.example.ui.theme.CoralPrimary
import com.example.ui.theme.GoldSecondary
import com.example.ui.theme.MintSuccess
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.math.abs

@Composable
fun ProfileScreen(
    currentUser: String?,
    userStats: UserStats?,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val username = currentUser ?: "Officer"
    val stats = userStats ?: UserStats(username = username)
    val avatarId = if (stats.avatarId in 1..10) stats.avatarId else ((abs(username.hashCode()) % 10) + 1)
    val avatarRes = AvatarHelper.getAvatarRes(avatarId, username)
    val avatarTitle = AvatarHelper.getAvatarTitle(avatarId, username)
    val rankTitle = stringResource(id = stats.getRankTitleRes())
    val badgeIdString = String.format("%05d", abs(username.hashCode()) % 100000)

    val totalStars = (1..4).sumOf { stats.getStarsForLevel(it) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(BackgroundDark, BackgroundGradientEnd, Color(0xFF0F172A))
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0x22FFFFFF))
                        .border(1.dp, Color(0x3364B5F6), CircleShape)
                        .testTag("profile_back_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Text(
                    text = stringResource(R.string.profile_title),
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                // Placeholder to balance the row
                Spacer(modifier = Modifier.size(36.dp))
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Large Avatar Card with soft glowing depth
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, Color(0x4464B5F6), RoundedCornerShape(24.dp)),
                colors = CardDefaults.cardColors(containerColor = Color(0xCC1A233A)),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Avatar Image with ring
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .clip(CircleShape)
                            .background(Color(0x3364B5F6))
                            .border(2.5.dp, GoldSecondary.copy(alpha = 0.85f), CircleShape)
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = avatarRes),
                            contentDescription = stringResource(R.string.profile_avatar_title),
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Username
                    Text(
                        text = username,
                        color = TextPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Officer Badge ID
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0x33FFFFFF),
                        border = BorderStroke(1.dp, Color(0x22FFFFFF))
                    ) {
                        Text(
                            text = stringResource(R.string.profile_badge_id, badgeIdString),
                            color = Color(0xFF90CAF9),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Rank Banner
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0x2A4FCB8F),
                        border = BorderStroke(1.dp, MintSuccess.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = MintSuccess,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = rankTitle,
                                color = MintSuccess,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = avatarTitle,
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Career Highlights Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = Color(0x99162032)),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = stringResource(R.string.stats_career_totals),
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    ProfileStatRow(
                        label = stringResource(R.string.stats_personal_best),
                        value = "${stats.overallBestScore} pts",
                        highlight = true
                    )
                    ProfileStatRow(
                        label = stringResource(R.string.stats_games_played),
                        value = "${stats.gamesPlayed}"
                    )
                    ProfileStatRow(
                        label = stringResource(R.string.stats_violations_sliced),
                        value = "${stats.totalViolationsSliced}"
                    )
                    ProfileStatRow(
                        label = stringResource(R.string.stats_traps_avoided),
                        value = "${stats.totalTrapsAvoided}"
                    )
                    ProfileStatRow(
                        label = stringResource(R.string.stats_traps_sliced),
                        value = "${stats.totalTrapsSliced}"
                    )
                    ProfileStatRow(
                        label = stringResource(R.string.stats_best_combo_streak),
                        value = "${stats.bestComboStreak}x"
                    )
                    ProfileStatRow(
                        label = "Mission Stars Earned",
                        value = "$totalStars / 12 ★",
                        highlight = true
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Close button
            Button(
                onClick = onBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .testTag("profile_close_btn"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CoralPrimary,
                    contentColor = Color(0xFF0F172A)
                )
            ) {
                Text(
                    text = stringResource(R.string.profile_close),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ProfileStatRow(label: String, value: String, highlight: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = TextSecondary, fontSize = 13.sp)
        Text(
            text = value,
            color = if (highlight) GoldSecondary else TextPrimary,
            fontSize = 14.sp,
            fontWeight = if (highlight) FontWeight.Black else FontWeight.Bold
        )
    }
}
