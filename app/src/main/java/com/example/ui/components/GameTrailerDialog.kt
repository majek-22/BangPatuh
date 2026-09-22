package com.example.ui.components

import android.net.Uri
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.VideoView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.viewinterop.AndroidView
import com.example.R

/**
 * Clean, distraction-free Game Trailer player for the MainMenu screen.
 * Plays the original video automatically without any on-screen buttons or overlays.
 * Once the video finishes playing, it automatically closes directly.
 */
@Composable
fun GameTrailerDialog(
    onDismiss: () -> Unit,
    onPauseMusic: () -> Unit = {},
    onResumeMusic: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var videoViewRef by remember { mutableStateOf<VideoView?>(null) }

    // Pause BGM when trailer starts; stop video and resume BGM when trailer closes
    DisposableEffect(Unit) {
        onPauseMusic()
        onDispose {
            try {
                videoViewRef?.apply {
                    setOnPreparedListener(null)
                    setOnCompletionListener(null)
                    setOnErrorListener(null)
                    if (isPlaying) {
                        stopPlayback()
                    }
                }
            } catch (_: Exception) {}
            onResumeMusic()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                // Tapping anywhere allows closing or skipping the video directly
                onDismiss()
            }
            .testTag("game_trailer_container"),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            factory = { ctx ->
                VideoView(ctx).apply {
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    keepScreenOn = true

                    val videoUri = Uri.parse("android.resource://${ctx.packageName}/${R.raw.game_trailer_1}")
                    setVideoURI(videoUri)

                    setOnPreparedListener { mp ->
                        mp.setVolume(1f, 1f)
                        mp.isLooping = false
                        start()
                    }

                    setOnCompletionListener {
                        // Automatically close directly when the video finishes
                        onDismiss()
                    }

                    setOnErrorListener { _, _, _ ->
                        // Close automatically if there is any playback error
                        onDismiss()
                        true
                    }

                    videoViewRef = this
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}
