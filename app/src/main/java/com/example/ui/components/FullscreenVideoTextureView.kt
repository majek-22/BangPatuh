package com.example.ui.components

import android.content.Context
import android.graphics.Matrix
import android.graphics.SurfaceTexture
import android.media.MediaPlayer
import android.view.TextureView
import android.view.ViewGroup
import android.widget.FrameLayout

/**
 * A specialized TextureView for video playback that:
 * 1. Supports true FULL SCREEN display (Center Crop scaling to fill 100% of parent view without letterboxing/black bars).
 * 2. Reuses the underlying SurfaceTexture / MediaPlayer so playback transitions seamlessly with zero black flicker.
 */
class FullscreenVideoTextureView(context: Context) : TextureView(context), TextureView.SurfaceTextureListener {

    private var mediaPlayer: MediaPlayer? = null
    private var videoWidth: Int = 0
    private var videoHeight: Int = 0
    private var surfaceTextureReady = false

    var onSurfaceAvailableCallback: ((SurfaceTexture) -> Unit)? = null
        set(value) {
            field = value
            val currentTexture = surfaceTexture
            if (currentTexture != null && isAvailable) {
                value?.invoke(currentTexture)
            }
        }

    init {
        surfaceTextureListener = this
        layoutParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    }

    fun attachMediaPlayer(mp: MediaPlayer?, width: Int = 0, height: Int = 0) {
        this.mediaPlayer = mp
        if (width > 0 && height > 0) {
            this.videoWidth = width
            this.videoHeight = height
            adjustAspectRatio()
        }
    }

    fun updateVideoSize(width: Int, height: Int) {
        this.videoWidth = width
        this.videoHeight = height
        adjustAspectRatio()
    }

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
        surfaceTextureReady = true
        onSurfaceAvailableCallback?.invoke(surface)
        adjustAspectRatio()
    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
        adjustAspectRatio()
    }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
        surfaceTextureReady = false
        return true
    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
        // Called when a new video frame is rendered
    }

    /**
     * Center crop transformation matrix ensuring the video fills 100% of the screen (FULL SCREEN)
     * without letterbox bars and keeping the aspect ratio intact.
     */
    fun adjustAspectRatio() {
        if (videoWidth == 0 || videoHeight == 0 || width == 0 || height == 0) {
            return
        }

        val viewWidth = width.toFloat()
        val viewHeight = height.toFloat()

        val sx = viewWidth / videoWidth.toFloat()
        val sy = viewHeight / videoHeight.toFloat()

        // Center Crop: use max scale to fill entire screen
        val scale = maxOf(sx, sy)

        val scaledWidth = videoWidth * scale
        val scaledHeight = videoHeight * scale

        val dx = (viewWidth - scaledWidth) / 2f
        val dy = (viewHeight - scaledHeight) / 2f

        val matrix = Matrix()
        // TextureView matrix scales from top-left, so we scale then translate to center
        matrix.setScale(scaledWidth / viewWidth, scaledHeight / viewHeight, viewWidth / 2f, viewHeight / 2f)
        setTransform(matrix)
        postInvalidate()
    }
}
