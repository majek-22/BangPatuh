package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.R

@Entity(tableName = "user_stats")
data class UserStats(
    @PrimaryKey
    val username: String,
    val avatarId: Int = 1,
    val overallBestScore: Int = 0,
    val highestTierReached: Int = 0,
    val bestScoreLevel: Int = 1,
    val bestScoreDifficulty: String = "Auto",
    val gamesPlayed: Int = 0,
    val totalViolationsSliced: Int = 0,
    val totalTrapsAvoided: Int = 0,
    val totalTrapsSliced: Int = 0,
    val bestComboStreak: Int = 0,
    val briberySliced: Int = 0,
    val fraudSliced: Int = 0,
    val moneyLaunderingSliced: Int = 0,
    val dataBreachSliced: Int = 0,
    val systemicCorruptionSliced: Int = 0,
    val otherViolationsSliced: Int = 0,
    val level1Best: Int = 0,
    val level1Stars: Int = 0,
    val level2Best: Int = 0,
    val level2Stars: Int = 0,
    val level3Best: Int = 0,
    val level3Stars: Int = 0,
    val level4Best: Int = 0,
    val level4Stars: Int = 0,
    val maxUnlockedLevel: Int = 1,
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun getBestForLevel(level: Int): Int {
        return when (level) {
            1 -> level1Best
            2 -> level2Best
            3 -> level3Best
            4 -> level4Best
            else -> 0
        }
    }

    fun getStarsForLevel(level: Int): Int {
        return when (level) {
            1 -> level1Stars
            2 -> level2Stars
            3 -> level3Stars
            4 -> level4Stars
            else -> 0
        }
    }

    fun isLevelUnlocked(level: Int): Boolean {
        if (level <= 1) return true
        if (level <= maxUnlockedLevel) return true
        return when (level) {
            1 -> true
            2 -> level1Best >= 1200
            3 -> (level1Best >= 1200 && level2Best >= 2500) || maxUnlockedLevel >= 3
            4 -> (level1Best >= 1200 && level2Best >= 2500 && level3Best >= 5000) || maxUnlockedLevel >= 4
            else -> false
        }
    }

    fun calculateMaxUnlockedLevel(): Int {
        var maxLvl = maxOf(1, maxUnlockedLevel)
        if (level1Best >= 1200) maxLvl = maxOf(maxLvl, 2)
        if (level1Best >= 1200 && level2Best >= 2500) maxLvl = maxOf(maxLvl, 3)
        if (level1Best >= 1200 && level2Best >= 2500 && level3Best >= 5000) maxLvl = maxOf(maxLvl, 4)
        return maxLvl
    }

    fun getUnlockScoreRequired(level: Int): Int {
        return when (level) {
            2 -> 1200
            3 -> 2500
            4 -> 5000
            else -> 0
        }
    }

    fun getRankTitleRes(): Int {
        return when {
            overallBestScore >= 1200 -> R.string.rank_chief_compliance_ninja
            overallBestScore >= 800 -> R.string.rank_senior_risk_specialist
            overallBestScore >= 450 -> R.string.rank_lead_investigator
            overallBestScore >= 200 -> R.string.rank_junior_auditor
            else -> R.string.rank_compliance_intern
        }
    }

    fun getRankTitle(language: String = "en"): String {
        val l = language.lowercase()
        return when {
            overallBestScore >= 1200 -> when (l) {
                "ja" -> "チーフ・コンプライアンス忍者"
                "in", "id" -> "Pimpinan Ninja Kepatuhan"
                else -> "Chief Compliance Ninja"
            }
            overallBestScore >= 800 -> when (l) {
                "ja" -> "シニア・リスクスペシャリスト"
                "in", "id" -> "Spesialis Risiko Senior"
                else -> "Senior Risk Specialist"
            }
            overallBestScore >= 450 -> when (l) {
                "ja" -> "主任調査員"
                "in", "id" -> "Ketua Investigasi"
                else -> "Lead Investigator"
            }
            overallBestScore >= 200 -> when (l) {
                "ja" -> "ジュニア監査員"
                "in", "id" -> "Auditor Junior"
                else -> "Junior Auditor"
            }
            else -> when (l) {
                "ja" -> "コンプライアンス・インターン"
                "in", "id" -> "Magang Kepatuhan"
                else -> "Compliance Intern"
            }
        }
    }
}
