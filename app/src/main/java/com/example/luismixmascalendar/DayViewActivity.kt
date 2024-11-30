package com.example.luismixmascalendar

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.ComponentActivity

class DayViewActivity : ComponentActivity() {
    private val youtubeUrls = mapOf(
        1 to "https://youtu.be/KIiVMXaiWpY?si=y7Wsz-4zcR4u6nUV&t=12",
        2 to "https://youtu.be/RrEyr4otoUU?si=v9QG9qiTbKMUvVgJ&t=10",
        3 to "https://youtu.be/UXklIngt5gs?si=QVugr9pZTWVNaP8K&t=24",
        4 to "https://youtu.be/J0qbS8HeAIQ?si=lrgOXkE_ONKiHIWz",
        5 to "https://youtu.be/9GZL_Un8RI4?si=xYbOzlGeCQXB11bA&t=11",
        6 to "https://youtu.be/ObIH2jBhK6w?si=YhWNE9HcOBUvgyaw&t=12",
        7 to "https://youtu.be/bhjqRmMaI1g?si=zhmlTuEuF7OrNJ3W&t=11",
        8 to "https://youtu.be/Iz-eQlk-vb8?si=BjebgqpWhlnU1Ho5&t=87",
        9 to "https://youtu.be/NK7x-1-MdmE?si=caig7N2YbpfZy3yr&t=14",
        10 to "https://youtu.be/ksoI-1X9sr4?si=LHv-MhScfjVGmeaA&t=73",
        11 to "https://youtu.be/q7F83UQXf-Q?si=BUgvBa8GU8Z2uU8j&t=18",
        12 to "https://youtu.be/jqY7fXl3HBI?si=MirO0la-A4IVF1a3",
        13 to "https://youtu.be/UejnB9xCAAM?si=4sf0nfVo0wb-F-k7",
        14 to "https://youtu.be/TnEpZqlTSgw?si=nlzZpnCKyNkaOY1M&t=12",
        15 to "https://youtu.be/QQG2X9tdVzo?si=xXIbZg3nDt2AxrlG",
        16 to "https://youtu.be/wOjzo02Tmck?si=ou8JwVP4zCwejm_j&t=24",
        17 to "https://youtu.be/mHw71VcYRBQ?si=3UcDqhkKkqrtHAHi&t=13",
        18 to "https://youtu.be/12_eYjSP5G8?si=rgtYpp2tBknwNvy9&t=30",
        19 to "https://youtu.be/UuGrc3vvdMw?si=erOSWyh4DL-gWnYV",
        20 to "https://youtu.be/0Y2hRtUrR-I?si=fMw5bOeWhdkVlIPM",
        21 to "https://youtu.be/64II_Ge_Fgk?si=ggRgExRvA5vhhOhp&t=7",
        22 to "https://youtu.be/cqab6q40Vjw?si=6oYgQje93sQsaYnu&t=3",
        23 to "https://youtu.be/01Nh53Xt1s4?si=y7Ms6ghkpgKkl4FK&t=22",
        24 to "https://youtu.be/5xkJR2pyxTc?si=HtzKKFsp25-xGjEQ&t=16",
        25 to "https://youtu.be/cqab6q40Vjw?si=6oYgQje93sQsaYnu&t=3"
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

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.day_view)

        val day = intent.getIntExtra("day", 1)

        val webView = WebView(this)
        webView.settings.javaScriptEnabled = true
        webView.webViewClient = WebViewClient()

        youtubeUrls[day]?.let { url ->
            val videoId = url.split("youtu.be/")[1].split("?")[0]
            val embedUrl = "https://www.youtube.com/embed/$videoId"
            webView.loadUrl(embedUrl)
        }

        findViewById<Button>(R.id.backButton).setOnClickListener {
            finish()
        }

        val imageView = findViewById<ImageView>(R.id.dayImage)
        val imageResId = resources.getIdentifier("day$day", "drawable", packageName)
        if (imageResId != 0) {
            imageView.setImageResource(imageResId)
        }

        val dayText = findViewById<TextView>(R.id.dayText)
        dayText.text = "Dia $day: ${songTitles[day]}"
    }
}