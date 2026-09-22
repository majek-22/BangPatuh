package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GoldSecondary

data class LanguageOption(
    val code: String,
    val badge: String,
    val label: String,
    val flag: String
)

val SUPPORTED_LANGUAGES = listOf(
    LanguageOption(code = "in", badge = "ID", label = "Bahasa Indonesia", flag = "🇮🇩"),
    LanguageOption(code = "en", badge = "EN", label = "English", flag = "🇬🇧"),
    LanguageOption(code = "ja", badge = "JA", label = "日本語", flag = "🇯🇵")
)

/**
 * Dropdown menu showing the 3 language options (ID, EN, JA) when the language button is clicked.
 */
@Composable
fun LanguageDropdownMenu(
    expanded: Boolean,
    currentLanguage: String,
    onDismissRequest: () -> Unit,
    onLanguageSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = modifier
            .background(Color(0xFF0F1B2B))
            .border(BorderStroke(1.dp, Color(0x6600E5FF)), RoundedCornerShape(12.dp))
            .testTag("language_dropdown_menu"),
        shape = RoundedCornerShape(12.dp),
        containerColor = Color(0xFF0F1B2B),
        shadowElevation = 10.dp
    ) {
        Column(
            modifier = Modifier
                .width(180.dp)
                .padding(vertical = 4.dp)
        ) {
            // 3 choices: ID, EN, JA
            SUPPORTED_LANGUAGES.forEach { option ->
                val isSelected = when (option.code) {
                    "in" -> currentLanguage.equals("in", ignoreCase = true) || currentLanguage.equals("id", ignoreCase = true)
                    "ja" -> currentLanguage.equals("ja", ignoreCase = true)
                    else -> currentLanguage.equals("en", ignoreCase = true)
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onLanguageSelected(option.code)
                            onDismissRequest()
                        }
                        .background(
                            if (isSelected) Color(0x3300E5FF) else Color.Transparent
                        )
                        .padding(horizontal = 12.dp, vertical = 9.dp)
                        .testTag("lang_option_${option.badge}"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = option.flag,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .background(
                                    if (isSelected) GoldSecondary.copy(alpha = 0.25f) else Color(0x33FFFFFF),
                                    RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 5.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = option.badge,
                                color = if (isSelected) GoldSecondary else Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = option.label,
                            color = if (isSelected) Color.White else Color(0xFFCFD8DC),
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }

                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = GoldSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
