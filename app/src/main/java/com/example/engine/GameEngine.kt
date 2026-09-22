package com.example.engine

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.example.data.ComplianceCategory
import com.example.data.FloatingPopup
import com.example.data.GameDifficulty
import com.example.data.GameItem
import com.example.data.LevelConfig
import com.example.data.Particle
import com.example.data.SlicedCategoryRecord
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

/**
 * Score-based difficulty tier definition.
 *
 * Thresholds:
 * - Tier 0 (score 0-99): baseline spawn interval and item speed
 * - Tier 1 (score 100-299): spawn interval -15%, item speed +10%
 * - Tier 2 (score 300-599): spawn interval -30%, item speed +20%
 * - Tier 3 (score 600-999): spawn interval -42%, item speed +30%
 * - Tier 4 (score 1000+): spawn interval -50%, item speed +35% (hard cap)
 *
 * NOTE: These tuning percentages are a starting point for playtesting and provide
 * responsive, fair arcade difficulty ramp without impossible reaction spikes.
 */
data class ScoreDifficultyTier(
    val tier: Int,
    val spawnIntervalReduction: Float,
    val speedIncrease: Float
) {
    val speedIncreasePercent: Float get() = speedIncrease
    val spawnIntervalReductionPercent: Float get() = spawnIntervalReduction
}

/**
 * Pure Kotlin GameEngine responsible for physics updates, parabolic item launching,
 * collision/slice hit-testing, score-based difficulty scaling, trap handling, and combos.
 *
 * Has zero Compose dependencies to enable fast, independent JVM unit testing.
 */
