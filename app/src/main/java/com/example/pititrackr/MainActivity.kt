import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.pititrackr.R
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var counterTextView: TextView
    private lateinit var smokeButton: Button
    private val prefs by lazy { getSharedPreferences("SmokePrefs", Context.MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        counterTextView = findViewById(R.id.counterTextView)
        smokeButton = findViewById(R.id.smokeButton)

        updateCounter()

        smokeButton.setOnClickListener {
            val today = getCurrentDate()
            val count = prefs.getInt(today, 0) + 1
            prefs.edit().putInt(today, count).apply()
            updateCounter()
        }
    }

    private fun updateCounter() {
        val today = getCurrentDate()
        val count = prefs.getInt(today, 0)
        counterTextView.text = "Cigarros fumados hoy: $count"
    }

    private fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(Date())
    }
}