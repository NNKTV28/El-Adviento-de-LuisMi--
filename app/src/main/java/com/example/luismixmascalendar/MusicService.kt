package com.example.luismixmascalendar

import android.app.*
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.media.MediaPlayer
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import java.util.*

class MusicService : Service() {
    private var mediaPlayer: MediaPlayer? = null
    private val binder = MusicBinder()
    private var currentDay = 1
    private lateinit var db: SQLiteDatabase
    private var wakeLock: PowerManager.WakeLock? = null
    
    private val songTitles = mapOf(
        1 to "Va a Nevar",
        2 to "Frente a La Chimenea",
        3 to "Sonríe",
        4 to "Amor A Mares",
        5 to "Te propongo esta noche",
        6 to "Santa Claus Llegó A La Ciudad",
        7 to "Dame",
        8 to "Más",
        9 to "Estaré En Mi Casa Esta Navidad",
        10 to "Suave",
        11 to "Un Hombre Busca a Una Mujer",
        12 to "Cómo Es Posible Que a Mi Lado",
        13 to "Motivos",
        14 to "Mi Humilde Oración",
        15 to "Serenata Huasteca",
        16 to "Amarte es un placer",
        17 to "Amor, Amor, Amor",
        18 to "La Fiesta Del Mariachi",
        19 to "Contigo En La Distancia",
        20 to "Sueña",
        21 to "Llegó La Navidad",
        22 to "Te Deseo Muy Felices Fiestas",
        23 to "Blanca Navidad",
        24 to "Noche De Paz",
        25 to "Navidad, Navidad"
    )
    
    private val videoPaths = mapOf(
        1 to R.raw.day_1,
        2 to R.raw.day_2,
        3 to R.raw.day_3,
        4 to R.raw.day_4,
        5 to R.raw.day_5,
        6 to R.raw.day_6,
        7 to R.raw.day_7,
        8 to R.raw.day_8,
        9 to R.raw.day_9,
        10 to R.raw.day_10,
        11 to R.raw.day_11,
        12 to R.raw.day_12,
        13 to R.raw.day_13,
        14 to R.raw.day_14,
        15 to R.raw.day_15,
        16 to R.raw.day_16,
        17 to R.raw.day_17,
        18 to R.raw.day_18,
        19 to R.raw.day_19,
        20 to R.raw.day_20,
        21 to R.raw.day_21,
        22 to R.raw.day_22,
        23 to R.raw.day_23,
        24 to R.raw.day_24,
        25 to R.raw.day_25
    )

    companion object {
        const val ACTION_PLAY = "com.example.luismixmascalendar.PLAY"
        const val ACTION_PAUSE = "com.example.luismixmascalendar.PAUSE"
        const val ACTION_NEXT = "com.example.luismixmascalendar.NEXT"
        const val ACTION_STOP = "com.example.luismixmascalendar.STOP"
        const val ACTION_DAY_CHANGED = "com.example.luismixmascalendar.DAY_CHANGED"
        const val EXTRA_DAY = "day"
        const val CHANNEL_ID = "music_playback_channel"
        const val NOTIFICATION_ID = 1
    }

    inner class MusicBinder : Binder() {
        fun getService(): MusicService = this@MusicService
    }

    override fun onCreate() {
        super.onCreate()
        db = openOrCreateDatabase("admin.db", MODE_PRIVATE, null)
        db.execSQL("CREATE TABLE IF NOT EXISTS opened_days (day INTEGER PRIMARY KEY)")
        
        // Acquire wake lock to keep playing when screen is off
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "LuismiCalendar::MusicWakeLock"
        )
        wakeLock?.acquire(10*60*1000L /*10 minutes*/)
        
