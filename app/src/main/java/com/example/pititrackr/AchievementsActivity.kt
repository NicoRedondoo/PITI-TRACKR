package com.example.pititrackr

import CigaretteDatabaseHelper
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AchievementsActivity : AppCompatActivity() {

    // Cambiamos el listener para aceptar el parámetro Achievement aunque no lo usemos
    private val achievementListener = { _: Achievement -> refreshAchievements() }
    private lateinit var dbHelper: CigaretteDatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_achievements)

        // Registrar el listener con la nueva firma
        AchievementManager.registerListener(achievementListener)
        refreshAchievements()

        dbHelper = CigaretteDatabaseHelper(this)
        updateCigaretteCount()

        AchievementManager.initAchievements(this)
        val listView = findViewById<ListView>(R.id.achievementList)

        val adapter = AchievementAdapter(this, AchievementManager.getAchievements())
        listView.adapter = adapter

        findViewById<Button>(R.id.btnBack).setOnClickListener { finish() }

        findViewById<ImageButton>(R.id.medalButton).setOnClickListener { finish() }

        findViewById<ImageButton>(R.id.btnHome).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        findViewById<ImageButton>(R.id.statsButton).setOnClickListener {
            startActivity(Intent(this, StatsActivity::class.java))
        }
    }

    private fun updateCigaretteCount() {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val count = dbHelper.getCigaretteCount(today)
        findViewById<TextView>(R.id.counterTextView)?.text = count.toString()
    }

    private fun refreshAchievements() {
        // Actualiza tanto la lista como el contador
        val unlockedCount = AchievementManager.getUnlockedCount()
        findViewById<TextView>(R.id.medalCountTextView)?.text = unlockedCount.toString()

        val adapter = AchievementAdapter(this, AchievementManager.getAchievements())
        findViewById<ListView>(R.id.achievementList).adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        AchievementManager.initAchievements(this)
        val adapter = AchievementAdapter(this, AchievementManager.getAchievements())
        findViewById<ListView>(R.id.achievementList).adapter = adapter

        updateCigaretteCount() // aquí también para que se actualice al volver
    }

    override fun onDestroy() {
        super.onDestroy()
        // No olvides desregistrar el listener cuando la actividad se destruya
        AchievementManager.unregisterListener(achievementListener)
    }
}