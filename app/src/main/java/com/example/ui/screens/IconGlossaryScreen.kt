package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.data.ComplianceCategory
import com.example.data.GlossaryEntry
import com.example.data.GlossarySection
import com.example.ui.theme.CoralPrimary
import com.example.ui.theme.GoldSecondary
import com.example.ui.theme.MintSuccess

private enum class RulesTabFilter {
    ALL,
    VIOLATIONS,
    LEGITIMATE,
    TRAPS,
    BONUS
}

@Composable
fun IconGlossaryScreen(
    currentLanguage: String = "en",
    onToggleLanguage: (() -> Unit)? = null,
    onSelectLanguage: ((String) -> Unit)? = null,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isId = currentLanguage.equals("in", ignoreCase = true) || currentLanguage.equals("id", ignoreCase = true)
    val isJa = currentLanguage.equals("ja", ignoreCase = true)
    var selectedEntry by remember { mutableStateOf<GlossaryEntry?>(null) }
    var currentFilter by remember { mutableStateOf(RulesTabFilter.ALL) }

    val filteredEntries = remember(currentFilter) {
        when (currentFilter) {
            RulesTabFilter.ALL -> GlossaryEntry.ALL_ENTRIES
            RulesTabFilter.VIOLATIONS -> GlossaryEntry.ALL_ENTRIES.filter { it.section == GlossarySection.VIOLATIONS }
            RulesTabFilter.LEGITIMATE -> GlossaryEntry.ALL_ENTRIES.filter { it.section == GlossarySection.LEGITIMATE }
            RulesTabFilter.TRAPS -> GlossaryEntry.ALL_ENTRIES.filter { it.section == GlossarySection.TRAPS }
            RulesTabFilter.BONUS -> GlossaryEntry.ALL_ENTRIES.filter { it.section == GlossarySection.BONUS }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF09131F),
                        Color(0xFF0F1E32),
                        Color(0xFF070D16)
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color(0x22FFFFFF))
                            .border(1.dp, Color(0x4464B5F6), CircleShape)
                            .testTag("glossary_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = if (isJa) "戻る" else if (isId) "Kembali" else "Back",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (isJa) "プレイ規則・図鑑" else if (isId) "Aturan Permainan" else "Gameplay Rules",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = if (isJa) "ターゲット識別と得点メカニズム一覧" else if (isId) "Panduan identifikasi item & konsekuensi skor" else "Item identification guide & score mechanics",
                            color = Color(0xFF90CAF9),
                            fontSize = 11.sp
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Total Badges Counter Badge
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xDD091522),
                        border = BorderStroke(1.dp, GoldSecondary.copy(alpha = 0.5f)),
                        shadowElevation = 6.dp
                    ) {
                        Box(
                            modifier = Modifier
                                .height(34.dp)
                                .padding(horizontal = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isJa) "${GlossaryEntry.ALL_ENTRIES.size} 項目" else if (isId) "${GlossaryEntry.ALL_ENTRIES.size} Item" else "${GlossaryEntry.ALL_ENTRIES.size} Items",
                                color = GoldSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Game Style Filter Tabs
            RulesFilterTabs(
                currentFilter = currentFilter,
                currentLanguage = currentLanguage,
                onFilterSelected = { currentFilter = it }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Items List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredEntries, key = { it.category.name }) { entry ->
                    GlossaryCard(
                        entry = entry,
                        currentLanguage = currentLanguage,
                        onSelect = { selectedEntry = entry }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }

        // Icon Detail Popup Dialog (Rich Tactical Dossier)
        selectedEntry?.let { entry ->
            IconDetailDialog(
                entry = entry,
                currentLanguage = currentLanguage,
                onDismiss = { selectedEntry = null }
            )
        }
    }
}

/**
 * Filter tabs: All, Violations, Legitimate, Traps, Bonus
 */
@Composable
private fun RulesFilterTabs(
    currentFilter: RulesTabFilter,
    currentLanguage: String,
    onFilterSelected: (RulesTabFilter) -> Unit
) {
    val isId = currentLanguage.equals("in", ignoreCase = true) || currentLanguage.equals("id", ignoreCase = true)
    val isJa = currentLanguage.equals("ja", ignoreCase = true)
    val scrollState = rememberScrollState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        RulesTabFilter.entries.forEach { filter ->
            val isSelected = currentFilter == filter
            val label = when (filter) {
                RulesTabFilter.ALL -> if (isJa) "すべて" else if (isId) "Semua" else "All"
                RulesTabFilter.VIOLATIONS -> if (isJa) "違反行為" else if (isId) "Pelanggaran" else "Violations"
                RulesTabFilter.LEGITIMATE -> if (isJa) "正規手続き" else if (isId) "Dokumen Sah" else "Legitimate"
                RulesTabFilter.TRAPS -> if (isJa) "罠・誤報" else if (isId) "Jebakan" else "Traps"
                RulesTabFilter.BONUS -> if (isJa) "ボーナス" else if (isId) "Bonus" else "Bonus"
            }
            val count = when (filter) {
                RulesTabFilter.ALL -> GlossaryEntry.ALL_ENTRIES.size
                RulesTabFilter.VIOLATIONS -> GlossaryEntry.ALL_ENTRIES.count { it.section == GlossarySection.VIOLATIONS }
                RulesTabFilter.LEGITIMATE -> GlossaryEntry.ALL_ENTRIES.count { it.section == GlossarySection.LEGITIMATE }
                RulesTabFilter.TRAPS -> GlossaryEntry.ALL_ENTRIES.count { it.section == GlossarySection.TRAPS }
                RulesTabFilter.BONUS -> GlossaryEntry.ALL_ENTRIES.count { it.section == GlossarySection.BONUS }
            }
            val accentColor = when (filter) {
                RulesTabFilter.ALL -> Color(0xFF90CAF9)
                RulesTabFilter.VIOLATIONS -> Color(0xFFFFC857)
                RulesTabFilter.LEGITIMATE -> Color(0xFF64B5F6)
                RulesTabFilter.TRAPS -> Color(0xFFFF5252)
                RulesTabFilter.BONUS -> Color(0xFF00E676)
            }

            val bgColor by animateColorAsState(
                targetValue = if (isSelected) accentColor.copy(alpha = 0.25f) else Color(0x22132238),
                label = "tab_bg"
            )
            val borderColor by animateColorAsState(
                targetValue = if (isSelected) accentColor else Color(0x22FFFFFF),
                label = "tab_border"
            )

            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onFilterSelected(filter) }
                    .border(1.dp, borderColor, RoundedCornerShape(12.dp)),
                color = bgColor,
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = label,
                        color = if (isSelected) accentColor else Color(0xFFCFD8DC),
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = CircleShape,
                        color = if (isSelected) accentColor else Color(0x33FFFFFF)
                    ) {
                        Text(
                            text = "$count",
                            color = if (isSelected) Color(0xFF09131F) else Color(0xFF90A4AE),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Individual Rule Card with un-tinted 3D full-color graphic badge
 */
@Composable
private fun GlossaryCard(
    entry: GlossaryEntry,
    currentLanguage: String,
    onSelect: () -> Unit
) {
    val isId = currentLanguage.equals("in", ignoreCase = true) || currentLanguage.equals("id", ignoreCase = true)
    val isJa = currentLanguage.equals("ja", ignoreCase = true)
    val (badgeColor, actionLabel, pointText) = when {
        entry.category == ComplianceCategory.BONUS_CORRUPTOR -> Triple(
            Color(0xFF00E676),
            if (isJa) "❄️ フリーズ" else "❄️ FREEZE BONUS",
            if (isJa) "+10点 / 連撃 (フリーズ中)" else if (isId) "+10 Pts / Tebasan (Mode Freeze)" else "+10 Pts / Slice (Freeze Mode)"
        )
        entry.category == ComplianceCategory.SHIELD -> Triple(
            Color(0xFF00E676),
            if (isJa) "💎 シールド" else if (isId) "💎 PERISAI" else "💎 SHIELD",
            if (isJa) "+1ライフ & +25点" else if (isId) "+1 Nyawa & +25 Pts" else "+1 Life & +25 Pts"
        )
        entry.category == ComplianceCategory.SYSTEMIC_CORRUPTION -> Triple(
            Color(0xFFFFC857),
            if (isJa) "⚔️ 斬る" else if (isId) "⚔️ TEBAS" else "⚔️ SLICE",
            if (isJa) "+25点 × コンボ" else if (isId) "+25 Pts × Kombo" else "+25 Pts × Combo"
        )
        entry.section == GlossarySection.VIOLATIONS -> Triple(
            Color(0xFFFFC857),
            if (isJa) "⚔️ 斬る" else if (isId) "⚔️ TEBAS" else "⚔️ SLICE",
            if (isJa) "+10点 × コンボ" else if (isId) "+10 Pts × Kombo" else "+10 Pts × Combo"
        )
        entry.section == GlossarySection.LEGITIMATE -> Triple(
            Color(0xFF64B5F6),
            if (isJa) "🛡️ 守る" else if (isId) "🛡️ LINDUNGI" else "🛡️ PROTECT",
            if (isJa) "切断時 -1ライフ" else if (isId) "-1 Nyawa jika tertebas" else "-1 Life if sliced"
        )
        entry.section == GlossarySection.TRAPS -> Triple(
            Color(0xFFFF5252),
            if (isJa) "⚠️ 回避" else if (isId) "⚠️ JEBAKAN" else "⚠️ TRAP",
            if (isJa) "誤切断時 -10点" else if (isId) "-10 Pts jika tertebas" else "-10 Pts if sliced"
        )
        else -> Triple(
            Color(0xFF00E676),
            if (isJa) "💎 ボーナス" else "💎 BONUS",
            if (isJa) "+10点 / スラッシュ" else if (isId) "+10 Pts / Tebasan" else "+10 Pts / Slice"
        )
    }

    val discBgColor = when (entry.section) {
        GlossarySection.VIOLATIONS -> Color(0xFF3E2805)
        GlossarySection.LEGITIMATE -> Color(0xFF0C2C4D)
        GlossarySection.TRAPS -> Color(0xFF3E0E0E)
        GlossarySection.BONUS -> Color(0xFF052F1A)
    }

    val itemDisplayName = entry.category.getDisplayName(currentLanguage)
    val itemExplanation = entry.category.getExplanation(currentLanguage)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onSelect)
            .border(1.dp, badgeColor.copy(alpha = 0.45f), RoundedCornerShape(16.dp))
            .testTag("glossary_item_${entry.category.name}"),
        colors = CardDefaults.cardColors(containerColor = Color(0xDD0E1B2D)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Un-tinted 3D Badge container with matching gameplay disc background
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(discBgColor)
                    .border(1.5.dp, badgeColor, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                // High-resolution full-color illustration - NO TINT!
                Image(
                    painter = painterResource(id = entry.category.iconRes),
                    contentDescription = itemDisplayName,
                    modifier = Modifier.size(46.dp),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = itemDisplayName,
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    // Action Directive Pill
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = badgeColor.copy(alpha = 0.20f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, badgeColor.copy(alpha = 0.6f))
                    ) {
                        Text(
                            text = actionLabel,
                            color = badgeColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(3.dp))

                // Points & Consequence
                Text(
                    text = pointText,
                    color = badgeColor.copy(alpha = 0.9f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Real-world explanation
                Text(
                    text = itemExplanation,
                    color = Color(0xFFCFD8DC),
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = if (isJa) "タップして詳細を確認" else if (isId) "Ketuk untuk melihat detail" else "Tap to view details",
                tint = badgeColor.copy(alpha = 0.7f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

private data class RulesDialogInfo(
    val badgeColor: Color,
    val directive: String,
    val desc: String,
    val tip: String
)

/**
 * Rich Tactical Investigation Dossier Dialog
 */
@Composable
private fun IconDetailDialog(
    entry: GlossaryEntry,
    currentLanguage: String,
    onDismiss: () -> Unit
) {
    val isId = currentLanguage.equals("in", ignoreCase = true) || currentLanguage.equals("id", ignoreCase = true)
    val isJa = currentLanguage.equals("ja", ignoreCase = true)
    val info = when {
        entry.category == ComplianceCategory.BONUS_CORRUPTOR -> RulesDialogInfo(
            badgeColor = Color(0xFF00E676),
            directive = if (isJa) "❄️ 腐敗者ボーナス！時間を止めて連撃せよ！" else if (isId) "❄️ KORUPTOR BONUS! BEKUKAN & TEBAS!" else "❄️ BONUS CORRUPTOR! FREEZE & SLICE!",
            desc = if (isJa)
                "腐敗者ターゲットを斬ると5秒間時間が停止（フリーズモード）！停止中に素早く連続スラッシュせよ: 1撃ごとに+10点ボーナス！"
            else if (isId)
                "Tebas target koruptor ini untuk menghentikan waktu (Mode Freeze) selama 5 detik! Selama waktu membeku, tebas target ini sebanyak mungkin: setiap tebasan menghasilkan +10 poin bonus!"
            else
                "Slice this corruptor target to freeze time (Freeze Mode) for 5 seconds! While time is frozen, slash the corruptor as many times as possible: each slash awards +10 bonus points!",
            tip = if (isJa)
                "5秒間の停止時間を最大限に活用し、画面を素早く連続スワイプしてボーナス点を稼ぎましょう！"
            else if (isId)
                "Manfaatkan 5 detik waktu membeku untuk melakukan gesekan cepat beruntun demi memaksimalkan perolehan skor bonus!"
            else
                "Take advantage of the 5-second freeze to perform rapid multi-slashes and maximize your bonus score!"
        )
        entry.category == ComplianceCategory.SHIELD -> RulesDialogInfo(
            badgeColor = Color(0xFF00E676),
            directive = if (isJa) "💎 黄金シールド！必ず獲得せよ！" else if (isId) "💎 PERISAI EMAS! SEGERA AMBIL!" else "💎 GOLDEN SHIELD! CLAIM IT!",
            desc = if (isJa)
                "稀に出現する黄金シールドを斬ると、ライフが+1回復（最大4）し、+25点のボーナススコアを獲得できます！"
            else if (isId)
                "Tebas perisai emas langka ini untuk memulihkan +1 NYAWA (maksimal 4) dan memperoleh skor bonus +25 poin!"
            else
                "Slice this rare golden shield to restore +1 LIFE (max 4) and gain +25 bonus points!",
            tip = if (isJa)
                "黄金シールドは極めて貴重です。最優先で獲得して任務の継続時間を延ばしましょう！"
            else if (isId)
                "Perisai emas sangat berharga. Utamakan menebasnya untuk memperpanjang shift Anda!"
            else
                "Golden shields are very valuable. Prioritize slicing them to extend your shift!"
        )
        entry.category == ComplianceCategory.SYSTEMIC_CORRUPTION -> RulesDialogInfo(
            badgeColor = Color(0xFFFFC857),
            directive = if (isJa) "⚔️ 組織的腐敗！超高得点ターゲット！" else if (isId) "⚔️ KORUPSI SISTEMIK! POIN TINGGI!" else "⚔️ SYSTEMIC CORRUPTION! HIGH SCORE!",
            desc = if (isJa)
                "組織的腐敗は高得点（+25点×コンボ）。画面下へ逃す前に即座に一刀両断してください！"
            else if (isId)
                "Pelanggaran korupsi sistemik bernilai poin tinggi (+25 Poin × Kombo). Segera tebas sebelum lolos ke bawah batas layar!"
            else
                "High-value systemic corruption violation (+25 Pts × Combo). Slash immediately before it escapes past the bottom screen!",
            tip = if (isJa)
                "全違反項目中最大の基礎得点（+25点）を持ちます。決して見逃してはなりません！"
            else if (isId)
                "Memberikan skor dasar tertinggi (+25 Poin) di antara semua jenis pelanggaran. Jangan sampai terlewat!"
            else
                "Awards the highest base score (+25 Pts) among all violation types. Never let it pass!"
        )
        entry.section == GlossarySection.VIOLATIONS -> RulesDialogInfo(
            badgeColor = Color(0xFFFFC857),
            directive = if (isJa) "⚔️ 一刀両断せよ！" else if (isId) "⚔️ WAJIB DITEBAS!" else "⚔️ MUST SLASH!",
            desc = if (isJa)
                "画面下へ落ちる前に即座に斬り捨ててください。斬ると得点とコンボが加算されます。"
            else if (isId)
                "Segera tebas sebelum jatuh melewati batas layar. Menebas memberikan poin dan menaikkan kombo."
            else
                "Slash immediately before it falls past the screen. Slicing gives points and increases combo.",
            tip = if (isJa)
                "安全ゾーン内で集中して一撃を加えよ。画面外へ違反を逃がしてはならない！"
            else if (isId)
                "Fokus tebas saat berada di area aman. Jangan biarkan lolos ke bawah!"
            else
                "Focus your slash within the safe zone. Don't let violations escape below!"
        )
        entry.section == GlossarySection.LEGITIMATE -> RulesDialogInfo(
            badgeColor = Color(0xFF64B5F6),
            directive = if (isJa) "🛡️ 守れ！絶対に斬るな！" else if (isId) "🛡️ LINDUNGI! JANGAN DITEBAS!" else "🛡️ PROTECT! DO NOT SLASH!",
            desc = if (isJa)
                "正規の公的文書はそのまま通過させてください。誤って斬ると規律違反となりライフが1減少します！"
            else if (isId)
                "Biarkan dokumen sah jatuh bebas. Menebas dokumen sah akan merusak kepatuhan dan mengurangi 1 NYAWA!"
            else
                "Let legitimate documents fall freely. Slicing legitimate documents breaches compliance and costs 1 LIFE!",
            tip = if (isJa)
                "公式の承認印や青色アイコンを合図に見分けよ。慌ててスワイプしてはならない！"
            else if (isId)
                "Perhatikan cap resmi atau warna biru. Jangan panik menggesek layar!"
            else
                "Look out for official seals or blue color. Don't panic swipe!"
        )
        entry.section == GlossarySection.TRAPS -> RulesDialogInfo(
            badgeColor = Color(0xFFFF5252),
            directive = if (isJa) "⚠️ 罠だ！手を出すな！" else if (isId) "⚠️ JEBAKAN! HINDARI DITEBAS!" else "⚠️ TRAP! AVOID SLICING!",
            desc = if (isJa)
                "根拠のない噂や囮です。誤って斬ると10点減点され、コンボがリセットされます。"
            else if (isId)
                "Ini adalah umpan/hoaks yang belum terbukti. Menebasnya akan dikenai PENALTI -10 POIN dan memutus kombo."
            else
                "This is unproven bait or a hoax. Slicing it inflicts a -10 POINTS PENALTY and resets combo.",
            tip = if (isJa)
                "罠アイテムは違反品と紛らわしく出現する。切断する前に慎重に見極めよ！"
            else if (isId)
                "Item jebakan sering muncul berdekatan dengan pelanggaran. Teliti sebelum menebas!"
            else
                "Trap items often appear close to violations. Inspect carefully before slicing!"
        )
        else -> RulesDialogInfo(
            badgeColor = Color(0xFF00E676),
            directive = if (isJa) "💎 黄金シールド！必ず獲得せよ！" else if (isId) "💎 PERISAI EMAS! SEGERA AMBIL!" else "💎 GOLDEN SHIELD! CLAIM IT!",
            desc = if (isJa)
                "稀に出現する黄金シールドを斬ると、ライフが+1回復（最大4）し、+25点のボーナススコアを獲得できます！"
            else if (isId)
                "Tebas perisai emas langka ini untuk memulihkan +1 NYAWA (maksimal 4) dan memperoleh skor bonus +25 poin!"
            else
                "Slice this rare golden shield to restore +1 LIFE (max 4) and gain +25 bonus points!",
            tip = if (isJa)
                "黄金シールドは極めて貴重です。最優先で獲得して任務の継続時間を延ばしましょう！"
            else if (isId)
                "Perisai emas sangat berharga. Utamakan menebasnya untuk memperpanjang shift Anda!"
            else
                "Golden shields are very valuable. Prioritize slicing them to extend your shift!"
        )
    }

    val badgeColor = info.badgeColor
    val actionDirective = info.directive
    val actionDesc = info.desc
    val tipText = info.tip

    val discBgColor = when (entry.section) {
        GlossarySection.VIOLATIONS -> Color(0xFF3E2805)
        GlossarySection.LEGITIMATE -> Color(0xFF0C2C4D)
        GlossarySection.TRAPS -> Color(0xFF3E0E0E)
        GlossarySection.BONUS -> Color(0xFF052F1A)
    }

    val itemDisplayName = entry.category.getDisplayName(currentLanguage)
    val itemExplanation = entry.category.getExplanation(currentLanguage)

    val infiniteTransition = rememberInfiniteTransition(label = "pulse_glow")
    val glowScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_scale"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.5.dp, badgeColor, RoundedCornerShape(24.dp))
                .testTag("glossary_detail_dialog"),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0C1626)),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Large 3D Icon with Radiant Disc Background
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .scale(glowScale)
                        .clip(CircleShape)
                        .background(discBgColor)
                        .border(2.dp, badgeColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = entry.category.iconRes),
                        contentDescription = itemDisplayName,
                        modifier = Modifier.size(64.dp),
                        contentScale = ContentScale.Fit
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Item Name
                Text(
                    text = itemDisplayName,
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Action Pill
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = badgeColor.copy(alpha = 0.25f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, badgeColor)
                ) {
                    Text(
                        text = actionDirective,
                        color = badgeColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Context Box (Compliance Meaning & In-Game Action)
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0x33FFFFFF),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x22FFFFFF))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = if (isJa) "ゲームアリーナ規則:" else if (isId) "ATURAN ARENA GAME:" else "GAME ARENA RULES:",
                            color = badgeColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = actionDesc,
                            color = Color(0xFFECEFF1),
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = if (isJa) "コンプライアンス背景:" else if (isId) "KONTEKS TATA KELOLA:" else "COMPLIANCE CONTEXT:",
                            color = Color(0xFF90CAF9),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = itemExplanation,
                            color = Color(0xFFB0BEC5),
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Ninja Bang Patuh Pro Tip
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0x22FFC857), RoundedCornerShape(10.dp))
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.mascot_owl_transparent),
                        contentDescription = null,
                        modifier = Modifier.size(34.dp),
                        contentScale = ContentScale.Fit
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = if (isJa) "バン・パトゥ直伝の心得:" else if (isId) "TIPS PATUH NINJA:" else "NINJA PATUH TIP:",
                            color = GoldSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = tipText,
                            color = Color(0xFFFFF8E1),
                            fontSize = 10.sp,
                            lineHeight = 13.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Dismiss Button
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("glossary_dialog_dismiss_btn"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = badgeColor,
                        contentColor = Color(0xFF09131F)
                    )
                ) {
                    Text(
                        text = if (isJa) "了解" else if (isId) "Dimengerti" else "Understood",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}
