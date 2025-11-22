package com.example.luismixmascalendar

import android.annotation.SuppressLint
import android.content.*
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.widget.*
import androidx.activity.ComponentActivity

@Suppress("DEPRECATION")
class DayViewActivity : ComponentActivity() {
    private var musicService: MusicService? = null
    private var serviceBound = false
    private lateinit var playPauseButton: ImageButton
    private lateinit var dayImageView: ImageView
    private lateinit var dayText: TextView
    private var currentDay = 1
    private val handler = Handler(Looper.getMainLooper())
    private val updateUIRunnable = object : Runnable {
        override fun run() {
            updatePlayPauseButton()
            handler.postDelayed(this, 500) // Update every 500ms
        }
    }
    
    private val dayChangeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == MusicService.ACTION_DAY_CHANGED) {
                val newDay = intent.getIntExtra(MusicService.EXTRA_DAY, currentDay)
                if (newDay != currentDay) {
                    currentDay = newDay
                    updateDayUI()
                }
            }
        }
    }
    
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as MusicService.MusicBinder
            musicService = binder.getService()
            serviceBound = true
            
            // Start playing the day's music
            val day = intent.getIntExtra("day", 1)
            musicService?.playDay(day)
            
            // Start updating UI
            handler.post(updateUIRunnable)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            serviceBound = false
            musicService = null
        }
    }
    
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

    @SuppressLint("SetJavaScriptEnabled", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.day_view)

        // Bind to music service
        val serviceIntent = Intent(this, MusicService::class.java)
        bindService(serviceIntent, serviceConnection, BIND_AUTO_CREATE)

        currentDay = intent.getIntExtra("day", 1)
        
        // Get UI references
        dayImageView = findViewById(R.id.dayImageView)
        dayText = findViewById(R.id.dayText)
        
        // Display initial day
        updateDayUI()
        
        // Register broadcast receiver for day changes
        val filter = IntentFilter(MusicService.ACTION_DAY_CHANGED)
        registerReceiver(dayChangeReceiver, filter)

        findViewById<Button>(R.id.backButton).setOnClickListener {
            // Stop music and service when going back
            musicService?.let {
                val stopIntent = Intent(this, MusicService::class.java)
                stopIntent.action = MusicService.ACTION_STOP
                startService(stopIntent)
            }
            finish()
        }

        // Setup media controls
        setupMediaControls()
    }
    
    private fun updateDayUI() {
        // Update image
        val imageResId = resources.getIdentifier("day_${currentDay}", "drawable", packageName)
        if (imageResId != 0) {
            dayImageView.setImageResource(imageResId)
        }
        
        // Update text
        dayText.text = "Dia $currentDay: ${songTitles[currentDay]}"
    }
    
    private fun setupMediaControls() {
        playPauseButton = findViewById(R.id.playPauseButton)
        val nextButton: ImageButton = findViewById(R.id.nextButton)
        
        playPauseButton.setOnClickListener {
            val service = musicService
            android.util.Log.d("DayViewActivity", "Play/Pause button clicked")
            if (service != null) {
                val intent = Intent(this, MusicService::class.java)
                val isPlaying = service.isPlaying()
                android.util.Log.d("DayViewActivity", "Music is playing: $isPlaying")
                if (isPlaying) {
                    // Pause music
                    intent.action = MusicService.ACTION_PAUSE
                    android.util.Log.d("DayViewActivity", "Paused music")
                } else {
                    // Play music
                    intent.action = MusicService.ACTION_PLAY
                    android.util.Log.d("DayViewActivity", "Playing music")
                }
                startService(intent)
                // Update button after a short delay
                handler.postDelayed({
                    updatePlayPauseButton()
                }, 200)
            } else {
                android.util.Log.e("DayViewActivity", "Service is null!")
            }
        }
        
        nextButton.setOnClickListener {
            if (musicService != null) {
                val intent = Intent(this, MusicService::class.java)
                intent.action = MusicService.ACTION_NEXT
                startService(intent)
            }
        }
    }
    
    private fun updatePlayPauseButton() {
        musicService?.let { service ->
            if (service.isPlaying()) {
                playPauseButton.setImageResource(android.R.drawable.ic_media_pause)
            } else {
                playPauseButton.setImageResource(android.R.drawable.ic_media_play)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(updateUIRunnable)
        unregisterReceiver(dayChangeReceiver)
        if (serviceBound) {
            unbindService(serviceConnection)
            serviceBound = false
        }
    }
}
