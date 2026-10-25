package com.luismi.adviento.data

import com.luismi.adviento.resources.Res
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class Song(
    val day: Int,
    val title: String,
    val album: String,
    val year: Int,
    /** Path inside composeResources, e.g. "files/songs/day01.m4a". */
    val audio: String,
    /** Path inside composeResources, e.g. "files/songs/day01.jpg". */
    val cover: String,
)

object SongCatalog {
    private val json = Json { ignoreUnknownKeys = true }

    /** Reads the bundled songs.json produced by tools/download_songs.py. */
    suspend fun load(): Map<Int, Song> {
        val bytes = Res.readBytes("files/songs/songs.json")
        return json.decodeFromString<List<Song>>(bytes.decodeToString()).associateBy { it.day }
    }

    /** Platform URI of a bundled resource, playable by the native audio APIs. */
    fun uriOf(path: String): String = Res.getUri(path)
}
