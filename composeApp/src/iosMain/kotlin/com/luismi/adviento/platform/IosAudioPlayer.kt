package com.luismi.adviento.platform

import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFAudio.AVAudioPlayer
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryPlayback
import platform.AVFAudio.setActive
import platform.Foundation.NSURL

@OptIn(ExperimentalForeignApi::class)
class IosAudioPlayer : AudioPlayer {
    private var player: AVAudioPlayer? = null

    override val isPlaying: Boolean get() = player?.playing == true
    override val positionMs: Long get() = ((player?.currentTime ?: 0.0) * 1000).toLong()
    override val durationMs: Long get() = ((player?.duration ?: 0.0) * 1000).toLong()

    override fun load(uri: String) {
        stop()
        // Playback category: keeps playing with the silent switch on and in the background.
        AVAudioSession.sharedInstance().run {
            setCategory(AVAudioSessionCategoryPlayback, error = null)
            setActive(true, error = null)
        }
        val url = NSURL.URLWithString(uri) ?: return
        player = AVAudioPlayer(contentsOfURL = url, error = null).apply { prepareToPlay() }
    }

    override fun play() {
        player?.play()
    }

    override fun pause() {
        player?.pause()
    }

    override fun seekTo(positionMs: Long) {
        player?.currentTime = positionMs / 1000.0
    }

    override fun stop() {
        player?.stop()
        player = null
    }
}