        createNotificationChannel()
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        android.util.Log.d("MusicService", "onStartCommand called with action: ${intent?.action}")
        when (intent?.action) {
            ACTION_PLAY -> {
                android.util.Log.d("MusicService", "ACTION_PLAY received")
                playMusic()
            }
            ACTION_PAUSE -> {
                android.util.Log.d("MusicService", "ACTION_PAUSE received")
                pauseMusic()
            }
            ACTION_NEXT -> {
                android.util.Log.d("MusicService", "ACTION_NEXT received")
                playNextDay()
            }
            ACTION_STOP -> {
                android.util.Log.d("MusicService", "ACTION_STOP received")
                stopMusic()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_STICKY
    }

    fun playDay(day: Int) {
        currentDay = day
        mediaPlayer?.release()
        
        val resourceId = videoPaths[day]
        if (resourceId != null) {
            mediaPlayer = MediaPlayer.create(this, resourceId).apply {
                setOnCompletionListener {
                    // Auto-play next day if available
                    if (currentDay < 25) {
                        playNextDay()
                    }
                }
                start()
            }
            
            // Mark day as opened in database
            val cursor = db.rawQuery("SELECT * FROM opened_days WHERE day = ?", arrayOf(day.toString()))
            if (cursor.count == 0) {
                val values = ContentValues()
                values.put("day", day)
                db.insert("opened_days", null, values)
            }
            cursor.close()
            
            // Broadcast day change
            val intent = Intent(ACTION_DAY_CHANGED)
            intent.putExtra(EXTRA_DAY, day)
            sendBroadcast(intent)
            
            showNotification()
        }
    }

    private fun playMusic() {
        try {
            mediaPlayer?.let {
                if (!it.isPlaying) {
                    it.start()
                }
            }
            showNotification()
        } catch (e: Exception) {
            android.util.Log.e("MusicService", "Error playing music", e)
        }
    }

    private fun pauseMusic() {
        try {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.pause()
                }
            }
            showNotification()
        } catch (e: Exception) {
            android.util.Log.e("MusicService", "Error pausing music", e)
        }
    }

    private fun stopMusic() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    private fun playNextDay() {
        val nextDay = currentDay + 1
        if (nextDay > 25) return
        
        // Check if next day is available
        if (isDayAvailable(nextDay)) {
            playDay(nextDay)
        } else {
            // Show toast that day is not available
            android.widget.Toast.makeText(
                this,
                "El día $nextDay todavía no está disponible!",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun isDayAvailable(day: Int): Boolean {
        // Check if day is already opened
        val cursor = db.rawQuery("SELECT * FROM opened_days WHERE day = ?", arrayOf(day.toString()))
        val isOpened = cursor.count > 0
        cursor.close()
        
        if (isOpened) return true
        
        // Check if it's December and day <= current day
        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
        
        return currentMonth == Calendar.DECEMBER && day <= currentDayOfMonth
    }

    fun isPlaying(): Boolean {
        return mediaPlayer?.isPlaying ?: false
    }

    fun getCurrentDay(): Int = currentDay

    fun getCurrentSongTitle(): String = songTitles[currentDay] ?: "Luis Miguel"

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Reproducción de Música",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Controles de reproducción de música"
                setShowBadge(false)
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun showNotification() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 
            0, 
            intent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val playPauseAction = if (isPlaying()) {
            NotificationCompat.Action(
                android.R.drawable.ic_media_pause,
                "Pausar",
                getPendingIntent(ACTION_PAUSE)
            )
        } else {
            NotificationCompat.Action(
                android.R.drawable.ic_media_play,
                "Reproducir",
                getPendingIntent(ACTION_PLAY)
            )
        }

        val nextAction = NotificationCompat.Action(
            android.R.drawable.ic_media_next,
            "Siguiente",
            getPendingIntent(ACTION_NEXT)
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Día $currentDay - ${getCurrentSongTitle()}")
            .setContentText("Luis Miguel - Calendario de Adviento 2024")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentIntent(pendingIntent)
            .addAction(playPauseAction)
            .addAction(nextAction)
            .setOngoing(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    private fun getPendingIntent(action: String): PendingIntent {
        val intent = Intent(this, MusicService::class.java).apply {
            this.action = action
        }
        return PendingIntent.getService(
            this,
            action.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        stopMusic()
        db.close()
        wakeLock?.release()
    }
}
