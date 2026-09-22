package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.util.Log
import com.example.R

/**
 * MusicManager handles background music playback with track switching and mute control.
 * It resolves resources directly via R.raw constants to prevent package-name mismatch
 * issues between applicationId and namespace.
 */
class MusicManager(private val context: Context) {

    companion object {
        private const val TAG = "MusicManager"
        private const val DEFAULT_MUSIC_VOLUME = 0.75f
    }

    private var mediaPlayer: MediaPlayer? = null
    private var currentTrackType: TrackType = TrackType.NONE
    private var isMuted: Boolean = false
    private var currentPlaybackSpeed: Float = 1.0f

    var isTrailerPlaying: Boolean = false
        set(value) {
            field = value
            if (value) {
                pause()
            }
        }

    enum class TrackType {
        NONE, MENU, GAMEPLAY
    }

    fun setPlaybackSpeed(speed: Float) {
        currentPlaybackSpeed = speed
        try {
            mediaPlayer?.let { player ->
                if (player.isPlaying) {
                    val params = player.playbackParams
                    params.speed = speed
                    player.playbackParams = params
                    Log.i(TAG, "setPlaybackSpeed: speed=$speed applied to MediaPlayer")
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to set playback speed: ${e.message}")
        }
    }

    fun getPlaybackSpeed(): Float = currentPlaybackSpeed

    fun setMuted(muted: Boolean) {
        isMuted = muted
        val volume = if (muted) 0.0f else DEFAULT_MUSIC_VOLUME
        try {
            mediaPlayer?.setVolume(volume, volume)
            Log.i(TAG, "setMuted: muted=$muted, applied volume=$volume to mediaPlayer")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update volume on MediaPlayer", e)
        }
    }

    fun isMuted(): Boolean = isMuted

    fun playMenuTrack() {
        playMenuTheme()
    }

    fun playGameplayTrack() {
        playGameplayTheme()
    }

    fun playMenuTheme() {
        setPlaybackSpeed(1.0f)
        if (isTrailerPlaying) {
            Log.d(TAG, "playMenuTheme: suppressed because trailer is playing")
            return
        }
        if (currentTrackType == TrackType.MENU && mediaPlayer?.isPlaying == true) {
            Log.d(TAG, "playMenuTheme: menu track is already playing")
            return
        }
        startTrack("menu_theme", TrackType.MENU)
    }

    fun playGameplayTheme() {
        if (isTrailerPlaying) {
            Log.d(TAG, "playGameplayTheme: suppressed because trailer is playing")
            return
        }
        if (currentTrackType == TrackType.GAMEPLAY && mediaPlayer?.isPlaying == true) {
            Log.d(TAG, "playGameplayTheme: gameplay track is already playing")
            return
        }
        startTrack("gameplay_theme", TrackType.GAMEPLAY)
    }

    private fun resolveRawResourceId(resourceName: String): Int {
        // Direct compile-time ID resolution (safe against namespace/applicationId mismatches)
        val directId = when (resourceName) {
            "menu_theme" -> R.raw.menu_theme
            "gameplay_theme" -> R.raw.gameplay_theme
            else -> 0
        }
        if (directId != 0) {
            return directId
        }

        // Fallback to identifier lookup with namespace and package name
        val fromNamespace = context.resources.getIdentifier(resourceName, "raw", "com.example")
        if (fromNamespace != 0) return fromNamespace

        return context.resources.getIdentifier(resourceName, "raw", context.packageName)
    }

    private fun startTrack(resourceName: String, trackType: TrackType) {
        stop()
        currentTrackType = trackType

        val resId = resolveRawResourceId(resourceName)
        if (resId == 0) {
            Log.e(TAG, "Audio resource '$resourceName' not found in res/raw (resId=0). Audio will remain silent.")
            return
        }

        Log.i(TAG, "Starting audio track '$resourceName' with resId=$resId, trackType=$trackType, isMuted=$isMuted")

        try {
            // First attempt with MediaPlayer.create
            var player = MediaPlayer.create(context, resId)
            if (player == null) {
                Log.w(TAG, "MediaPlayer.create returned null for $resourceName, trying openRawResourceFd fallback...")
                val afd = context.resources.openRawResourceFd(resId)
                if (afd != null) {
                    player = MediaPlayer().apply {
                        setAudioAttributes(
                            AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_GAME)
                                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                .build()
                        )
                        setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                        afd.close()
                        prepare()
                    }
                }
            }

            if (player == null) {
                Log.e(TAG, "Could not initialize MediaPlayer for audio resource '$resourceName' (resId=$resId).")
                return
            }

            val volume = if (isMuted) 0.0f else DEFAULT_MUSIC_VOLUME
            player.isLooping = true
            player.setVolume(volume, volume)
            player.start()

            if (currentPlaybackSpeed != 1.0f) {
                try {
                    val params = player.playbackParams
                    params.speed = currentPlaybackSpeed
                    player.playbackParams = params
                    Log.i(TAG, "Applied playback speed $currentPlaybackSpeed on track start")
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to apply playback speed on track start: ${e.message}")
                }
            }

            mediaPlayer = player
            Log.i(TAG, "Successfully started background track '$resourceName' (isPlaying=${player.isPlaying}, volume=$volume)")
        } catch (e: Exception) {
            Log.e(TAG, "Fatal error playing audio track '$resourceName': ${e.message}", e)
            mediaPlayer?.release()
            mediaPlayer = null
        }
    }

    fun pause() {
        try {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.pause()
                Log.d(TAG, "Paused MediaPlayer")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error pausing MediaPlayer: ${e.message}", e)
        }
    }

    fun resume() {
        if (isTrailerPlaying) {
            Log.d(TAG, "resume: suppressed because trailer is playing")
            return
        }
        try {
            if (mediaPlayer != null && !mediaPlayer!!.isPlaying) {
                val volume = if (isMuted) 0.0f else DEFAULT_MUSIC_VOLUME
                mediaPlayer?.setVolume(volume, volume)
                mediaPlayer?.start()
                Log.d(TAG, "Resumed MediaPlayer")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error resuming MediaPlayer: ${e.message}", e)
        }
    }

    fun stop() {
        try {
            mediaPlayer?.let { player ->
                player.setOnPreparedListener(null)
                player.setOnCompletionListener(null)
                player.setOnErrorListener(null)
                try {
                    if (player.isPlaying) {
                        player.pause()
                    }
                } catch (_: Exception) {}
                player.reset()
                player.release()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping/releasing MediaPlayer: ${e.message}", e)
        } finally {
            mediaPlayer = null
            currentTrackType = TrackType.NONE
        }
    }

    fun release() {
        stop()
    }
}
