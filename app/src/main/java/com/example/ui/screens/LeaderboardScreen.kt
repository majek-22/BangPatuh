package com.example.ui.screens

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.data.LeaderboardItem
import com.example.data.LevelConfig
import com.example.data.local.GameSessionRecord
import com.example.data.local.UserStats
import com.example.ui.components.CategoryDistributionDonutChart
import com.example.ui.components.ScoreProgressionLineChart
import com.example.ui.theme.GoldSecondary
import com.example.ui.theme.MintSuccess
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun LeaderboardScreen(
    currentUser: String?,
    userStats: UserStats?,
    leaderboardEntries: List<LeaderboardItem>,
    isOffline: Boolean,
    isLoading: Boolean,
    errorMessage: String? = null,
    recentSessions: List<GameSessionRecord> = emptyList(),
    initialTab: Int = 0,
    currentLanguage: String = "en",
    onRefresh: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isId = currentLanguage.equals("in", ignoreCase = true) || currentLanguage.equals("id", ignoreCase = true)
    var selectedTab by remember(initialTab) { mutableIntStateOf(initialTab) } // 0 = Global Top 100, 1 = My Stats

    val bgGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF0D1B2A),
            Color(0xFF1B263B),
            Color(0xFF0A1118)
        )
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(bgGradient)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color(0x33FFFFFF))
                            .testTag("leaderboard_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = stringResource(R.string.leaderboard_title),
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (selectedTab == 0) {
                    IconButton(
                        onClick = onRefresh,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color(0x3364B5F6))
                            .testTag("leaderboard_refresh_btn")
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color(0xFFFFD54F),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh",
                                tint = Color.White
                            )
                        }
                    }
                }
            }

            // Tab Row
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color(0x33000000),
                contentColor = Color.White,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = Color(0xFFFFD54F)
                    )
                },
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            text = stringResource(R.string.leaderboard_tab_global),
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == 0) Color(0xFFFFD54F) else Color(0xFFB0BEC5)
                        )
                    },
                    modifier = Modifier.testTag("tab_global_leaderboard")
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            text = stringResource(R.string.leaderboard_tab_my_stats),
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == 1) Color(0xFFFFD54F) else Color(0xFFB0BEC5)
                        )
                    },
                    modifier = Modifier.testTag("tab_my_stats")
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (selectedTab == 0) {
                // GLOBAL TOP 100 TAB
                GlobalLeaderboardTab(
                    entries = leaderboardEntries,
                    currentUser = currentUser,
                    isOffline = isOffline,
                    isLoading = isLoading,
                    errorMessage = errorMessage,
                    currentLanguage = currentLanguage,
                    onRefresh = onRefresh
                )
            } else {
                // MY STATS TAB
                MyStatsTab(
                    currentUser = currentUser,
                    userStats = userStats,
                    leaderboardEntries = leaderboardEntries,
                    recentSessions = recentSessions,
                    currentLanguage = currentLanguage
                )
            }
        }
    }
}

@Composable
private fun GlobalLeaderboardTab(
    entries: List<LeaderboardItem>,
    currentUser: String?,
    isOffline: Boolean,
    isLoading: Boolean,
    errorMessage: String? = null,
    currentLanguage: String = "en",
    onRefresh: () -> Unit = {}
) {
    val isId = currentLanguage.equals("in", ignoreCase = true) || currentLanguage.equals("id", ignoreCase = true)
    val isJa = currentLanguage.equals("ja", ignoreCase = true)
    val isServerOffline = isOffline || !errorMessage.isNullOrBlank()

    Column(modifier = Modifier.fillMaxSize()) {
        // Offline Banner (Server Offline)
        if (isServerOffline) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0x33FF5252))
                    .border(1.dp, Color(0x66FF5252), RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CloudOff,
                    contentDescription = null,
                    tint = Color(0xFFFF5252),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (isJa) "サーバーオフライン" else "Server Offline",
                            color = Color(0xFFFF8A80),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0x33FF5252)
                        ) {
                            Text(
                                text = "DISCONNECT",
                                color = Color(0xFFFF8A80),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (entries.isNotEmpty()) {
                            stringResource(R.string.leaderboard_offline_banner)
                        } else {
                            if (isJa) "Firebaseサーバーに接続できません。ローカルデータを表示しています。"
                            else if (isId) "Tidak dapat terhubung ke server Firebase. Menampilkan data lokal."
                            else "Unable to connect to Firebase server. Showing local data."
                        },
                        color = Color(0xFFFFCCBC),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        if (entries.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color(0xFFFFD54F))
                } else if (isServerOffline) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Color(0x22FF5252))
                                .border(1.5.dp, Color(0x66FF5252), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudOff,
                                contentDescription = null,
                                tint = Color(0xFFFF5252),
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = if (isJa) "サーバーオフライン" else "Server Offline",
                            color = Color(0xFFFF8A80),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (isJa) "Firebaseサーバーに接続できません。\nインターネット接続を確認して再試行してください。"
                            else if (isId) "Tidak dapat terhubung ke server Firebase.\nSilakan periksa koneksi internet dan coba lagi."
                            else "Unable to connect to Firebase server.\nPlease check your internet connection and try again.",
                            color = Color(0xFFB0BEC5),
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onRefresh,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isJa) "再試行" else if (isId) "COBA LAGI" else "RETRY",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                } else {
                    Text(
                        text = stringResource(R.string.leaderboard_empty),
                        color = Color(0xFF90CAF9),
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(entries) { item ->
                    val isSelf = currentUser != null && item.username.equals(currentUser, ignoreCase = true)
                    LeaderboardRow(item = item, isSelf = isSelf, currentLanguage = currentLanguage)
                }
            }
        }
    }
}

