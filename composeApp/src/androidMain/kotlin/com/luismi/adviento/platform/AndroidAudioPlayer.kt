package com.luismi.adviento.platform

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log

class AndroidAudioPlayer(private val context: Context) : AudioPlayer {
    private var player: MediaPlayer? = null

    override val isPlaying: Boolean get() = runCatching { player?.isPlaying == true }.getOrDefault(false)
    override val positionMs: Long get() = runCatching { player?.currentPosition?.toLong() }.getOrNull() ?: 0L
    override val durationMs: Long get() = runCatching { player?.duration?.toLong() }.getOrNull()?.coerceAtLeast(0L) ?: 0L

    override fun load(uri: String) {
        stop()
        player = try {
            MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                setSource(uri)
                prepare()
            }
        } catch (e: Exception) {
            Log.e("AudioPlayer", "Cannot load $uri", e)
            null
        }
    }

    /** Compose resources live in the APK assets; open them by file descriptor (they are stored uncompressed). */
    private fun MediaPlayer.setSource(uri: String) {
        if (uri.startsWith(ASSET_PREFIX)) {
            context.assets.openFd(uri.removePrefix(ASSET_PREFIX)).use { setDataSource(it.fileDescriptor, it.startOffset, it.length) }
        } else {
            setDataSource(context, Uri.parse(uri))
        }
    }

    override fun play() {
        player?.start()
    }

    override fun pause() {
        if (isPlaying) player?.pause()
    }

    override fun seekTo(positionMs: Long) {
        player?.seekTo(positionMs.toInt())
    }

    override fun stop() {
        player?.release()
        player = null
    }

    private companion object {
        const val ASSET_PREFIX = "file:///android_asset/"
    }
}
