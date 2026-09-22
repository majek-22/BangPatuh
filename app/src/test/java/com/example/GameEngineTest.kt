package com.example

import com.example.data.ComplianceCategory
import com.example.engine.GameEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GameEngineTest {

    private lateinit var engine: GameEngine

    @Before
    fun setUp() {
        engine = GameEngine(screenWidth = 1080f, screenHeight = 1920f)
        engine.resetGame()
    }

    @Test
    fun testSliceViolationIncreasesScoreAndCombo() {
        val item = engine.spawnItemManually(
            category = ComplianceCategory.BRIBERY,
            x = 500f,
            y = 500f,
            radius = 58f
        )

        assertFalse("Item should start unsliced", item.sliced)

        val hits = engine.processSliceSegment(400f, 500f, 600f, 500f)

        assertEquals("Should register 1 slice hit", 1, hits)
        assertTrue("Item should be sliced", item.sliced)
        assertEquals("Bribery base score is 10", 10, engine.score)
        assertEquals("Combo streak should be 1", 1, engine.comboStreak)
        assertEquals("Combo multiplier should be 1x for 1st hit", 1, engine.comboMultiplier)
    }

    @Test
    fun testConsecutiveViolationSlicesIncreaseComboMultiplier() {
        for (i in 1..10) {
            val item = engine.spawnItemManually(
                category = ComplianceCategory.FRAUD,
                x = 100f * i,
                y = 500f,
                radius = 58f
            )
            engine.processSliceSegment(item.x - 40f, 500f, item.x + 40f, 500f)
        }

        assertEquals("Combo streak should be 10", 10, engine.comboStreak)
        assertEquals("Combo multiplier should reach max 4x", 4, engine.comboMultiplier)
    }

    @Test
    fun testWrongSliceDeductsLifeAndResetsCombo() {
        for (i in 1..3) {
            val v = engine.spawnItemManually(ComplianceCategory.DATA_BREACH, 100f * i, 300f)
            engine.processSliceSegment(v.x - 30f, 300f, v.x + 30f, 300f)
        }
        assertEquals("Combo streak should be 3", 3, engine.comboStreak)
        assertEquals("Multiplier should be 2x", 2, engine.comboMultiplier)

        val legitItem = engine.spawnItemManually(ComplianceCategory.OFFICIAL_DOCUMENT, 500f, 500f)
        val hits = engine.processSliceSegment(450f, 500f, 550f, 500f)

        assertEquals("Should register 1 slice hit", 1, hits)
        assertTrue("Legitimate item marked sliced", legitItem.sliced)
        assertEquals("Lives should be reduced by 1 (3 -> 2)", 2, engine.lives)
        assertEquals("Combo streak must reset to 0 on mistake", 0, engine.comboStreak)
        assertEquals("Multiplier must reset to 1x on mistake", 1, engine.comboMultiplier)
    }

    @Test
    fun testTrapSliceDeductsTenPointsAndResetsComboWithoutLifeLoss() {
        val initialLives = engine.lives
        // Give some initial score
        val v = engine.spawnItemManually(ComplianceCategory.BRIBERY, 300f, 300f)
        engine.processSliceSegment(250f, 300f, 350f, 300f)
        val scoreBeforeTrap = engine.score
        assertEquals("Should have 10 points", 10, scoreBeforeTrap)
        assertEquals("Combo streak should be 1", 1, engine.comboStreak)

        // Spawn a trap item (False Alarm)
        val trap = engine.spawnItemManually(ComplianceCategory.FALSE_ALARM, 500f, 500f)
        val hits = engine.processSliceSegment(450f, 500f, 550f, 500f)

        assertEquals("Should register 1 hit on trap", 1, hits)
        assertTrue("Trap should be marked sliced", trap.sliced)
        assertEquals("Score should be deducted by 10 points (10 -> 0)", 0, engine.score)
        assertEquals("Traps sliced count should be 1", 1, engine.trapsSlicedCount)
        assertEquals("Lives should NOT be deducted on trap slice!", initialLives, engine.lives)
        assertEquals("Combo streak must reset to 0 on trap slice", 0, engine.comboStreak)
        assertEquals("Multiplier must reset to 1x on trap slice", 1, engine.comboMultiplier)
    }

    @Test
    fun testTrapFallingOffScreenAvoidedWithoutLifePenalty() {
        val initialLives = engine.lives

        // Spawn trap falling off bottom
        engine.spawnItemManually(
            category = ComplianceCategory.UNVERIFIED_RUMOR,
            x = 400f,
            y = 2000f,
            vy = 800f
        )

        engine.update(0.10f)

        assertEquals("No life loss when trap is safely avoided", initialLives, engine.lives)
        assertEquals("Traps avoided count should be 1", 1, engine.trapsAvoidedCount)
    }

    @Test
    fun testScoreDifficultyTierProgression() {
        assertEquals("Tier 0 for 0-99 points", 0, engine.getScoreDifficultyTier(0).tier)
        assertEquals("Tier 0 for 99 points", 0, engine.getScoreDifficultyTier(99).tier)
        assertEquals("Tier 1 for 100 points", 1, engine.getScoreDifficultyTier(100).tier)
        assertEquals("Tier 2 for 300 points", 2, engine.getScoreDifficultyTier(300).tier)
        assertEquals("Tier 3 for 600 points", 3, engine.getScoreDifficultyTier(600).tier)
        assertEquals("Tier 4 for 1000 points", 4, engine.getScoreDifficultyTier(1000).tier)
        assertEquals("Tier 4 for 2500 points", 4, engine.getScoreDifficultyTier(2500).tier)

        val tier4 = engine.getScoreDifficultyTier(1200)
        assertEquals("Tier 4 has 35% speed boost", 0.35f, tier4.speedIncreasePercent, 0.001f)
        assertEquals("Tier 4 has 50% interval reduction", 0.50f, tier4.spawnIntervalReductionPercent, 0.001f)
    }

    @Test
    fun testMissedViolationDeductsLifeAndResetsCombo() {
        val initialLives = engine.lives

        engine.spawnItemManually(
            category = ComplianceCategory.MONEY_LAUNDERING,
            x = 500f,
            y = 2000f,
            vy = 800f
        )

        engine.update(0.10f)

        assertEquals("Missing a violation must cost 1 life", initialLives - 1, engine.lives)
        assertEquals("Combo must reset to 0 on missed violation", 0, engine.comboStreak)
    }

    @Test
    fun testMissedLegitimateItemDoesNotDeductLife() {
        val initialLives = engine.lives

        engine.spawnItemManually(
            category = ComplianceCategory.VERIFIED_APPROVAL,
            x = 400f,
            y = 2000f,
            vy = 800f
        )

        engine.update(0.10f)

        assertEquals("Letting legitimate item fall must NOT penalize lives", initialLives, engine.lives)
    }

    @Test
    fun testShieldBonusIncreasesLifeAndScore() {
        val legit = engine.spawnItemManually(ComplianceCategory.CERTIFICATION, 100f, 100f)
        engine.processSliceSegment(50f, 100f, 150f, 100f)
        assertEquals("Lives should be 2 after 1 mistake", 2, engine.lives)
        assertEquals("Base lives is 2", 2, engine.baseLives)
        assertEquals("Shield bonus lives is 0", 0, engine.shieldBonusLives)

        val shield = engine.spawnItemManually(ComplianceCategory.SHIELD, 300f, 300f)
        engine.processSliceSegment(250f, 300f, 350f, 300f)

        assertTrue("Shield was sliced", shield.sliced)
        assertEquals("Shield grants +1 red life (2 -> 3)", 3, engine.lives)
        assertEquals("Base lives restored to 3 (red life)", 3, engine.baseLives)
        assertEquals("Shield bonus lives remains 0 (was red life restored)", 0, engine.shieldBonusLives)
        assertEquals("Shield awards +25 base points", 25, engine.score)
    }

    @Test
    fun testShieldRestoresRedLifeWhenAtOneLife() {
        // Drop to 1 life (2 mistakes)
        engine.spawnItemManually(ComplianceCategory.CERTIFICATION, 100f, 100f)
        engine.processSliceSegment(50f, 100f, 150f, 100f)
        engine.spawnItemManually(ComplianceCategory.CERTIFICATION, 100f, 100f)
        engine.processSliceSegment(50f, 100f, 150f, 100f)
        assertEquals("Lives is 1", 1, engine.lives)
        assertEquals("Base lives is 1", 1, engine.baseLives)

        // Slicing shield with 1 life should restore a red life (1 -> 2)
        engine.spawnItemManually(ComplianceCategory.SHIELD, 300f, 300f)
        engine.processSliceSegment(250f, 300f, 350f, 300f)
        assertEquals("Lives is now 2", 2, engine.lives)
        assertEquals("Base lives is now 2 (red life)", 2, engine.baseLives)
        assertEquals("Shield bonus lives is still 0", 0, engine.shieldBonusLives)
    }

    @Test
    fun testShieldGrantsFourthLifeAndAbsorbsHit() {
        assertEquals("Starts with 3 lives", 3, engine.lives)
        assertEquals("Starts with 3 base lives", 3, engine.baseLives)
        assertEquals("Starts with 0 shield bonus lives", 0, engine.shieldBonusLives)

        // When player already has 3 full red lives, shield grants 4th life (blue)
        val shield = engine.spawnItemManually(ComplianceCategory.SHIELD, 300f, 300f)
        engine.processSliceSegment(250f, 300f, 350f, 300f)

        assertEquals("Compliance shield grants 4th life (max 4)", 4, engine.lives)
        assertEquals("Shield bonus life is 1 (blue life)", 1, engine.shieldBonusLives)
        assertEquals("Base lives remain 3 (red lives)", 3, engine.baseLives)

        // Slicing another shield at 4 lives does not exceed max 4 lives
        val shield2 = engine.spawnItemManually(ComplianceCategory.SHIELD, 400f, 400f)
        engine.processSliceSegment(350f, 400f, 450f, 400f)
        assertEquals("Lives remains 4 (max 4)", 4, engine.lives)
        assertEquals("Shield bonus life is still 1", 1, engine.shieldBonusLives)
        assertEquals("Base lives is still 3", 3, engine.baseLives)

        // Damage should consume shield bonus life first, preserving 3 base lives
        val wrong = engine.spawnItemManually(ComplianceCategory.CERTIFICATION, 100f, 100f)
        engine.processSliceSegment(50f, 100f, 150f, 100f)
        assertEquals("Lives returns to 3 after shield absorbs damage", 3, engine.lives)
        assertEquals("Shield bonus life is consumed", 0, engine.shieldBonusLives)
        assertEquals("Base lives remain intact at 3", 3, engine.baseLives)
    }

    @Test
    fun testSliceSegmentMissDoesNotSlice() {
        val item = engine.spawnItemManually(ComplianceCategory.BRIBERY, 500f, 500f, radius = 50f)

        val hits = engine.processSliceSegment(100f, 100f, 200f, 200f)

        assertEquals("No hits registered", 0, hits)
        assertFalse("Item should remain unsliced", item.sliced)
    }

    @Test
    fun testGameOverWhenLivesDepleted() {
        var gameOverCalled = false
        engine.onGameOver = {
            gameOverCalled = true
        }

        for (i in 1..3) {
            val legit = engine.spawnItemManually(ComplianceCategory.VERIFIED_INVOICE, 100f * i, 200f)
            engine.processSliceSegment(legit.x - 30f, 200f, legit.x + 30f, 200f)
        }

        assertEquals("Lives should be 0", 0, engine.lives)
        assertTrue("Game should be in gameOver state", engine.isGameOver)
        assertTrue("onGameOver callback should be fired", gameOverCalled)
    }

    @Test
    fun testSlicedCategoriesRecordedForDebrief() {
        val b = engine.spawnItemManually(ComplianceCategory.BRIBERY, 200f, 200f)
        val m = engine.spawnItemManually(ComplianceCategory.MONEY_LAUNDERING, 400f, 200f)

        engine.processSliceSegment(b.x - 20f, 200f, b.x + 20f, 200f)
        engine.processSliceSegment(m.x - 20f, 200f, m.x + 20f, 200f)

        val summary = engine.getSlicedCategoriesSummary()
        assertEquals("Should have 2 distinct violation categories recorded", 2, summary.size)
        assertTrue(summary.any { it.category == ComplianceCategory.BRIBERY && it.count == 1 })
        assertTrue(summary.any { it.category == ComplianceCategory.MONEY_LAUNDERING && it.count == 1 })
    }

    @Test
    fun testBonusCorruptorSliceClearsOtherItemsAndFreezesGame() {
        // Spawn normal items
        val item1 = engine.spawnItemManually(ComplianceCategory.BRIBERY, 200f, 400f)
        val item2 = engine.spawnItemManually(ComplianceCategory.VERIFIED_INVOICE, 400f, 400f)

        // Spawn bonus corruptor
        val corruptor = engine.spawnItemManually(ComplianceCategory.BONUS_CORRUPTOR, 600f, 400f)
        assertEquals(3, engine.activeItems.size)
        assertFalse(engine.isFreezeActive)

        // Slicing through the corruptor circle (swipe completely across diameter)
        val hits = engine.processSliceSegment(500f, 400f, 700f, 400f)
        assertEquals(1, hits)

        // 1. All other items should be cleared automatically
        assertEquals(1, engine.activeItems.size)
        assertEquals(ComplianceCategory.BONUS_CORRUPTOR, engine.activeItems.first().category)

        // 2. Freeze mode should be active for 5.0 seconds
        assertTrue(engine.isFreezeActive)
        assertEquals(5.0f, engine.freezeTimer, 0.05f)
        assertEquals(1, engine.freezeBonusHits)

        // 3. Additional slices during freeze (must also swipe across circle)
        Thread.sleep(50)
        engine.processSliceSegment(500f, 400f, 700f, 400f)
        assertEquals(2, engine.freezeBonusHits)

        // 4. Advancing past 5 seconds ends freeze mode
        var freezeElapsed = 0f
        while (engine.isFreezeActive && freezeElapsed < 8f) {
            engine.update(0.04f)
            freezeElapsed += 0.04f
        }
        assertFalse(engine.isFreezeActive)
        assertEquals(0f, engine.freezeTimer, 0.01f)
    }

    @Test
    fun testBonusCorruptorCanSpawnMultipleTimesRandomly() {
        // Run update loop over simulated time and count bonus corruptor spawns
        var bonusSpawns = 0
        val maxSimulatedTime = 160.0f // 160 seconds of gameplay to account for increasing distance between spawns
        var simulated = 0f
        while (simulated < maxSimulatedTime) {
            val hadBonusBefore = engine.activeItems.any { it.category.isFreezeBonus && !it.sliced }
            engine.update(0.04f)
            simulated += 0.04f

            // Slice active unsliced violations to prevent lives draining, and count bonus corruptor spawns
            for (item in engine.activeItems.toList()) {
                if (item.category.isFreezeBonus && !item.sliced && !hadBonusBefore) {
                    bonusSpawns++
                    engine.processSliceSegment(item.x - 100f, item.y, item.x + 100f, item.y)
                } else if (item.isViolation && !item.sliced) {
                    engine.processSliceSegment(item.x - 20f, item.y, item.x + 20f, item.y)
                }
            }
        }

        // Should have spawned multiple times (at least 2 times in 90 seconds)
        assertTrue("Bonus corruptor should spawn multiple times over 90s, but spawned $bonusSpawns times", bonusSpawns >= 2)
    }

    @Test
    fun testBonusCorruptorRejectsTapWithoutFullSlice() {
        val corruptor = engine.spawnItemManually(ComplianceCategory.BONUS_CORRUPTOR, 500f, 500f, radius = 80f)
        engine.startStroke()

        // Micro touch or tiny tap inside the circle (length 10f < 80f * 1.25f)
        val tapHits = engine.processSliceSegment(500f, 500f, 505f, 505f)
        assertEquals(0, tapHits)
        assertFalse(engine.isFreezeActive)

        // Now full slice spanning through the circle (length 200f > 80f * 1.25f)
        val sliceHits = engine.processSliceSegment(390f, 500f, 610f, 500f)
        assertEquals(1, sliceHits)
        assertTrue(engine.isFreezeActive)
    }
}
