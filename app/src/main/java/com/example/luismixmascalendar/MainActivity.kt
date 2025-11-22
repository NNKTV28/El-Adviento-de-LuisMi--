package com.example.luismixmascalendar

import android.annotation.SuppressLint
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
    private var testing = false // Testing mode
    private var testingDay = 25 // Current day for testing
    private val imageViews = mutableListOf<ImageView>()
    @SuppressLint("SetTextI18", "DiscouragedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        db = openOrCreateDatabase("admin.db", MODE_PRIVATE, null)
        db.execSQL("CREATE TABLE IF NOT EXISTS opened_days (day INTEGER PRIMARY KEY)")

        // Setup testing mode UI
        setupTestingMode()

        // Show advent calendar for days 1-24
        for (i in 1..24) {
            val imageViewId = resources.getIdentifier("imageView$i", "id", packageName)
            val imageView = findViewById<ImageView>(imageViewId)
            imageViews.add(imageView)
            setupImageView(imageView, i)
        }
        
        // Setup day 25 special square
        val imageView25 = findViewById<ImageView>(R.id.imageView25)
        setupImageView(imageView25, 25)
    }

    private fun setupTestingMode() {
        val testModeSwitch = findViewById<Switch>(R.id.testModeSwitch)
        val testDayPicker = findViewById<NumberPicker>(R.id.testDayPicker)
        val testingLayout = findViewById<LinearLayout>(R.id.testingLayout)
        
        // Setup number picker
        testDayPicker.minValue = 1
        testDayPicker.maxValue = 25
        testDayPicker.value = testingDay
        testDayPicker.wrapSelectorWheel = false
        
        testDayPicker.setOnValueChangedListener { _, _, newVal ->
            testingDay = newVal
        }
        
        testModeSwitch.setOnCheckedChangeListener { _, isChecked ->
            testing = isChecked
            testingLayout.visibility = if (isChecked) View.VISIBLE else View.GONE
            Toast.makeText(this, if (isChecked) "Modo de prueba activado" else "Modo de prueba desactivado", Toast.LENGTH_SHORT).show()
        }
    }
    @SuppressLint("SetTextI18", "DiscouragedApi")
    private fun setupImageView(imageView: ImageView, day: Int) {
        val imageResId = resources.getIdentifier("day_${day}", "drawable", packageName)
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

            val canOpen = if (testing) {
                day <= testingDay
            } else {
                currentMonth == Calendar.DECEMBER && day <= currentDay
            }

            if (canOpen) {
                openDay(day)
                imageView.foreground = getDrawable(R.drawable.green_border)
            } else {
                Toast.makeText(this@MainActivity, "Oh No! Todavia no puedes abrir este dia!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openDay(day: Int) {
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

    override fun onResume() {
        super.onResume()
        // Music continues playing via service
    }

    override fun onDestroy() {
        super.onDestroy()
        db.close()
    }
}