package com.example.luismixmascalendar

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.VideoView
import androidx.activity.ComponentActivity

@Suppress("DEPRECATION")
class DayViewActivity : ComponentActivity() {
    private val videoPaths = mapOf(
        1 to R.raw.video_day_1,
        2 to R.raw.video_day_2,
        3 to R.raw.video_day_3,
        4 to R.raw.video_day_4,
        5 to R.raw.video_day_5,
        6 to R.raw.video_day_6,
        7 to R.raw.video_day_7,
        8 to R.raw.video_day_8,
        9 to R.raw.video_day_9,
        10 to R.raw.video_day_10,
        11 to R.raw.video_day_11,
        12 to R.raw.video_day_12,
        13 to R.raw.video_day_13,
        14 to R.raw.video_day_14,
        15 to R.raw.video_day_15,
        16 to R.raw.video_day_16,
        17 to R.raw.video_day_17,
        18 to R.raw.video_day_18,
        19 to R.raw.video_day_19,
        20 to R.raw.video_day_20,
        21 to R.raw.video_day_21,
        22 to R.raw.video_day_22,
        23 to R.raw.video_day_23,
        24 to R.raw.video_day_24,
        25 to R.raw.video_day_25
    )

    private val songTitles = mapOf(
        1 to "Diciembre está por llegar! Va a nevar!",
        2 to "La ternura que hay en ti, me acariciará",
        3 to "Nunca dejes de soñar...Sonríe!",
        4 to "Yo te necesito!",
        5 to "No me dejes nunca, aunque me hunda",
        6 to "Hoy es San Nicolas! Y ya llegó a la ciudad!",
        7 to "Voy a amarte tanto, como fuego entre tus brazos",
        8 to "Hoy es noche de luna llena...y buen amor!!!",
        9 to "Con la noche buena  \n" +
                "llegará el amor.  \n" +
                "Quiero estar contigo al menos  \n" +
                "con el corazón",
        10 to "Suave! Como siempre te soñé!",
        11 to "Has elegido bien!",
        12 to "Con el color del sol por todo el cuerpo",
        13 to "Mi motivo mejor eres tú",
        14 to "No más guerras No más vidas rotas",
        15 to "Yo por ti me muero",
        16 to "Tú, intensamente tú",
        17 to "Amor! Nació de dios para los dos, nació del alma.",
        18 to "La fiesta va a comenzar!",
        19 to "Doy gracias la cielo por haberte conocido",
        20 to "Sueña  \n" +
                "con un mundo donde todos los días  \n" +
                "el sol brillará",
        21 to "Si todas las cosas traen recuerdos,  será porque llegó la Navidad",
        22 to "Se acerca la noche buena",
        23 to "La nostalgia vuelve al hogar, \n" +
                "al llegar la blanca Navidad",
        24 to "Feliz noche buena!",
        25 to "Día de alegría y felicidad!"
    )

    @SuppressLint("SetJavaScriptEnabled", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.day_view)

        val day = intent.getIntExtra("day", 1)
        val videoView: VideoView = findViewById(R.id.videoView)

        val videoPath = videoPaths[day]
        if (videoPath != null) {
            val uri = "android.resource://${packageName}/${videoPath}"
            videoView.setVideoPath(uri)
            videoView.start()
        }

        findViewById<Button>(R.id.backButton).setOnClickListener {
            finish()
        }

        val dayText: TextView = findViewById(R.id.dayText)
        dayText.text = "Dia $day: ${songTitles[day]}"
    }
}