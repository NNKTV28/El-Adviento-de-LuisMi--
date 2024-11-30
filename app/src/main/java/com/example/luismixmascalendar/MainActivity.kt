package com.example.luismixmascalendar

import android.annotation.SuppressLint
import android.media.MediaPlayer
import android.os.Bundle
import android.widget.*
import androidx.activity.ComponentActivity
import android.database.sqlite.SQLiteDatabase
import android.content.ContentValues
import android.content.Intent
import android.view.View
import java.util.*

class MainActivity : ComponentActivity() {
    private lateinit var db: SQLiteDatabase
    private var mediaPlayer: MediaPlayer? = null
    private val testing = true // Set to true for testing
    private val testingDay = 1 // Change this value to simulate different days
    private val imageViews = mutableListOf<ImageView>()

    @SuppressLint("SetTextI18", "DiscouragedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        // Initialize and play random background sound
        val soundResources = arrayOf(R.raw.bells_1, R.raw.bells_2)
        val randomSound = soundResources.random()
        mediaPlayer = MediaPlayer.create(this, randomSound)
        mediaPlayer?.isLooping = true
        mediaPlayer?.start()

        db = openOrCreateDatabase("admin.db", MODE_PRIVATE, null)
        db.execSQL("CREATE TABLE IF NOT EXISTS opened_days (day INTEGER PRIMARY KEY)")

        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentDay = if (testing) testingDay else calendar.get(Calendar.DAY_OF_MONTH)

        val christmasImageView = findViewById<ImageView>(R.id.imageView25)

        if (currentMonth == Calendar.DECEMBER && currentDay == 25) {
            // Hide all other images and show Christmas image
            for (i in 1..24) {
                val imageViewId = resources.getIdentifier("imageView$i", "id", packageName)
                val imageView = findViewById<ImageView>(imageViewId)
                imageView.visibility = View.GONE
            }
            christmasImageView.visibility = View.VISIBLE
            christmasImageView.setImageResource(R.drawable.day_25)
            christmasImageView.scaleType = ImageView.ScaleType.CENTER_INSIDE
            christmasImageView.foreground = getDrawable(R.drawable.green_border)
            christmasImageView.setOnClickListener {
                mediaPlayer?.stop()
                openDay(25)
            }
        } else {
            // Show advent calendar
            christmasImageView.visibility = View.GONE
            for (i in 1..24) {
                val imageViewId = resources.getIdentifier("imageView$i", "id", packageName)
                val imageView = findViewById<ImageView>(imageViewId)
                imageViews.add(imageView)
                imageView.visibility = View.VISIBLE
                setupImageView(imageView, i)
            }
        }
    }

    @SuppressLint("SetTextI18", "DiscouragedApi")
    private fun setupImageView(imageView: ImageView, day: Int) {
        val imageResId = resources.getIdentifier("day_${day}", "raw", packageName)
        imageView.setImageResource(imageResId)
        imageView.scaleType = ImageView.ScaleType.CENTER_CROP

        // Check if day is already opened
        val cursor = db.rawQuery("SELECT * FROM opened_days WHERE day = ?", arrayOf(day.toString()))
        if (cursor.count > 0) {
            imageView.foreground = getDrawable(R.drawable.green_border)
        } else {
            imageView.foreground = getDrawable(R.drawable.red_border)
        }
        cursor.close()

        imageView.setOnClickListener {
            val calendar = Calendar.getInstance()
            val currentMonth = calendar.get(Calendar.MONTH)
            val currentDay = if (testing) testingDay else calendar.get(Calendar.DAY_OF_MONTH)

            if (testing || (currentMonth == Calendar.DECEMBER && day <= currentDay)) {
                mediaPlayer?.stop()
                openDay(day)
                imageView.foreground = getDrawable(R.drawable.green_border)
            } else {
                Toast.makeText(this@MainActivity, "Oh No! Todavia no puedes abrir este dia!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openDay(day: Int) {
        // The if statement was redundant, as both branches did the same thing
        val intent = Intent(this, DayViewActivity::class.java)
        intent.putExtra("day", day)
        startActivity(intent)

        val cursor = db.rawQuery("SELECT * FROM opened_days WHERE day = ?", arrayOf(day.toString()))
        if (cursor.count == 0) {
            val values = ContentValues()
            values.put("day", day)
            db.insert("opened_days", null, values)
        }
        cursor.close()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        db.close()
    }
}