package com.example.pititrackr

import CigaretteDatabaseHelper
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var counterTextView: TextView
    private lateinit var smokeButton: ImageButton
    private lateinit var touchAnimation: Animation
    private lateinit var dbHelper: CigaretteDatabaseHelper
    private lateinit var confettiView: ConfettiView

    private val achievementPopupQueue = mutableListOf<Achievement>()
    private var isShowingPopup = false
    private val popupHandler = Handler(Looper.getMainLooper())

    private val emotionOptions = listOf(
        "Estresado", "Ansioso", "Triste", "Irritado", "Cansado",
        "Aburrido", "Frustrado", "Feliz", "Relajado", "Motivado",
        "Sociable", "Celebrando", "Otro"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        AchievementManager.initAchievements(this)
        AchievementManager.registerListener { achievement ->
            runOnUiThread {
                achievementPopupQueue.add(achievement)
                processAchievementQueue()
            }
        }

        confettiView = findViewById(R.id.confettiView)
        setupUI()
        checkAllAchievements()
        updateMedalCount()
        checkDailyAchievementsOnAppStart()

    }

    private fun setupUI() {
        counterTextView = findViewById(R.id.counterTextView)
        smokeButton = findViewById(R.id.smokeButton)
        touchAnimation = AnimationUtils.loadAnimation(this, R.anim.touch_button)
        dbHelper = CigaretteDatabaseHelper(this)

        smokeButton.setOnClickListener {
            smokeButton.startAnimation(touchAnimation)
            showEmotionDialog()
        }

        findViewById<ImageButton>(R.id.statsButton).setOnClickListener {
            startActivity(Intent(this, StatsActivity::class.java))
        }

        findViewById<Button>(R.id.btnBack).setOnClickListener { finish() }

        findViewById<ImageButton>(R.id.medalButton).setOnClickListener {
            startActivity(Intent(this, AchievementsActivity::class.java))
        }

        smokeButton.setOnLongClickListener {
            val options = arrayOf(
                "Insertar datos aleatorios",
                "Test de logros",
                "Eliminar cigarros de hoy",
                "Eliminar últimos 3 días",
                "Eliminar últimos 5 días",
                "Bloquear todos los logros",
                "Iniciar por primera vez"
            )

            AlertDialog.Builder(this)
                .setTitle("Selecciona una acción de prueba")
                .setItems(options) { _, which ->
                    when (which) {
                        0 -> insertarDatosAleatorios()
                        1 -> toggleTestAchievements()
                        2 -> eliminarCigarrosDeHoy()
                        3 -> eliminarCigarrosUltimos3Dias()
                        4 -> eliminarCigarrosUltimos5Dias()
                        5 -> bloquearTodosLosLogros()
                        6 -> resetDiaDeInicio()
                    }
                }
                .setNegativeButton("Cancelar", null)
                .show()

            true
        }
    }

    private fun checkDailyAchievementsOnAppStart() {
        val prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        val today = getCurrentDate()
        val lastCheckedDay = prefs.getString("lastCheckedDay", null)

        if (lastCheckedDay != today) {
            prefs.edit().putString("lastCheckedDay", today).apply()

            val yesterday = getYesterday()
            val anteayer = getNDaysAgo(2)
            val haceTresDias = getNDaysAgo(3)

            val countAyer = dbHelper.getCigaretteCount(yesterday)
            val countAnteayer = dbHelper.getCigaretteCount(anteayer)
            val countTresDias = dbHelper.getCigaretteCount(haceTresDias)

            // ✅ Día sin fumar (ayer)
            if (countAyer == 0) {
                AchievementManager.unlock(this, "DAY_WITHOUT_SMOKING")
            }

            // ✅ Menos cigarros ayer que anteayer
            if (countAyer < countAnteayer) {
                AchievementManager.unlock(this, "LESS_THAN_YESTERDAY")
            }

            // ✅ 3 días seguidos sin fumar
            if (countAyer == 0 && countAnteayer == 0 && countTresDias == 0) {
                AchievementManager.unlock(this, "THREE_DAYS_NO_SMOKE")
            }
            processAchievementQueue()
            updateCounter()
        }
    }

    private fun getNDaysAgo(n: Int): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DATE, -n)
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
    }


    private fun insertarDatosAleatorios() {
        val emociones = emotionOptions
        val calendar = Calendar.getInstance()
        val random = Random()

        for (i in 0 until 30) { // 30 días atrás
            calendar.time = Date()
            calendar.add(Calendar.DAY_OF_YEAR, -i)
            val fecha = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)

            val cantidad = random.nextInt(6) + 1 // Entre 1 y 6 cigarrillos

            repeat(cantidad) {
                val emocion = emociones[random.nextInt(emociones.size)]
                dbHelper.insertCigarette(fecha, emocion)
            }
        }

        Toast.makeText(this, "Datos aleatorios insertados", Toast.LENGTH_SHORT).show()
        updateCounter()
    }

    private fun resetDiaDeInicio() {
        val prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        prefs.edit().remove("lastCheckedDay").apply()
        Toast.makeText(this, "Reseteado: simulará nuevo día en próximo inicio", Toast.LENGTH_SHORT).show()
    }



    private fun eliminarCigarrosDeHoy() {
        val today = getCurrentDate()
        val db = dbHelper.writableDatabase
        val deleted = db.delete("cigarette_entries", "date = ?", arrayOf(today))
        Toast.makeText(this, "Eliminados $deleted cigarros de hoy", Toast.LENGTH_SHORT).show()
        updateCounter()
    }



    private fun toggleTestAchievements() {
        val lessThanYesterday = "LESS_THAN_YESTERDAY"
        val dayWithoutSmoking = "DAY_WITHOUT_SMOKING"

        val alreadyUnlocked = AchievementManager.isUnlocked(lessThanYesterday) &&
                AchievementManager.isUnlocked(dayWithoutSmoking)

        if (alreadyUnlocked) {
            AchievementManager.lock(this, lessThanYesterday)
            AchievementManager.lock(this, dayWithoutSmoking)
            Toast.makeText(this, "Logros bloqueados", Toast.LENGTH_SHORT).show()
        } else {
            AchievementManager.unlock(this, lessThanYesterday)
            AchievementManager.unlock(this, dayWithoutSmoking)
        }
        updateMedalCount()
    }

    private fun processAchievementQueue() {
        if (achievementPopupQueue.isEmpty() || isShowingPopup) return

        isShowingPopup = true
        val achievement = achievementPopupQueue.removeAt(0)
        showAchievementPopup(achievement)
        updateCounter()
    }

    private fun showAchievementPopup(achievement: Achievement) {
        confettiView.visibility = View.VISIBLE
        confettiView.startConfetti()

        val dialogView = layoutInflater.inflate(R.layout.achievement_item, null)
        dialogView.findViewById<TextView>(R.id.achievementTitle).text = achievement.title
        dialogView.findViewById<TextView>(R.id.achievementDescription).text = achievement.description
        dialogView.findViewById<ImageView>(R.id.achievementIcon).setImageResource(R.drawable.medal2)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()

        popupHandler.postDelayed({
            confettiView.visibility = View.INVISIBLE
            dialog.dismiss()
            isShowingPopup = false
            processAchievementQueue()
        }, 3000)
    }

    private fun updateMedalCount() {
        findViewById<TextView>(R.id.medalCountTextView).text =
            AchievementManager.getUnlockedCount().toString()
    }

    private fun updateCounter() {
        val today = getCurrentDate()
        val count = dbHelper.getCigaretteCount(today)
        counterTextView.text = count.toString()
    }

    private fun insertCigaretteWithEmotion(emotion: String?) {
        val today = getCurrentDate()
        dbHelper.insertCigarette(today, emotion)
        updateCounter()
        Toast.makeText(this, "Cigarro registrado", Toast.LENGTH_SHORT).show()
    }

    private fun showEmotionDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("¿Cómo te sientes?")

        val layout = layoutInflater.inflate(R.layout.dialog_emotion, null)
        val radioGroup = layout.findViewById<RadioGroup>(R.id.radioGroupEmotions)
        val editTextOther = layout.findViewById<EditText>(R.id.editTextOtherEmotion)

        emotionOptions.forEach { emotion ->
            val radioButton = RadioButton(this).apply {
                text = emotion
                textSize = 16f
                setTextColor(resources.getColor(android.R.color.black, null))
            }
            radioGroup.addView(radioButton)
        }

        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            val selected = radioGroup.findViewById<RadioButton>(checkedId).text
            editTextOther.visibility = if (selected == "Otro") View.VISIBLE else View.GONE
        }

        builder.setView(layout)

        builder.setPositiveButton("Confirmar") { _, _ ->
            val selectedId = radioGroup.checkedRadioButtonId
            if (selectedId != -1) {
                val selectedRadio = radioGroup.findViewById<RadioButton>(selectedId)
                val selectedEmotion = if (selectedRadio.text == "Otro") {
                    editTextOther.text.toString().ifBlank { "Otro" }
                } else {
                    selectedRadio.text.toString()
                }
                insertCigaretteWithEmotion(selectedEmotion)
            } else {
                Toast.makeText(this, "Por favor selecciona una emoción", Toast.LENGTH_SHORT).show()
            }
        }

        builder.setNegativeButton("Cancelar", null)
        builder.show()
    }

    private fun getCurrentDate(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    private fun getYesterday(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DATE, -1)
        return sdf.format(calendar.time)
    }

    private fun checkDailyAchievementsOnce() {
        val prefs = getSharedPreferences("Achievements", MODE_PRIVATE)
        val lastCheckedDate = prefs.getString("lastCheckDate", null)
        val today = getCurrentDate()

        if (lastCheckedDate == today) return // Ya fue comprobado hoy

        val yesterday = getYesterday()
        val todayCount = dbHelper.getCigaretteCount(today)
        val yesterdayCount = dbHelper.getCigaretteCount(yesterday)

        // Logros de comparación con ayer
        if (todayCount < yesterdayCount) AchievementManager.unlock(this, "LESS_THAN_YESTERDAY")
        if (yesterdayCount > 0 && todayCount <= yesterdayCount - 5) {
            AchievementManager.unlock(this, "FIVE_LESS_THAN_YESTERDAY")
        }

        // Día sin fumar: si AYER no fumaste
        if (yesterdayCount == 0) {
            AchievementManager.unlock(this, "DAY_WITHOUT_SMOKING")
        }

        // Logros de racha
        var zeroStreak = 0
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val calendar = Calendar.getInstance()

        for (i in 1..7) {
            calendar.time = Date()
            calendar.add(Calendar.DATE, -i)
            val date = dateFormat.format(calendar.time)
            if (dbHelper.getCigaretteCount(date) == 0) {
                zeroStreak++
            } else {
                break
            }
        }

        if (zeroStreak >= 3) AchievementManager.unlock(this, "THREE_DAYS_NO_SMOKE")
        if (zeroStreak >= 7) AchievementManager.unlock(this, "ONE_WEEK_NO_SMOKE")

        prefs.edit().putString("lastCheckDate", today).apply()
    }

    private fun eliminarCigarrosUltimos3Dias() {
        val calendar = Calendar.getInstance()
        val db = dbHelper.writableDatabase
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        var totalDeleted = 0

        for (i in 0 until 3) {
            calendar.time = Date()
            calendar.add(Calendar.DAY_OF_YEAR, -i)
            val date = sdf.format(calendar.time)
            totalDeleted += db.delete("cigarette_entries", "date = ?", arrayOf(date))
        }

        Toast.makeText(this, "Eliminados $totalDeleted cigarros de los últimos 3 días", Toast.LENGTH_SHORT).show()
        updateCounter()
    }


    private fun eliminarCigarrosUltimos5Dias() {
        val calendar = Calendar.getInstance()
        val db = dbHelper.writableDatabase
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        var totalDeleted = 0

        for (i in 0 until 5) {
            calendar.time = Date()
            calendar.add(Calendar.DAY_OF_YEAR, -i)
            val date = sdf.format(calendar.time)
            totalDeleted += db.delete("cigarette_entries", "date = ?", arrayOf(date))
        }

        Toast.makeText(this, "Eliminados $totalDeleted cigarros de los últimos 5 días", Toast.LENGTH_SHORT).show()
        updateCounter()
    }
    private fun bloquearTodosLosLogros() {
        val allAchievementIds = listOf(
            "LESS_THAN_YESTERDAY",
            "FIVE_LESS_THAN_YESTERDAY",
            "DAY_WITHOUT_SMOKING",
            "LESS_3_DAYS_IN_ROW",
            "THREE_DAYS_NO_SMOKE",
            "ONE_WEEK_NO_SMOKE"
            // Añade aquí cualquier otro ID que tengas en tu AchievementManager
        )

        allAchievementIds.forEach { id ->
            AchievementManager.lock(this, id)
        }

        Toast.makeText(this, "Todos los logros han sido bloqueados", Toast.LENGTH_SHORT).show()
        updateMedalCount()
    }

    private fun checkAllAchievements() {
        val today = getCurrentDate()
        val todayCount = dbHelper.getCigaretteCount(today)
        val yesterdayCount = dbHelper.getCigaretteCount(getYesterday())

        // 1. Logros básicos (los que ya tenías)
        if (todayCount < yesterdayCount) {
            AchievementManager.unlock(this, "LESS_THAN_YESTERDAY")
            if (yesterdayCount > 0 && todayCount <= yesterdayCount - 5) {
                AchievementManager.unlock(this, "FIVE_LESS_THAN_YESTERDAY")
            }
        }

        if (todayCount == 0) {
            AchievementManager.unlock(this, "DAY_WITHOUT_SMOKING")
        }

        // 2. Verificar logros semanales
        checkWeeklyAchievements()

        // 3. Verificar logros mensuales
        checkMonthlyAchievements()

        // 4. Verificar rachas
        updateStreaks(todayCount)
    }

    private fun checkWeeklyAchievements() {
        val calendar = Calendar.getInstance()
        val currentWeek = calendar.get(Calendar.WEEK_OF_YEAR)

        calendar.add(Calendar.DATE, -7)
        val lastWeek = calendar.get(Calendar.WEEK_OF_YEAR)

        val allEntries = dbHelper.getAllEntries()

        val currentWeekEntries = allEntries.filter {
            val entryDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(it.first)
            val entryCalendar = Calendar.getInstance().apply { time = entryDate }
            entryCalendar.get(Calendar.WEEK_OF_YEAR) == currentWeek
        }

        val lastWeekEntries = allEntries.filter {
            val entryDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(it.first)
            val entryCalendar = Calendar.getInstance().apply { time = entryDate }
            entryCalendar.get(Calendar.WEEK_OF_YEAR) == lastWeek
        }

        val currentWeekTotal = currentWeekEntries.sumOf { it.second }
        val lastWeekTotal = lastWeekEntries.sumOf { it.second }

        if (currentWeekTotal < lastWeekTotal && lastWeekTotal > 0) {
            AchievementManager.unlock(this, "WEEK_LESS_THAN_LAST")
        }

        if (currentWeekEntries.all { it.second == 0 } && currentWeekEntries.size >= 7) {
            AchievementManager.unlock(this, "ONE_WEEK_NO_SMOKE")
        }
    }

    private fun checkMonthlyAchievements() {
        val allEntries = dbHelper.getAllEntries()
        if (allEntries.size < 30) return

        val last30Days = allEntries.takeLast(30)

        // Mes sin fumar
        if (last30Days.all { it.second == 0 }) {
            AchievementManager.unlock(this, "MONTH_NO_SMOKE")
        }

        // Reducción del 50%
        if (allEntries.size >= 60) {
            val previous30Days = allEntries.takeLast(60).take(30).sumOf { it.second }
            val last30DaysTotal = last30Days.sumOf { it.second }

            if (last30DaysTotal <= previous30Days / 2 && previous30Days > 0) {
                AchievementManager.unlock(this, "HALF_CONSUMPTION")
            }
        }
    }

    private fun updateStreaks(todayCount: Int) {
        val prefs = getSharedPreferences("Achievements", MODE_PRIVATE)
        var lessStreak = prefs.getInt("lessStreak", 0)
        var zeroStreak = prefs.getInt("zeroStreak", 0)

        val yesterdayCount = dbHelper.getCigaretteCount(getYesterday())

        if (todayCount < yesterdayCount) lessStreak++ else lessStreak = 0
        if (todayCount == 0) zeroStreak++ else zeroStreak = 0

        if (lessStreak >= 3) AchievementManager.unlock(this, "LESS_3_DAYS_IN_ROW")
        if (zeroStreak >= 3) AchievementManager.unlock(this, "THREE_DAYS_NO_SMOKE")
        if (zeroStreak >= 7) AchievementManager.unlock(this, "ONE_WEEK_NO_SMOKE")

        prefs.edit()
            .putInt("lessStreak", lessStreak)
            .putInt("zeroStreak", zeroStreak)
            .apply()
    }


    override fun onResume() {
        super.onResume()
        updateCounter()
        updateMedalCount()
        checkAllAchievements() // Actualizado aquí también
    }

    override fun onDestroy() {
        AchievementManager.unregisterListener { achievementPopupQueue.clear() }
        popupHandler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }
}