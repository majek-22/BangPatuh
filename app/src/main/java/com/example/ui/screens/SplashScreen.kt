package com.example.ui.screens

import android.content.Context
import android.content.res.AssetFileDescriptor
import android.graphics.SurfaceTexture
import android.media.MediaPlayer
import android.net.Uri
import android.view.Surface
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.R
import com.example.ui.components.FullscreenVideoTextureView

private enum class SplashStage {
    CORPORATE_FLASH,
    GAME_TRAILER
}

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var stage by remember { mutableStateOf(SplashStage.CORPORATE_FLASH) }
    var hasFinished by remember { mutableStateOf(false) }

    // Persistent MediaPlayer & Surface to guarantee ZERO black screen flicker
    var activeMediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var activeSurface by remember { mutableStateOf<Surface?>(null) }
    var activeSurfaceTexture by remember { mutableStateOf<SurfaceTexture?>(null) }
    var textureViewRef by remember { mutableStateOf<FullscreenVideoTextureView?>(null) }

    val safeReleaseMediaPlayer = {
        val mp = activeMediaPlayer
        activeMediaPlayer = null
        if (mp != null) {
            try {
                mp.setOnPreparedListener(null)
                mp.setOnCompletionListener(null)
                mp.setOnErrorListener(null)
                mp.setOnVideoSizeChangedListener(null)
                try {
                    if (mp.isPlaying) {
                        mp.pause()
                    }
                } catch (_: Exception) {}
                mp.reset()
                mp.release()
            } catch (_: Exception) {}
        }
    }

    val finishAll = {
        if (!hasFinished) {
            hasFinished = true
            safeReleaseMediaPlayer()
            try {
                activeSurface?.release()
                activeSurface = null
            } catch (_: Exception) {}
            onSplashFinished()
        }
    }

    // Function to play a video file onto the existing surface
    fun playVideo(resId: Int, onComplete: () -> Unit) {
        if (hasFinished) return
        try {
            val mp = activeMediaPlayer ?: MediaPlayer().also { activeMediaPlayer = it }
            // Detach listeners before resetting to prevent stale callbacks
            mp.setOnPreparedListener(null)
            mp.setOnCompletionListener(null)
            mp.setOnErrorListener(null)
            mp.setOnVideoSizeChangedListener(null)

            mp.reset()
            activeSurface?.let { surf ->
                if (surf.isValid) {
                    mp.setSurface(surf)
                }
            }
            mp.setScreenOnWhilePlaying(true)
            mp.setVolume(1f, 1f)
            mp.isLooping = false

            val afd: AssetFileDescriptor = context.resources.openRawResourceFd(resId)
            mp.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
            afd.close()

            mp.setOnVideoSizeChangedListener { _, width, height ->
                if (width > 0 && height > 0) {
                    textureViewRef?.updateVideoSize(width, height)
                }
            }

            mp.setOnPreparedListener { preparedMp ->
                if (!hasFinished) {
                    if (preparedMp.videoWidth > 0 && preparedMp.videoHeight > 0) {
                        textureViewRef?.updateVideoSize(preparedMp.videoWidth, preparedMp.videoHeight)
                    }
                    preparedMp.start()
                }
            }

            mp.setOnCompletionListener {
                if (!hasFinished) {
                    onComplete()
                }
            }

            mp.setOnErrorListener { _, _, _ ->
                if (!hasFinished) {
                    onComplete()
                }
                true
            }

            mp.prepareAsync()
        } catch (e: Exception) {
            if (!hasFinished) {
                onComplete()
            }
        }
    }

    fun advanceToTrailer() {
        if (stage == SplashStage.CORPORATE_FLASH && !hasFinished) {
            stage = SplashStage.GAME_TRAILER
            playVideo(R.raw.game_trailer_1) {
                finishAll()
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            safeReleaseMediaPlayer()
            try {
                activeSurface?.release()
                activeSurface = null
            } catch (_: Exception) {}
        }
    }

    // Default container is solid White to ensure initial launch screen has no black flicker
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                if (stage == SplashStage.CORPORATE_FLASH) {
                    advanceToTrailer()
                } else {
                    finishAll()
                }
            }
            .testTag("splash_container"),
        contentAlignment = Alignment.Center
    ) {
        // Fullscreen Hardware-Accelerated Video Surface
        AndroidView(
            factory = { ctx ->
                FullscreenVideoTextureView(ctx).apply {
                    onSurfaceAvailableCallback = { surfaceTexture ->
                        activeSurfaceTexture = surfaceTexture
                        val newSurface = Surface(surfaceTexture)
                        activeSurface = newSurface
                        activeMediaPlayer?.setSurface(newSurface)

                        // Start playing the corporate splash video immediately once surface is ready
                        playVideo(R.raw.splash_video) {
                            advanceToTrailer()
                        }
                    }
                    textureViewRef = this
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .testTag("splash_fullscreen_video_view")
        )

        // Subtle skip prompt in bottom-right corner when in GAME_TRAILER stage
        AnimatedVisibility(
            visible = stage == SplashStage.GAME_TRAILER,
            enter = fadeIn(tween(300)),
            exit = fadeOut(tween(200)),
            modifier = Modifier.align(Alignment.BottomEnd)
        ) {
            Box(
                modifier = Modifier
                    .padding(end = 24.dp, bottom = 20.dp)
                    .background(
                        color = Color.Black.copy(alpha = 0.55f),
                        shape = RoundedCornerShape(14.dp)
                    )
                    .padding(horizontal = 14.dp, vertical = 7.dp)
                    .testTag("splash_skip_prompt")
            ) {
                Text(
                    text = stringResource(R.string.splash_tap_to_skip),
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 12.sp
                )
            }
        }
    }
}