class GameEngine(
    var screenWidth: Float = 1080f,
    var screenHeight: Float = 1920f,
    val random: Random = Random.Default
) {
    companion object {
        const val DEFAULT_ROUND_TIME = 75f
        const val DEFAULT_LIVES = 3
        const val MAX_LIVES = 4
        const val GRAVITY = 1750f // px/s^2 for crisp arc trajectory
        const val SHIELD_SPAWN_CHANCE = 0.05f
        const val TRAP_SPAWN_CHANCE = 0.14f // Solid ~14% trap spawn rate (~12-15% chance)
        const val SYSTEMIC_CORRUPTION_CHANCE = 0.09f

        /**
         * Pure function to determine difficulty tier based on score.
         */
        fun getScoreDifficultyTier(currentScore: Int): ScoreDifficultyTier {
            return when {
                currentScore >= 1000 -> ScoreDifficultyTier(tier = 4, spawnIntervalReduction = 0.50f, speedIncrease = 0.35f)
                currentScore >= 600 -> ScoreDifficultyTier(tier = 3, spawnIntervalReduction = 0.42f, speedIncrease = 0.30f)
                currentScore >= 300 -> ScoreDifficultyTier(tier = 2, spawnIntervalReduction = 0.30f, speedIncrease = 0.20f)
                currentScore >= 100 -> ScoreDifficultyTier(tier = 1, spawnIntervalReduction = 0.15f, speedIncrease = 0.10f)
                else -> ScoreDifficultyTier(tier = 0, spawnIntervalReduction = 0.00f, speedIncrease = 0.00f)
            }
        }
    }

    fun getScoreDifficultyTier(currentScore: Int): ScoreDifficultyTier = Companion.getScoreDifficultyTier(currentScore)

    // --- Active Level and Difficulty Configuration ---
    var currentLevel: LevelConfig = LevelConfig.ALL_LEVELS.first()
        private set
    var currentDifficulty: GameDifficulty = GameDifficulty.NORMAL
        private set

    // --- State Properties ---
    var score: Int = 0
        private set
    var baseLives: Int = DEFAULT_LIVES
        private set
    var shieldBonusLives: Int = 0
        private set
    var lives: Int = DEFAULT_LIVES
        private set
    var elapsedTimeSeconds: Float = 0f
        private set
    var timeRemainingSeconds: Float
        get() = elapsedTimeSeconds
        private set(value) { elapsedTimeSeconds = value }
    var comboStreak: Int = 0
        private set
    var comboMultiplier: Int = 1
        private set
    var maxComboStreak: Int = 0
        private set
    var isGameOver: Boolean = false
        private set
    var currentLanguage: String = "en"

    // Trap statistics
    var trapsAvoidedCount: Int = 0
        private set
    var trapsSlicedCount: Int = 0
        private set
    var totalViolationsSlicedCount: Int = 0
        private set
    var highestTierReachedInRound: Int = 0
        private set

    // Active entities
    val activeItems: SnapshotStateList<GameItem> = mutableStateListOf<GameItem>()
    val particles = mutableListOf<Particle>()
    val popups = mutableListOf<FloatingPopup>()

    // Single-stroke multi-slice tracking (separate from round-long comboStreak)
    var strokeViolationCount: Int = 0
        private set
    private var strokePointsEarned: Int = 0
    private var strokeBonusAwarded: Boolean = false

    // Stroke points tracking for corruptor slice sensitivity
    private val currentStrokePoints = mutableListOf<StrokePoint>()
    private var strokeCorruptorHitsCount: Int = 0

    private data class StrokePoint(val x: Float, val y: Float, val timeMillis: Long)

    fun startStroke() {
        strokeViolationCount = 0
        strokePointsEarned = 0
        strokeBonusAwarded = false
        currentStrokePoints.clear()
        strokeCorruptorHitsCount = 0
    }

    fun endStroke() {
        strokeViolationCount = 0
        strokePointsEarned = 0
        strokeBonusAwarded = false
        currentStrokePoints.clear()
        strokeCorruptorHitsCount = 0
    }

    // Compliance Debrief tracking (violation category -> count sliced)
    private val slicedCategoriesMap = mutableMapOf<ComplianceCategory, Int>()

    // Spawn timing
    private var nextSpawnTimer: Float = 0.6f
    private var nextItemId: Long = 1L
    private var nextPopupId: Long = 1L

    // Debug safe-zone overlay toggle
    var showDebugOverlay: Boolean = false

    // Safe play-field boundaries (Dynamic landscape sizing, enlarged 15%)
    val itemRadius: Float get() = if (screenHeight > 0f) (screenHeight * 0.098f).coerceIn(46f, 78f) else 58f
    val safeZoneTop: Float get() = screenHeight * 0.16f
    val safeZoneBottom: Float get() = screenHeight * 0.94f
    val safeZoneLeft: Float get() = itemRadius * 1.2f
    val safeZoneRight: Float get() = screenWidth - itemRadius * 1.2f

    // Freeze bonus state
    var freezeTimer: Float = 0f
        private set
    val isFreezeActive: Boolean get() = freezeTimer > 0f
    var freezeBonusHits: Int = 0
        private set
    var freezeBonusSpawnedThisLevel: Boolean = false
        private set
    var freezeBonusSpawnCount: Int = 0
        private set
    private var freezeBonusTargetTime: Float = 30f
    private var lastBonusSpawnTime: Float = 0f
    private var lastFreezeHitTimeMillis: Long = 0L
    private var freezeCorruptorX: Float = 0f
    private var freezeCorruptorY: Float = 0f

    /**
     * Schedules the next appearance of the "Corruptor" freeze bonus item.
     * Truly random timing without a fixed timestamp.
     * Can spawn multiple times, with subsequent appearances spaced significantly farther apart.
     */
    private fun scheduleNextFreezeBonus() {
        freezeBonusSpawnCount++
        // Jarak antar kemunculan berikutnya lebih jauh dari kemunculan pertama
        val baseDelay = 40f + (freezeBonusSpawnCount * 18f)
        val jitter = random.nextFloat() * 20f
        freezeBonusTargetTime = elapsedTimeSeconds + baseDelay + jitter
    }

    // Callbacks for UI audio/visual feedback
    var onViolationSliced: ((item: GameItem, pointsEarned: Int, currentMultiplier: Int) -> Unit)? = null
    var onWrongSlice: ((item: GameItem) -> Unit)? = null
    var onTrapSliced: ((item: GameItem) -> Unit)? = null
    var onTrapAvoided: ((item: GameItem) -> Unit)? = null
    var onViolationMissed: ((item: GameItem) -> Unit)? = null
    var onShieldSliced: ((item: GameItem) -> Unit)? = null
    var onFreezeStarted: (() -> Unit)? = null
    var onFreezeBonusSliced: ((item: GameItem, totalSlices: Int, pointsAwarded: Int) -> Unit)? = null
    var onFreezeEnded: ((totalSlices: Int, totalBonusPoints: Int) -> Unit)? = null
    var onComboTriggered: ((comboCount: Int, hitX: Float, hitY: Float) -> Unit)? = null
    var onGameOver: ((finalScore: Int) -> Unit)? = null

    /**
     * Resets all game engine state for a new round with the given level and difficulty.
     */
    fun resetGame(
        level: LevelConfig = LevelConfig.ALL_LEVELS.first(),
        difficulty: GameDifficulty = GameDifficulty.NORMAL
    ) {
        currentLevel = level
        currentDifficulty = difficulty
        score = 0
        baseLives = DEFAULT_LIVES
        shieldBonusLives = 0
        lives = DEFAULT_LIVES
        elapsedTimeSeconds = 0f
        comboStreak = 0
        comboMultiplier = 1
        maxComboStreak = 0
        strokeViolationCount = 0
        strokePointsEarned = 0
        strokeBonusAwarded = false
        trapsAvoidedCount = 0
        trapsSlicedCount = 0
        totalViolationsSlicedCount = 0
        highestTierReachedInRound = 0
        isGameOver = false
        activeItems.clear()
        particles.clear()
        popups.clear()
        slicedCategoriesMap.clear()
        nextSpawnTimer = 0.6f
        freezeTimer = 0f
        freezeBonusHits = 0
        freezeBonusSpawnedThisLevel = false
        freezeBonusSpawnCount = 0
        lastBonusSpawnTime = 0f
        // First corruptor bonus appears purely randomly without a fixed timestamp (~15s to 40s)
        freezeBonusTargetTime = 15f + (random.nextFloat() * 25f)
        lastFreezeHitTimeMillis = 0L
        freezeCorruptorX = 0f
        freezeCorruptorY = 0f
    }

    /**
     * Updates screen dimensions when layout orientation or size changes.
     */
    fun updateScreenDimensions(width: Float, height: Float) {
        if (width > 0f && height > 0f) {
            screenWidth = width
            screenHeight = height
        }
    }

    /**
     * Main simulation update loop called each frame.
     *
     * @param deltaTime Elapsed real time in seconds since last frame.
     */
    fun update(deltaTime: Float) {
        if (isGameOver) return

        val dt = deltaTime.coerceIn(0f, 0.05f) // Prevent simulation explosion on lag spikes

        // 0. Freeze Bonus Mode: All regular items and game timer are frozen for 5 seconds
        if (freezeTimer > 0f) {
            freezeTimer = (freezeTimer - dt).coerceAtLeast(0f)

            // Dynamic hover wobble animation for the corruptor bonus item
            val corruptor = activeItems.find { it.category.isFreezeBonus && !it.sliced }
            if (corruptor != null) {
                corruptor.x = freezeCorruptorX
                corruptor.y = freezeCorruptorY + sin(freezeTimer * 4.5f) * 10f
                corruptor.rotation += 35f * dt
            }

            // Particles and popups continue updating for immediate juicy slice feedback
            updateParticles(dt)
            updatePopups(dt)

            if (freezeTimer <= 0f) {
                endFreezeMode()
            }
            return
        }

        // 1. Update round elapsed play timer (counts UP, no countdown end condition!)
        elapsedTimeSeconds += dt

        // 2. Spawn progressive waves of compliance items
        updateSpawning(dt)

        // 3. Update physics and trajectory for active items
        updateItemsPhysics(dt)

        // 4. Update particle effects
        updateParticles(dt)

        // 5. Update floating score & alert popups
        updatePopups(dt)
    }

    // =========================================================================
    // PHYSICS & SPAWN TIMING (Features 3 & 4)
    // =========================================================================

    /**
     * Computes score-driven difficulty scaling and schedules item launches.
     * Replaces time-based ramping with round score thresholds (Tiers 0-4).
     */
    private fun updateSpawning(dt: Float) {
        // Truly random freeze bonus corruptor spawn (can spawn multiple times with wider intervals)
        val hasBonusOnScreen = activeItems.any { it.category.isFreezeBonus && !it.sliced }
        val shouldSpawnBonus = !hasBonusOnScreen && freezeTimer <= 0f && (elapsedTimeSeconds >= freezeBonusTargetTime)
        if (shouldSpawnBonus) {
            freezeBonusSpawnedThisLevel = true
            lastBonusSpawnTime = elapsedTimeSeconds
            scheduleNextFreezeBonus()
            spawnFreezeBonusItem()
            nextSpawnTimer = 0.8f
            return
        }

        nextSpawnTimer -= dt
        if (nextSpawnTimer <= 0f) {
            // Cap: max 3 concurrent unsliced items on screen
            val unslicedCount = activeItems.count { !it.sliced }
            if (unslicedCount >= 3) {
                nextSpawnTimer = 0.5f
                return
            }

            val tier = getScoreDifficultyTier(score)

            // Calculate spawn interval with score tier reduction and hard mode multiplier
            val baseInterval = currentLevel.baseSpawnInterval
            val intervalReduction = tier.spawnIntervalReduction
            val diffIntervalMult = currentDifficulty.spawnIntervalMultiplier

            // Slower Tier 0 pacing: 25-30% slower to ensure clear early onboarding
            val tier0PacingMult = if (tier.tier == 0) 1.28f else 1.0f

            // Minimum interval clamped so round never becomes impossible or cluster-ambiguous
            val scaledInterval = (baseInterval * (1f - intervalReduction) * diffIntervalMult * tier0PacingMult)
                .coerceAtLeast(0.85f)

            nextSpawnTimer = scaledInterval + (random.nextFloat() * 0.25f)

            // Wave count scaling based on score tier and available space
            val waveCount = when {
                tier.tier >= 3 && unslicedCount <= 1 && random.nextFloat() < 0.35f -> 2
                else -> 1
            }

            for (i in 0 until waveCount) {
                spawnRandomItem(tier = tier, waveIndex = i, waveTotal = waveCount)
            }
        }
    }

    /**
     * Spawns a single item with constrained parabolic flight trajectory (Feature 3).
     */
    private fun spawnRandomItem(tier: ScoreDifficultyTier, waveIndex: Int, waveTotal: Int) {
        val category: ComplianceCategory
        val isBonus = random.nextFloat() < SHIELD_SPAWN_CHANCE

        if (isBonus) {
            category = ComplianceCategory.SHIELD
        } else {
            // Traps spawn at constant ~12-15% chance across missions
            val trapPool = if (currentLevel.allowedTraps.isNotEmpty()) currentLevel.allowedTraps else ComplianceCategory.TRAPS
            val isTrap = random.nextFloat() < TRAP_SPAWN_CHANCE

            if (isTrap) {
                category = trapPool.random(random)
            } else {
                // Ratio of legitimate decoys increases with difficulty tier (25% to 50%)
                val decoyChance = 0.25f + (tier.tier * 0.06f)
                val isDecoy = random.nextFloat() < decoyChance

                if (isDecoy && currentLevel.allowedLegitimate.isNotEmpty()) {
                    category = currentLevel.allowedLegitimate.random(random)
                } else {
                    val allowedViolations = currentLevel.allowedViolations
                    val hasSystemic = allowedViolations.contains(ComplianceCategory.SYSTEMIC_CORRUPTION)
                    val isSystemic = hasSystemic && random.nextFloat() < SYSTEMIC_CORRUPTION_CHANCE

                    category = if (isSystemic) {
                        ComplianceCategory.SYSTEMIC_CORRUPTION
                    } else {
                        allowedViolations.filter { it != ComplianceCategory.SYSTEMIC_CORRUPTION }.randomOrNull(random)
                            ?: allowedViolations.random(random)
                    }
                }
            }
        }

        // --- Dynamic Landscape Safe-Zone Trajectory Math ---
        val radius = itemRadius
        val horizontalMargin = radius + 24f // Full radius + safety buffer
        val usableWidth = (screenWidth - horizontalMargin * 2f).coerceAtLeast(120f)

        // Distribute wave items horizontally across landscape width
        val sectionWidth = usableWidth / waveTotal
        val minX = horizontalMargin + waveIndex * sectionWidth
        val x0 = minX + random.nextFloat() * sectionWidth
        val y0 = screenHeight + radius + (random.nextFloat() * 15f)

        // Vertical apex constraints based on dynamic screen height
        val apexMinY = (screenHeight * 0.18f).coerceAtLeast(60f)
        val apexMaxY = (screenHeight * 0.42f).coerceAtLeast(140f)
        val peakY = apexMinY + random.nextFloat() * (apexMaxY - apexMinY)

        val heightDiff = (y0 - peakY).coerceAtLeast(screenHeight * 0.45f)
        val dynamicGravity = (screenHeight * 1.75f).coerceIn(1200f, 2200f)

        // Combined speed multiplier: score tier increase + difficulty multiplier
        val rawSpeedMultiplier = (1.0f + tier.speedIncrease) * currentDifficulty.speedMultiplier
        val speedMultiplier = rawSpeedMultiplier.coerceIn(1.0f, 1.35f)

        // Vertical launch velocity: vy0 = -sqrt(2 * g * heightDiff) * speedMultiplier
        val vy0 = -sqrt(2f * dynamicGravity * heightDiff) * speedMultiplier
        val timeToApex = -vy0 / dynamicGravity
        val totalFlightTime = (timeToApex * 2f).coerceAtLeast(0.95f) // Minimum visible flight time

        // Horizontal target clamped strictly inside [horizontalMargin, screenWidth - horizontalMargin]
        val targetX = (horizontalMargin + 24f) + random.nextFloat() * (usableWidth - 48f)
        val vx0 = (targetX - x0) / totalFlightTime

        val item = GameItem(
            id = nextItemId++,
            category = category,
            isViolation = category.isViolation,
            initialX = x0,
            initialY = y0,
            initialVx = vx0,
            initialVy = vy0,
            initialRotation = random.nextFloat() * 360f,
            rotationSpeed = (random.nextFloat() - 0.5f) * 120f, // deg/s
            radius = radius
        )

        activeItems.add(item)
    }

    /**
     * Updates physics, gravity, rotation, and boundary conditions for all active items.
     */
    private fun updateItemsPhysics(dt: Float) {
        val dynamicGravity = (screenHeight * 1.75f).coerceIn(1200f, 2200f)

        for (i in activeItems.lastIndex downTo 0) {
            val item = activeItems[i]

            if (!item.sliced) {
                // Unsliced item parabolic flight under gravity
                item.vy += dynamicGravity * dt
                item.x += item.vx * dt
                item.y += item.vy * dt
                item.rotation += item.rotationSpeed * dt

                // Horizontal clamp safety check (zero off-screen clipping)
                val minX = item.radius
                val maxX = screenWidth - item.radius
                if (item.x < minX) {
                    item.x = minX
                    item.vx = -item.vx * 0.5f
                } else if (item.x > maxX) {
                    item.x = maxX
                    item.vx = -item.vx * 0.5f
                }

                // Check off-screen exit at bottom (after flight: top edge is beyond screen height)
                if (item.vy > 0f && item.y > screenHeight + item.radius) {
                    activeItems.removeAt(i)

                    if (item.isViolation) {
                        // Crucial Compliance Rule: letting a violation escape costs 1 life!
                        handleMissedViolation(item)
                    } else if (item.category.isTrap) {
                        // Letting a trap fall off-screen unsliced: NO penalty!
                        trapsAvoidedCount++
                        onTrapAvoided?.invoke(item)
                    } else if (item.category.isFreezeBonus) {
                        // Bonus corruptor missed unsliced: schedule next appearance with a wide gap
                        lastBonusSpawnTime = elapsedTimeSeconds
                        scheduleNextFreezeBonus()
                    }
                }
            } else {
                // Sliced item split animation: two halves separate and tumble apart under gravity
                item.vy += dynamicGravity * dt
                item.half1Vy += dynamicGravity * dt
                item.half2Vy += dynamicGravity * dt

                item.half1OffsetX += item.half1Vx * dt
                item.half1OffsetY += item.half1Vy * dt
                item.half2OffsetX += item.half2Vx * dt
                item.half2OffsetY += item.half2Vy * dt

                item.halfRotation1 += 220f * dt
                item.halfRotation2 -= 220f * dt

                // Fade out sliced pieces
                item.alpha = (item.alpha - dt * 1.6f).coerceAtLeast(0f)

                // Despawn once faded or fallen off screen
                if (item.alpha <= 0f || (item.y + item.half1OffsetY > screenHeight + 200f)) {
                    activeItems.removeAt(i)
                }
            }
        }
    }

    // =========================================================================
    // SLICE HIT-DETECTION & SCORING LOGIC (Feature B: Trap Rules)
    // =========================================================================

    fun processSliceSegment(x1: Float, y1: Float, x2: Float, y2: Float): Int {
        if (isGameOver) return 0

        val dx = x2 - x1
        val dy = y2 - y1
        val lenSq = dx * dx + dy * dy
        if (lenSq < 4f) return 0 // Ignore micro-jitters without movement

        val now = System.currentTimeMillis()
        if (currentStrokePoints.isEmpty()) {
            currentStrokePoints.add(StrokePoint(x1, y1, now))
        }
        currentStrokePoints.add(StrokePoint(x2, y2, now))

        var sliceCount = 0
        val sliceAngle = (atan2(dy, dx) * 180f / PI).toFloat()

        for (item in activeItems) {
            if (item.sliced) continue

            val cx = item.x
            val cy = item.y

            val t = (((cx - x1) * dx + (cy - y1) * dy) / lenSq).coerceIn(0f, 1f)
            val projX = x1 + t * dx
            val projY = y1 + t * dy

            val distSq = (cx - projX) * (cx - projX) + (cy - projY) * (cy - projY)
            val hitRadiusSq = item.radius * item.radius

            if (distSq <= hitRadiusSq) {
                if (item.category.isFreezeBonus) {
                    // Sensitive slice requirement for Corruptor bonus:
                    // Player must actually perform a full slice passing through/across the item's circle,
                    // not just touch or tap on it. Slice segment or accumulated stroke through the circle
                    // must have a cut length spanning the circle (> 1.25 * radius) or start/end crossing diameter.
                    val segLen = kotlin.math.sqrt(lenSq)
                    val r = item.radius
                    val passesAcrossCircle = if (segLen >= r * 1.25f) {
                        true
                    } else {
                        // Check accumulated points of this stroke that passed within or across the corruptor's bounds
                        var strokeMinX = Float.MAX_VALUE
                        var strokeMaxX = -Float.MAX_VALUE
                        var strokeMinY = Float.MAX_VALUE
                        var strokeMaxY = -Float.MAX_VALUE
                        var pointsInProximity = 0
                        val expandedRadiusSq = (r * 1.6f) * (r * 1.6f)

                        for (pt in currentStrokePoints) {
                            val pDistSq = (pt.x - cx) * (pt.x - cx) + (pt.y - cy) * (pt.y - cy)
                            if (pDistSq <= expandedRadiusSq) {
                                strokeMinX = minOf(strokeMinX, pt.x)
                                strokeMaxX = maxOf(strokeMaxX, pt.x)
                                strokeMinY = minOf(strokeMinY, pt.y)
                                strokeMaxY = maxOf(strokeMaxY, pt.y)
                                pointsInProximity++
                            }
                        }
                        val strokeSpanSq = (strokeMaxX - strokeMinX) * (strokeMaxX - strokeMinX) +
                                (strokeMaxY - strokeMinY) * (strokeMaxY - strokeMinY)
                        pointsInProximity >= 2 && strokeSpanSq >= (r * 1.25f) * (r * 1.25f)
                    }

                    if (!passesAcrossCircle) {
                        // Slice does not pass across the circle with sufficient swipe length; ignore touch/tap
                        continue
                    }

                    if (freezeTimer <= 0f) {
                        // First slice triggers the 5-second freeze mode!
                        freezeCorruptorX = item.x.coerceIn(item.radius, screenWidth - item.radius)
                        freezeCorruptorY = item.y.coerceIn(item.radius, screenHeight - item.radius)
                        item.x = freezeCorruptorX
                        item.y = freezeCorruptorY
                        item.vx = 0f
                        item.vy = 0f
                        item.isFrozen = true

                        // Automatically clear all other flying items on screen
                        activeItems.removeAll { it.id != item.id }

                        startFreezeMode()
                        handleFreezeBonusHit(item, projX, projY)
                        strokeCorruptorHitsCount++
                        sliceCount++
                    } else {
                        // Slices during active freeze
                        if (now - lastFreezeHitTimeMillis >= 40L) {
                            lastFreezeHitTimeMillis = now
                            handleFreezeBonusHit(item, projX, projY)
                            strokeCorruptorHitsCount++
                            sliceCount++
                        }
                    }
                } else if (freezeTimer <= 0f) {
                    sliceItem(item, sliceAngle, projX, projY)
                    sliceCount++
                }
            }
        }

        return sliceCount
    }

    /**
     * Spawns the freeze bonus corruptor item launched from the bottom, flying in a parabolic trajectory.
     */
    fun spawnFreezeBonusItem() {
        val radius = (itemRadius * 1.25f).coerceIn(75f, 104f)
        val horizontalMargin = radius + 32f
        val usableWidth = (screenWidth - horizontalMargin * 2f).coerceAtLeast(120f)

        val x0 = horizontalMargin + random.nextFloat() * usableWidth
        val y0 = screenHeight + radius + 15f

        val apexMinY = (screenHeight * 0.20f).coerceAtLeast(80f)
        val apexMaxY = (screenHeight * 0.42f).coerceAtLeast(140f)
        val peakY = apexMinY + random.nextFloat() * (apexMaxY - apexMinY)

        val heightDiff = (y0 - peakY).coerceAtLeast(screenHeight * 0.45f)
        val dynamicGravity = (screenHeight * 1.75f).coerceIn(1200f, 2200f)

        // Float slightly gentler so player has ample opportunity to notice and slice it
        val vy0 = -sqrt(2f * dynamicGravity * heightDiff) * 0.95f
        val timeToApex = -vy0 / dynamicGravity
        val totalFlightTime = (timeToApex * 2.1f).coerceAtLeast(1.2f)

        val targetX = (horizontalMargin + 25f) + random.nextFloat() * (usableWidth - 50f)
        val vx0 = (targetX - x0) / totalFlightTime

        val item = GameItem(
            id = nextItemId++,
            category = ComplianceCategory.BONUS_CORRUPTOR,
            isViolation = false,
            initialX = x0,
            initialY = y0,
            initialVx = vx0,
            initialVy = vy0,
            initialRotation = random.nextFloat() * 360f,
            rotationSpeed = (random.nextFloat() - 0.5f) * 80f,
            radius = radius,
            initialSliced = false,
            initialBonusHits = 0,
            initialIsFrozen = false
        )
        activeItems.add(item)
    }

    private fun startFreezeMode() {
        freezeTimer = 5.0f
        freezeBonusHits = 0
        lastFreezeHitTimeMillis = 0L
        onFreezeStarted?.invoke()
    }

    private fun endFreezeMode() {
        freezeTimer = 0f
        val corruptor = activeItems.find { it.category.isFreezeBonus && !it.sliced }
        if (corruptor != null) {
            corruptor.isFrozen = false
            corruptor.sliced = true
            corruptor.sliceAngle = 45f
            corruptor.half1Vx = -220f
            corruptor.half1Vy = -240f
            corruptor.half2Vx = 220f
            corruptor.half2Vy = -240f
            corruptor.half1OffsetX = -10f
            corruptor.half2OffsetX = 10f

            // Grand celebration coin & money shower
            spawnParticleBurst(corruptor.x, corruptor.y, count = 45, color = 0xFFFFD700)
            spawnParticleBurst(corruptor.x, corruptor.y, count = 30, color = 0xFF00E676)
            spawnParticleBurst(corruptor.x, corruptor.y, count = 20, color = 0xFF69F0AE)
        }

        val totalBonusPts = freezeBonusHits * 10
        onFreezeEnded?.invoke(freezeBonusHits, totalBonusPts)
        nextSpawnTimer = 1.0f

        // Schedule next random bonus corruptor appearance (distance between spawns is substantially wider)
        lastBonusSpawnTime = elapsedTimeSeconds
        scheduleNextFreezeBonus()
    }

    private fun handleFreezeBonusHit(item: GameItem, hitX: Float, hitY: Float) {
        freezeBonusHits++
        item.bonusHits = freezeBonusHits
        val ptsPerSlice = 10
        score += ptsPerSlice

        // Burst gold and money particles
        spawnParticleBurst(hitX, hitY, count = 14, color = 0xFFFFD700)
        spawnParticleBurst(hitX, hitY, count = 8, color = 0xFF4CAF50)

        addPopup("+$ptsPerSlice", hitX, hitY - 20f, color = 0xFFFFD700, scale = 1.25f)

        onFreezeBonusSliced?.invoke(item, freezeBonusHits, ptsPerSlice)
    }

    private fun sliceItem(item: GameItem, sliceAngle: Float, hitX: Float, hitY: Float) {
        if (item.category.isFreezeBonus) {
            handleFreezeBonusHit(item, hitX, hitY)
            return
        }

        item.sliced = true
        item.isCombo4xSliced = false
        item.sliceAngle = sliceAngle

        val sliceRad = sliceAngle * (PI.toFloat() / 180f)
        val normalX = -sin(sliceRad)
        val normalY = cos(sliceRad)
        val separationSpeed = 280f

        item.half1Vx = item.vx + normalX * separationSpeed
        item.half1Vy = item.vy + normalY * separationSpeed
        item.half2Vx = item.vx - normalX * separationSpeed
        item.half2Vy = item.vy - normalY * separationSpeed

        if (item.category.isBonus) {
            // Bonus Shield
            handleShieldSliced(item, hitX, hitY)
        } else if (item.category.isTrap) {
            // Feature B: Trap Sliced (-10 pts, reset combo, NO life deducted)
            handleTrapSliced(item, hitX, hitY)
        } else if (item.isViolation) {
            // Violation Sliced (+10 pts * comboMultiplier)
            handleViolationSliced(item, hitX, hitY)
        } else {
            // Legitimate Procedure Sliced (-1 life, reset combo)
            handleWrongSlice(item, hitX, hitY)
        }
    }

    /**
     * Correct slice on a compliance violation:
     * - Base points * comboMultiplier (or doubled for 3+ multi-slice strokes).
     * - Increases combo streak & multiplier up to 4x.
     * - Multi-slice bonus (3+ violations in one continuous stroke) awards 2x multiplier.
     * - Gold and Cyan particle bursts.
     */
    private fun handleViolationSliced(item: GameItem, hitX: Float, hitY: Float) {
        comboStreak++
        if (comboStreak > maxComboStreak) {
            maxComboStreak = comboStreak
        }

        val oldMultiplier = comboMultiplier
        comboMultiplier = when {
            comboStreak >= 10 -> 4
            comboStreak >= 6 -> 3
            comboStreak >= 3 -> 2
            else -> 1
        }

        item.isCombo4xSliced = false

        val combo4xPopupText = when {
            currentLanguage.equals("ja", ignoreCase = true) -> "コンボ X4!"
            currentLanguage.equals("in", ignoreCase = true) || currentLanguage.equals("id", ignoreCase = true) -> "KOMBO X4!"
            else -> "COMBO X4!"
        }

        if (oldMultiplier < 4 && comboMultiplier == 4) {
            spawnParticleBurst(hitX, hitY, count = 28, color = 0xFFFFC857)
            addPopup(combo4xPopupText, hitX, hitY - 70f, color = 0xFFFFD700, scale = 1.65f)
            onComboTriggered?.invoke(4, hitX, hitY)
        }

        strokeViolationCount++

        val basePts = item.category.basePoints // Base points
        val normalPoints = (basePts * comboMultiplier * currentDifficulty.scoreMultiplier).toInt()
        val finalPoints: Int

        when {
            strokeViolationCount == 3 -> {
                // Multi-slice combo bonus triggered: 2x multiplier for this stroke!
                // Retroactively double the points of the previous 2 hits from this stroke, plus 2x for this 3rd hit
                val thisHitPoints = normalPoints * 2
                val retroactiveBonus = strokePointsEarned // doubles previous 2 hits
                finalPoints = thisHitPoints + retroactiveBonus
                score += finalPoints
                strokeBonusAwarded = true

                spawnParticleBurst(hitX, hitY, count = 36, color = 0xFF00E5FF) // Electric cyan
                spawnParticleBurst(hitX, hitY, count = 20, color = 0xFFFFC857) // Warm gold

                addPopup("+$thisHitPoints (x${comboMultiplier * 2})", hitX, hitY - 20f, color = 0xFFFFC857, scale = 1.25f)
            }
            strokeViolationCount == 4 -> {
                // 4-item multi-slice in one stroke: Quad Slice Combo 4x!
                val multiHitPoints = normalPoints * 2
                finalPoints = multiHitPoints
                score += finalPoints

                spawnParticleBurst(hitX, hitY, count = 30, color = 0xFFFFC857)
                addPopup("+$multiHitPoints (x${comboMultiplier * 2})", hitX, hitY - 20f, color = 0xFFFFC857, scale = 1.3f)
                //addPopup(combo4xPopupText, hitX, hitY - 65f, color = 0xFFFFD700, scale = 1.65f)
                //onComboTriggered?.invoke(4, hitX, hitY)
            }
            strokeViolationCount > 4 -> {
                // Chained 5th+ slice in this stroke continues to receive 2x multiplier
                val multiHitPoints = normalPoints * 2
                finalPoints = multiHitPoints
                score += finalPoints

                spawnParticleBurst(hitX, hitY, count = 35, color = 0xFF00E5FF)
                addPopup("+$multiHitPoints (x${comboMultiplier * 2})", hitX, hitY - 20f, color = 0xFFFFC857, scale = 1.25f)
            }
            else -> {
                finalPoints = normalPoints
                score += finalPoints
                strokePointsEarned += finalPoints

                val particleCountMult = if (comboMultiplier >= 4) 3 else 1
                
                spawnParticleBurst(hitX, hitY, count = 26 * particleCountMult, color = 0xFFFFC857)
                spawnParticleBurst(hitX, hitY, count = 10 * particleCountMult, color = 0xFFFFD700)

                if (comboMultiplier >= 4) {
                    spawnParticleBurst(hitX, hitY, count = 15, color = 0xFF00E5FF) 
                }

                val comboLabel = when {
                    currentLanguage.equals("ja", ignoreCase = true) -> "コンボ X4"
                    currentLanguage.equals("in", ignoreCase = true) || currentLanguage.equals("id", ignoreCase = true) -> "KOMBO X4"
                    else -> "COMBO X4"
                }

                val popupText = if (comboMultiplier >= 4) {
                    "+$finalPoints ($comboLabel)"
                } else if (comboMultiplier > 1) {
                    "+$finalPoints (x$comboMultiplier)"
                } else {
                    "+$finalPoints"
                }
                val popupColor = if (comboMultiplier >= 4) 0xFFFFD700 else 0xFFFFC857
                val popupScale = if (comboMultiplier >= 4) 1.35f else 1.2f
                addPopup(popupText, hitX, hitY - 20f, color = popupColor, scale = popupScale)
            }
        }

        totalViolationsSlicedCount++
        val currentCount = slicedCategoriesMap.getOrDefault(item.category, 0)
        slicedCategoriesMap[item.category] = currentCount + 1

        val effectiveMultiplier = if (strokeViolationCount >= 3) comboMultiplier * 2 else comboMultiplier
        onViolationSliced?.invoke(item, finalPoints, effectiveMultiplier)
    }

    /**
     * Trap sliced (Feature B):
     * - -10 points.
     * - Resets combo streak to 0 (1x multiplier).
     * - Does NOT subtract a life!
     * - Warning orange popup.
     */
    private fun handleTrapSliced(item: GameItem, hitX: Float, hitY: Float) {
        trapsSlicedCount++
        comboStreak = 0
        comboMultiplier = 1
        score = (score - 10).coerceAtLeast(0)

        // Amber-Orange warning burst
        spawnParticleBurst(hitX, hitY, count = 22, color = 0xFFFF7043)

        addPopup("-10 (TRAP!)", hitX, hitY - 20f, color = 0xFFFF7043, scale = 1.25f)

        onTrapSliced?.invoke(item)
    }

    /**
     * Deducts 1 life. If the player has an active blue shield bonus life,
     * the shield absorbs the damage and is consumed first, protecting core lives.
     * Returns true if a shield life was consumed.
     */
    private fun deductLife(): Boolean {
        val hadShield = shieldBonusLives > 0
        if (hadShield) {
            shieldBonusLives--
        } else {
            baseLives = (baseLives - 1).coerceAtLeast(0)
        }
        lives = (baseLives + shieldBonusLives).coerceAtLeast(0)
        return hadShield
    }

    /**
     * Wrong slice on legitimate item:
     * - Deducts 1 life.
     * - Resets combo streak to 0 (1x).
     * - Red particle burst + warning popup.
     */
    private fun handleWrongSlice(item: GameItem, hitX: Float, hitY: Float) {
        comboStreak = 0
        comboMultiplier = 1
        val hadShield = deductLife()

        spawnParticleBurst(hitX, hitY, count = 26, color = if (hadShield) 0xFF00E5FF else 0xFFE14B5A)
        addPopup(if (hadShield) "SHIELD BROKEN!" else "-1 LIFE!", hitX, hitY - 20f, color = if (hadShield) 0xFF00E5FF else 0xFFE14B5A, scale = 1.3f)

        onWrongSlice?.invoke(item)

        if (lives <= 0) {
            triggerGameOver()
        }
    }

    /**
     * Missed violation fell off screen:
     * - Deducts 1 life.
     * - Resets combo streak to 0 (1x).
     */
    private fun handleMissedViolation(item: GameItem) {
        comboStreak = 0
        comboMultiplier = 1
        val hadShield = deductLife()

        addPopup(
            if (hadShield) "MISSED! SHIELD LOST" else "MISSED! -1 LIFE",
            item.x.coerceIn(100f, screenWidth - 100f),
            screenHeight - 60f,
            color = if (hadShield) 0xFF00E5FF else 0xFFE14B5A,
            scale = 1.1f
        )

        onViolationMissed?.invoke(item)

        if (lives <= 0) {
            triggerGameOver()
        }
    }

    /**
     * Shield bonus pickup:
     * - If player has 1 or 2 red lives (baseLives < 3), restores 1 RED life.
     * - If player already has full 3 red lives (baseLives == 3), grants +1 BLUE shield bonus life (max 4 lives total).
     * - In all cases, awards bonus points.
     */
    private fun handleShieldSliced(item: GameItem, hitX: Float, hitY: Float) {
        score += (item.category.basePoints * currentDifficulty.scoreMultiplier).toInt()
        val gainedBlueShield: Boolean
        val gainedLife: Boolean

        if (baseLives < DEFAULT_LIVES) {
            // Restore 1 red life first if player has 1 or 2 lives
            baseLives++
            gainedLife = true
            gainedBlueShield = false
        } else if (shieldBonusLives < 1) {
            // Player already has 3 full red lives; grant 1 blue shield bonus life (4 lives max)
            shieldBonusLives = 1
            gainedLife = true
            gainedBlueShield = true
        } else {
            gainedLife = false
            gainedBlueShield = false
        }
        lives = (baseLives + shieldBonusLives).coerceAtMost(MAX_LIVES)

        if (gainedBlueShield) {
            spawnParticleBurst(hitX, hitY, count = 25, color = 0xFF00E5FF)
            spawnParticleBurst(hitX, hitY, count = 20, color = 0xFFFFD54F)
            addPopup("+1 SHIELD LIFE!", hitX, hitY - 20f, color = 0xFF00E5FF, scale = 1.3f)
        } else if (gainedLife) {
            spawnParticleBurst(hitX, hitY, count = 25, color = 0xFFFF3B30)
            spawnParticleBurst(hitX, hitY, count = 20, color = 0xFFFFD54F)
            addPopup("+1 LIFE!", hitX, hitY - 20f, color = 0xFFFF3B30, scale = 1.3f)
        } else {
            spawnParticleBurst(hitX, hitY, count = 20, color = 0xFFFFD54F)
            addPopup("+25 PTS!", hitX, hitY - 20f, color = 0xFFFFD54F, scale = 1.2f)
        }

        onShieldSliced?.invoke(item)
    }

    private fun triggerGameOver() {
        if (!isGameOver) {
            isGameOver = true
            onGameOver?.invoke(score)
        }
    }

    // =========================================================================
    // PARTICLES & POPUPS
    // =========================================================================

    private fun spawnParticleBurst(originX: Float, originY: Float, count: Int, color: Long) {
        for (i in 0 until count) {
            val angle = random.nextFloat() * 2f * PI.toFloat()
            val speed = 90f + random.nextFloat() * 320f
            val size = 5f + random.nextFloat() * 7f
            val maxLife = 0.35f + random.nextFloat() * 0.35f

            particles.add(
                Particle(
                    x = originX,
                    y = originY,
                    vx = cos(angle) * speed,
                    vy = sin(angle) * speed,
                    color = color,
                    size = size,
                    alpha = 1.0f,
                    life = 0f,
                    maxLife = maxLife
                )
            )
        }
    }

    private fun updateParticles(dt: Float) {
        val iterator = particles.iterator()
        while (iterator.hasNext()) {
            val p = iterator.next()
            p.life += dt
            if (p.life >= p.maxLife) {
                iterator.remove()
                continue
            }
            p.x += p.vx * dt
            p.y += (p.vy + 500f * dt) * dt
            p.alpha = (1f - (p.life / p.maxLife)).coerceIn(0f, 1f)
        }
    }

    private fun addPopup(text: String, x: Float, y: Float, color: Long, scale: Float = 1.0f) {
        popups.add(
            FloatingPopup(
                id = nextPopupId++,
                text = text,
                x = x,
                y = y,
                vy = -100f,
                color = color,
                alpha = 1.0f,
                scale = scale,
                life = 0f,
                maxLife = 0.85f
            )
        )
    }

    private fun updatePopups(dt: Float) {
        val iterator = popups.iterator()
        while (iterator.hasNext()) {
            val popup = iterator.next()
            popup.life += dt
            if (popup.life >= popup.maxLife) {
                iterator.remove()
                continue
            }
            popup.y += popup.vy * dt
            val progress = popup.life / popup.maxLife
            popup.alpha = (1f - progress).coerceIn(0f, 1f)
            popup.scale += dt * 0.2f
        }
    }

    /**
     * Returns list of sliced violation categories and counts for the Result Screen recap.
     */
    fun getSlicedCategoriesSummary(): List<SlicedCategoryRecord> {
        return slicedCategoriesMap.map { (cat, count) ->
            SlicedCategoryRecord(category = cat, count = count)
        }.sortedByDescending { it.count }
    }

    // =========================================================================
    // TESTING HELPERS
    // =========================================================================

    fun spawnItemManually(
        category: ComplianceCategory,
        x: Float,
        y: Float,
        vx: Float = 0f,
        vy: Float = 0f,
        radius: Float = 75f
    ): GameItem {
        val item = GameItem(
            id = nextItemId++,
            category = category,
            isViolation = category.isViolation,
            initialX = x,
            initialY = y,
            initialVx = vx,
            initialVy = vy,
            radius = radius
        )
        activeItems.add(item)
        return item
    }
}
