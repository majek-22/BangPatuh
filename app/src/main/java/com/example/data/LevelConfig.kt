package com.example.data

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.example.R

enum class GameDifficulty(
    val id: String,
    @get:StringRes val labelRes: Int,
    val scoreMultiplier: Float,
    val speedMultiplier: Float,
    val spawnIntervalMultiplier: Float
) {
    NORMAL(
        id = "normal",
        labelRes = R.string.difficulty_normal,
        scoreMultiplier = 1.0f,
        speedMultiplier = 1.0f,
        spawnIntervalMultiplier = 1.0f
    ),
    HARD(
        id = "hard",
        labelRes = R.string.difficulty_hard,
        scoreMultiplier = 1.25f,
        speedMultiplier = 1.15f,
        spawnIntervalMultiplier = 0.85f
    )
}

data class LevelConfig(
    val levelNumber: Int,
    @get:StringRes val nameRes: Int,
    @get:StringRes val descRes: Int,
    @get:DrawableRes val iconRes: Int,
    @get:DrawableRes val backgroundRes: Int,
    val durationSeconds: Int,
    val oneStarScore: Int,
    val twoStarsScore: Int,
    val threeStarsScore: Int,
    val fourStarsScore: Int,
    val fiveStarsScore: Int,
    val allowedViolations: List<ComplianceCategory>,
    val allowedLegitimate: List<ComplianceCategory>,
    val allowedTraps: List<ComplianceCategory>,
    val baseSpawnInterval: Float
) {
    fun calculateStars(score: Int): Int {
        return when {
            score >= fiveStarsScore -> 5
            score >= fourStarsScore -> 4
            score >= threeStarsScore -> 3
            score >= twoStarsScore -> 2
            score >= oneStarScore -> 1
            else -> 0
        }
    }

    companion object {
        val ALL_LEVELS: List<LevelConfig> = listOf(
            LevelConfig(
                levelNumber = 1,
                nameRes = R.string.level1_name,
                descRes = R.string.level1_desc,
                iconRes = R.drawable.intern,
                backgroundRes = R.drawable.bg_internal_controls,
                durationSeconds = 60,
                oneStarScore = 300,
                twoStarsScore = 600,
                threeStarsScore = 900,
                fourStarsScore = 1200,
                fiveStarsScore = 1500,
                allowedViolations = listOf(
                    ComplianceCategory.BRIBERY,
                    ComplianceCategory.FRAUD
                ),
                allowedLegitimate = listOf(
                    ComplianceCategory.OFFICIAL_DOCUMENT,
                    ComplianceCategory.VERIFIED_APPROVAL
                ),
                allowedTraps = listOf(
                    ComplianceCategory.FALSE_ALARM,
                    ComplianceCategory.HONEST_MISTAKE
                ),
                baseSpawnInterval = 1.5f
            ),
            LevelConfig(
                levelNumber = 2,
                nameRes = R.string.level2_name,
                descRes = R.string.level2_desc,
                iconRes = R.drawable.staff,
                backgroundRes = R.drawable.bg_claims_fraud,
                durationSeconds = 65,
                oneStarScore = 700,
                twoStarsScore = 1300,
                threeStarsScore = 1900,
                fourStarsScore = 2500,
                fiveStarsScore = 3200,
                allowedViolations = listOf(
                    ComplianceCategory.BRIBERY,
                    ComplianceCategory.FRAUD,
                    ComplianceCategory.MONEY_LAUNDERING
                ),
                allowedLegitimate = listOf(
                    ComplianceCategory.OFFICIAL_DOCUMENT,
                    ComplianceCategory.VERIFIED_APPROVAL,
                    ComplianceCategory.VALID_PARTNERSHIP
                ),
                allowedTraps = listOf(
                    ComplianceCategory.FALSE_ALARM
                ),
                baseSpawnInterval = 1.4f
            ),
            LevelConfig(
                levelNumber = 3,
                nameRes = R.string.level3_name,
                descRes = R.string.level3_desc,
                iconRes = R.drawable.manager,
                backgroundRes = R.drawable.bg_data_privacy,
                durationSeconds = 70,
                oneStarScore = 1500,
                twoStarsScore = 2600,
                threeStarsScore = 3800,
                fourStarsScore = 5000,
                fiveStarsScore = 6200,
                allowedViolations = listOf(
                    ComplianceCategory.BRIBERY,
                    ComplianceCategory.FRAUD,
                    ComplianceCategory.MONEY_LAUNDERING,
                    ComplianceCategory.DATA_BREACH
                ),
                allowedLegitimate = listOf(
                    ComplianceCategory.OFFICIAL_DOCUMENT,
                    ComplianceCategory.VERIFIED_APPROVAL,
                    ComplianceCategory.VALID_PARTNERSHIP,
                    ComplianceCategory.CERTIFICATION,
                    ComplianceCategory.VERIFIED_INVOICE
                ),
                allowedTraps = listOf(
                    ComplianceCategory.FALSE_ALARM,
                    ComplianceCategory.UNVERIFIED_RUMOR,
                    ComplianceCategory.HONEST_MISTAKE
                ),
                baseSpawnInterval = 1.3f
            ),
            LevelConfig(
                levelNumber = 4,
                nameRes = R.string.level4_name,
                descRes = R.string.level4_desc,
                iconRes = R.drawable.chief,
                backgroundRes = R.drawable.bg_finance_crimes,
                durationSeconds = 75,
                oneStarScore = 2500,
                twoStarsScore = 4500,
                threeStarsScore = 6500,
                fourStarsScore = 8500,
                fiveStarsScore = 11000,
                allowedViolations = ComplianceCategory.ALL_VIOLATIONS,
                allowedLegitimate = ComplianceCategory.LEGITIMATE,
                allowedTraps = ComplianceCategory.TRAPS,
                baseSpawnInterval = 1.2f
            )
        )

        fun getByLevel(level: Int): LevelConfig {
            return ALL_LEVELS.find { it.levelNumber == level } ?: ALL_LEVELS.first()
        }
    }
}
