package com.example.pititrackr
import android.content.Context
import android.os.Bundle
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var counterTextView: TextView
    private lateinit var smokeButton: ImageButton
    private val prefs by lazy { getSharedPreferences("SmokePrefs", Context.MODE_PRIVATE) }
    private lateinit var touchAnimation: Animation

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        counterTextView = findViewById(R.id.counterTextView)
        smokeButton = findViewById(R.id.smokeButton)

        touchAnimation = AnimationUtils.loadAnimation(this, R.anim.touch_button)

        updateCounter()

        smokeButton.setOnClickListener {
            smokeButton.startAnimation(touchAnimation)
            val today = getCurrentDate()
            val count = prefs.getInt(today, 0) + 1
            prefs.edit().putInt(today, count).apply()
            updateCounter()
        }
    }

    private fun updateCounter() {
        val today = getCurrentDate()
        val count = prefs.getInt(today, 0)
        counterTextView.text = "$count"
    }

    private fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(Date())
    }
}