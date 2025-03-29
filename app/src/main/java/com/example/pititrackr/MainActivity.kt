package com.example.pititrackr
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var counterTextView: TextView
    private lateinit var smokeButton: ImageButton
    private val prefs by lazy { getSharedPreferences("SmokePrefs", Context.MODE_PRIVATE) }
    private lateinit var touchAnimation: Animation
    private var canUndoLastSmoke = false // Permite deshacer solo el último cigarro

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

            canUndoLastSmoke = true // Ahora se puede deshacer
        }

        counterTextView.setOnClickListener {
            if (canUndoLastSmoke) {
                Toast.makeText(this, "Mantén pulsado el botón para deshacer", Toast.LENGTH_SHORT).show()
            }
        }

        smokeButton.setOnLongClickListener {
            if (canUndoLastSmoke) {
                undoLastSmoke()
                canUndoLastSmoke = false // Solo se puede deshacer una vez
            } else {
                Toast.makeText(this, "No puedes deshacer ahora", Toast.LENGTH_SHORT).show()
            }
            true
        }
    }

    private fun updateCounter() {
        val today = getCurrentDate()
        val count = prefs.getInt(today, 0)
        counterTextView.text = "$count"
    }

    private fun undoLastSmoke() {
        val today = getCurrentDate()
        val currentCount = prefs.getInt(today, 0)

        if (currentCount > 0) {
            val newCount = currentCount - 1
            prefs.edit().putInt(today, newCount).apply()
            updateCounter()
            Toast.makeText(this, "Cigarro deshecho. Nueva cuenta: $newCount", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(Date())
    }
}