@Composable
private fun LeaderboardRow(item: LeaderboardItem, isSelf: Boolean, currentLanguage: String = "en") {
    val rankBadgeColor = when (item.rank) {
        1 -> Color(0xFFFFD54F) // Gold
        2 -> Color(0xFFCFD8DC) // Silver
        3 -> Color(0xFFFF8A65) // Bronze
        else -> Color(0xFF90CAF9)
    }

    val cardBg = if (isSelf) Color(0xDD1E3A5F) else Color(0x99132238)
    val cardBorder = if (isSelf) Color(0xFFFFD54F) else Color(0x22FFFFFF)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, cardBorder, RoundedCornerShape(12.dp))
            .testTag("leaderboard_row_${item.rank}"),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rank badge
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(rankBadgeColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "#${item.rank}",
                    color = rankBadgeColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.username,
                        color = if (isSelf) Color(0xFFFFD54F) else Color.White,
                        fontSize = 15.sp,
                        fontWeight = if (isSelf) FontWeight.Black else FontWeight.Bold
                    )
                    if (isSelf) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFFFFD54F))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text("YOU", color = Color(0xFF0F172A), fontSize = 9.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }
                val diffLocalized = when (currentLanguage.lowercase()) {
                    "ja" -> when {
                        item.difficulty.contains("auto", ignoreCase = true) -> "自動"
                        item.difficulty.contains("hard", ignoreCase = true) -> "ハード"
                        item.difficulty.contains("normal", ignoreCase = true) -> "ノーマル"
                        else -> item.difficulty
                    }
                    "in", "id" -> when {
                        item.difficulty.contains("auto", ignoreCase = true) -> "Otomatis"
                        item.difficulty.contains("hard", ignoreCase = true) -> "Sulit"
                        item.difficulty.contains("normal", ignoreCase = true) -> "Normal"
                        else -> item.difficulty
                    }
                    else -> item.difficulty
                }
                val levelLabel = when (currentLanguage.lowercase()) {
                    "ja" -> "レベル ${item.bestScoreLevel} • $diffLocalized"
                    "in", "id" -> "Level ${item.bestScoreLevel} • $diffLocalized"
                    else -> "Lvl ${item.bestScoreLevel} • $diffLocalized"
                }
                Text(
                    text = levelLabel,
                    color = Color(0xFF90CAF9),
                    fontSize = 11.sp
                )
            }

            // Score
            Text(
                text = "${item.bestScore}",
                color = Color(0xFFFFD54F),
                fontSize = 16.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
private fun MyStatsTab(
    currentUser: String?,
    userStats: UserStats?,
    leaderboardEntries: List<LeaderboardItem>,
    recentSessions: List<GameSessionRecord> = emptyList(),
    currentLanguage: String = "en"
) {
    val stats = userStats ?: UserStats(username = currentUser ?: "Officer")
    val userRank = leaderboardEntries.find { it.username.equals(currentUser, ignoreCase = true) }?.rank
    val avatarRes = AvatarHelper.getAvatarRes(stats.avatarId, stats.username)
    val rankTitle = when (currentLanguage.lowercase()) {
        "ja" -> stats.getRankTitle("ja")
        "in", "id" -> stats.getRankTitle("in")
        else -> stringResource(id = stats.getRankTitleRes())
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Global Rank & Personal Best Banner with Avatar
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.5.dp, GoldSecondary.copy(alpha = 0.8f), RoundedCornerShape(18.dp)),
            colors = CardDefaults.cardColors(containerColor = Color(0xCC1A2C46)),
            shape = RoundedCornerShape(18.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(Color(0x3364B5F6))
                        .border(2.dp, GoldSecondary, CircleShape)
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.foundation.Image(
                        painter = painterResource(id = avatarRes),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (userRank != null) {
                                stringResource(R.string.stats_global_rank_top100, userRank)
                            } else {
                                stringResource(R.string.stats_global_rank_outside)
                            },
                            color = GoldSecondary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0x334FCB8F)
                        ) {
                            Text(
                                text = rankTitle,
                                color = MintSuccess,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "${stringResource(R.string.stats_personal_best)}: ${stats.overallBestScore} pts",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    val statsDiffLocalized = when (currentLanguage.lowercase()) {
                        "ja" -> when {
                            stats.bestScoreDifficulty.contains("auto", ignoreCase = true) -> "自動"
                            stats.bestScoreDifficulty.contains("hard", ignoreCase = true) -> "ハード"
                            stats.bestScoreDifficulty.contains("normal", ignoreCase = true) -> "ノーマル"
                            else -> stats.bestScoreDifficulty
                        }
                        "in", "id" -> when {
                            stats.bestScoreDifficulty.contains("auto", ignoreCase = true) -> "Otomatis"
                            stats.bestScoreDifficulty.contains("hard", ignoreCase = true) -> "Sulit"
                            stats.bestScoreDifficulty.contains("normal", ignoreCase = true) -> "Normal"
                            else -> stats.bestScoreDifficulty
                        }
                        else -> stats.bestScoreDifficulty
                    }
                    val achievedText = when (currentLanguage.lowercase()) {
                        "ja" -> "レベル ${stats.bestScoreLevel} (${statsDiffLocalized}) で達成"
                        "in", "id" -> "Dicapai pada Level ${stats.bestScoreLevel} (${statsDiffLocalized})"
                        else -> "Achieved on Level ${stats.bestScoreLevel} (${statsDiffLocalized})"
                    }
                    Text(
                        text = achievedText,
                        color = Color(0xFF90CAF9),
                        fontSize = 11.sp
                    )
                }
            }
        }

        // 1. Violation Categories Breakdown Donut Chart
        CategoryDistributionDonutChart(userStats = stats)

        // 2. Score Progression Line Chart
        ScoreProgressionLineChart(sessions = recentSessions)

        // 3. Lifetime Counters Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(18.dp)),
            colors = CardDefaults.cardColors(containerColor = Color(0xAA132238)),
            shape = RoundedCornerShape(18.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(R.string.stats_career_totals),
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                StatRow(label = stringResource(R.string.stats_games_played), value = "${stats.gamesPlayed}")
                StatRow(label = stringResource(R.string.stats_violations_sliced), value = "${stats.totalViolationsSliced}")
                StatRow(label = stringResource(R.string.stats_traps_avoided), value = "${stats.totalTrapsAvoided}")
                StatRow(label = stringResource(R.string.stats_traps_sliced), value = "${stats.totalTrapsSliced}")
                StatRow(label = stringResource(R.string.stats_best_combo_streak), value = "${stats.bestComboStreak}x")
            }
        }

        // 4. Level Mastery Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(18.dp)),
            colors = CardDefaults.cardColors(containerColor = Color(0xAA132238)),
            shape = RoundedCornerShape(18.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(R.string.stats_level_mastery),
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(10.dp))

                LevelConfig.ALL_LEVELS.forEach { level ->
                    val best = stats.getBestForLevel(level.levelNumber)
                    val stars = stats.getStarsForLevel(level.levelNumber)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Level ${level.levelNumber}: ${stringResource(level.nameRes)}",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Best: $best pts",
                                color = Color(0xFF90CAF9),
                                fontSize = 11.sp
                            )
                        }

                        Row {
                            for (i in 1..5) {
                                val earned = i <= stars
                                Icon(
                                    imageVector = if (earned) Icons.Default.Star else Icons.Default.StarBorder,
                                    contentDescription = null,
                                    tint = if (earned) GoldSecondary else Color(0x55FFFFFF),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 5. Recent Shift History
        if (recentSessions.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(18.dp)),
                colors = CardDefaults.cardColors(containerColor = Color(0xAA132238)),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(R.string.stats_recent_shifts_title),
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    val dateFormat = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())

                    recentSessions.take(5).forEach { session ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Mission ${session.levelNumber} • ${dateFormat.format(Date(session.timestamp))}",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "Akurasi: ${session.accuracyPercent}% (${session.violationsSliced} netralisir)",
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }

                            Text(
                                text = "${session.score} pts",
                                color = GoldSecondary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = Color(0xFFB0BEC5), fontSize = 13.sp)
        Text(text = value, color = Color(0xFFFFD54F), fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}
