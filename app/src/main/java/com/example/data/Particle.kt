package com.example.data

data class Particle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    val color: Long,
    var size: Float,
    var alpha: Float = 1.0f,
    var life: Float = 0f,
    val maxLife: Float = 0.6f
)
