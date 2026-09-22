package com.example.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class GameItem(
    val id: Long,
    val category: ComplianceCategory,
    val isViolation: Boolean,
    initialX: Float,
    initialY: Float,
    initialVx: Float,
    initialVy: Float,
    initialRotation: Float = 0f,
    var rotationSpeed: Float = 0f,
    val radius: Float = 75f, // Hit circle radius in px
    initialSliced: Boolean = false,
    initialSliceAngle: Float = 0f, // Cut angle in degrees
    initialHalf1OffsetX: Float = 0f,
    initialHalf1OffsetY: Float = 0f,
    initialHalf2OffsetX: Float = 0f,
    initialHalf2OffsetY: Float = 0f,
    var half1Vx: Float = 0f,
    var half1Vy: Float = 0f,
    var half2Vx: Float = 0f,
    var half2Vy: Float = 0f,
    initialHalfRotation1: Float = 0f,
    initialHalfRotation2: Float = 0f,
    initialAlpha: Float = 1.0f,
    initialBonusHits: Int = 0,
    initialIsFrozen: Boolean = false,
    initialIsCombo4xSliced: Boolean = false
) {
    var x: Float by mutableFloatStateOf(initialX)
    var y: Float by mutableFloatStateOf(initialY)
    var vx: Float by mutableFloatStateOf(initialVx)
    var vy: Float by mutableFloatStateOf(initialVy)
    var rotation: Float by mutableFloatStateOf(initialRotation)
    var sliced: Boolean by mutableStateOf(initialSliced)
    var isCombo4xSliced: Boolean by mutableStateOf(initialIsCombo4xSliced)
    var sliceAngle: Float by mutableFloatStateOf(initialSliceAngle)
    var half1OffsetX: Float by mutableFloatStateOf(initialHalf1OffsetX)
    var half1OffsetY: Float by mutableFloatStateOf(initialHalf1OffsetY)
    var half2OffsetX: Float by mutableFloatStateOf(initialHalf2OffsetX)
    var half2OffsetY: Float by mutableFloatStateOf(initialHalf2OffsetY)
    var halfRotation1: Float by mutableFloatStateOf(initialHalfRotation1)
    var halfRotation2: Float by mutableFloatStateOf(initialHalfRotation2)
    var alpha: Float by mutableFloatStateOf(initialAlpha)
    var bonusHits: Int by mutableIntStateOf(initialBonusHits)
    var isFrozen: Boolean by mutableStateOf(initialIsFrozen)

    val isFreezeBonus: Boolean get() = category.isFreezeBonus
}
