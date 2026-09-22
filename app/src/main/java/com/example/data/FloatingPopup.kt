package com.example.data

data class FloatingPopup(
    val id: Long,
    val text: String,
    var x: Float,
    var y: Float,
    var vy: Float = -90f,
    val color: Long,
    var alpha: Float = 1.0f,
    var scale: Float = 1.0f,
    var life: Float = 0f,
    val maxLife: Float = 0.9f
)
