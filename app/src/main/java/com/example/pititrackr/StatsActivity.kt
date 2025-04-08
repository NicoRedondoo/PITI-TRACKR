package com.example.pititrackr

import CigaretteDatabaseHelper
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.SimpleDateFormat
import java.util.*

class StatsActivity : AppCompatActivity() {

    private lateinit var barChart: BarChart
    private lateinit var scrollableChart: BarChart
    private lateinit var pieChart: PieChart
    private lateinit var chartScrollView: View
    private lateinit var dbHelper: CigaretteDatabaseHelper
    private lateinit var tvMaxDay: TextView
    private lateinit var tvMinDay: TextView
    private lateinit var counterTextView: TextView
    private lateinit var cbShowAverage: CheckBox
    private lateinit var cbShowEmotions: CheckBox
    private lateinit var tvTopEmotions: TextView

    private var currentRange = 7

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.stats_activity)

        AchievementManager.initAchievements(this)
        AchievementManager.registerListener { updateMedalCount() }

        barChart = findViewById(R.id.barChart)
        scrollableChart = findViewById(R.id.barChartScrollable)
        pieChart = findViewById(R.id.pieChartEmotions)
        chartScrollView = findViewById(R.id.chartScrollView)
        cbShowAverage = findViewById(R.id.cbShowAverage)
        cbShowEmotions = findViewById(R.id.cbShowEmotions)
        tvMaxDay = findViewById(R.id.tvMaxDay)
        tvMinDay = findViewById(R.id.tvMinDay)
        counterTextView = findViewById(R.id.counterTextView)
        tvTopEmotions = findViewById(R.id.tvTopEmotions)
        dbHelper = CigaretteDatabaseHelper(this)

        setupBarChart(barChart)
        setupBarChart(scrollableChart)


        findViewById<ImageButton>(R.id.btnHome).setOnClickListener { finish() }
        findViewById<ImageButton>(R.id.medalButton).setOnClickListener {
            startActivity(Intent(this, AchievementsActivity::class.java))
        }
        findViewById<ImageButton>(R.id.statsButton).setOnClickListener { finish() }
        findViewById<TextView>(R.id.btnBack).setOnClickListener { finish() }

        findViewById<View>(R.id.btn3Days).setOnClickListener { updateChart(3) }
        findViewById<View>(R.id.btn5Days).setOnClickListener { updateChart(5) }
        findViewById<View>(R.id.btn7Days).setOnClickListener { updateChart(7) }
        findViewById<View>(R.id.btn15Days).setOnClickListener { updateChart(15) }
        findViewById<View>(R.id.btn1Month).setOnClickListener { updateChart(30) }

        cbShowAverage.setOnCheckedChangeListener { _, isChecked ->
            val currentChart = if (currentRange >= 15) scrollableChart else barChart
            toggleAverageLine(currentChart, isChecked)
        }

        cbShowEmotions.setOnCheckedChangeListener { _, isChecked ->
            pieChart.visibility = if (isChecked) View.VISIBLE else View.GONE
            chartScrollView.visibility = if (isChecked) View.GONE else if (currentRange >= 15) View.VISIBLE else View.GONE
            barChart.visibility = if (isChecked || currentRange >= 15) View.GONE else View.VISIBLE
            if (isChecked) showEmotionChart(currentRange) else updateChart(currentRange)
        }

        updateMedalCount()
        updateDailyCounter()
        updateChart(currentRange)
    }

    private fun updateMedalCount() {
        findViewById<TextView>(R.id.medalCountTextView).text =
            AchievementManager.getUnlockedCount().toString()
    }

    private fun setupBarChart(chart: BarChart) {
        chart.setBackgroundColor(Color.parseColor("#3A284D"))
        chart.description.isEnabled = false
        chart.legend.isEnabled = false
        chart.setTouchEnabled(true)
        chart.setPinchZoom(true)

        chart.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            textColor = Color.WHITE
            axisLineColor = Color.parseColor("#9575CD")
            gridColor = Color.TRANSPARENT
            granularity = 1f
        }

        chart.axisLeft.apply {
            textColor = Color.WHITE
            axisLineColor = Color.parseColor("#9575CD")
            gridColor = Color.parseColor("#503E6D")
            axisMinimum = 0f
            granularity = 1f
        }

        chart.axisRight.isEnabled = false
    }

    private fun updateDailyCounter() {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val count = dbHelper.getCigaretteCount(today)
        counterTextView.text = count.toString()
    }

    private fun updateChart(days: Int) {
        if (cbShowEmotions.isChecked) {
            currentRange = days
            showEmotionChart(days)
            return
        }
        currentRange = days
        val useScroll = days >= 15
        val currentChart = if (useScroll) scrollableChart else barChart

        barChart.visibility = if (useScroll) View.GONE else View.VISIBLE
        chartScrollView.visibility = if (useScroll) View.VISIBLE else View.GONE

        val entries = dbHelper.getAllEntries()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dayFormat = SimpleDateFormat("EEE dd", Locale.getDefault())
        val calendar = Calendar.getInstance()

        val filteredEntries = (days - 1 downTo 0).map { i ->
            calendar.time = Date()
            calendar.add(Calendar.DAY_OF_YEAR, -i)
            val date = dateFormat.format(calendar.time)
            val label = dayFormat.format(calendar.time)
            val count = entries.find { it.first == date }?.second ?: 0
            label to count
        }

        updateStats(filteredEntries)

        val barEntries = filteredEntries.mapIndexed { index, (_, count) ->
            BarEntry(index.toFloat(), count.toFloat())
        }

        val dataSet = BarDataSet(barEntries, "Cigarros").apply {
            colors = listOf(
                Color.parseColor("#FF9800"), Color.parseColor("#FFAB40"),
                Color.parseColor("#FFD180"), Color.parseColor("#FF8A65"),
                Color.parseColor("#FF7043"), Color.parseColor("#FFA000"),
                Color.parseColor("#FFC107")
            )
            valueTextColor = Color.WHITE
            valueTextSize = if (days > 15) 8f else 10f
            setDrawValues(true)
        }

        currentChart.data = BarData(dataSet).apply {
            barWidth = if (useScroll) 0.4f else 0.6f
        }

        if (useScroll) {
            currentChart.layoutParams.width = days * 70
            currentChart.requestLayout()
        }

        currentChart.xAxis.valueFormatter = IndexAxisValueFormatter(filteredEntries.map { it.first })
        currentChart.xAxis.labelRotationAngle = if (useScroll) -45f else 0f

        currentChart.animateY(800)
        currentChart.invalidate()

        if (cbShowAverage.isChecked) toggleAverageLine(currentChart, true)
    }

    private fun toggleAverageLine(chart: BarChart, show: Boolean) {
        chart.axisLeft.removeAllLimitLines()

        if (show && chart.data != null) {
            val yValues = chart.data.dataSets.flatMap {
                (it as? BarDataSet)?.values?.map { entry -> entry.y } ?: emptyList()
            }

            if (yValues.isNotEmpty()) {
                val average = yValues.average().toFloat()
                val limitLine = LimitLine(average, "Media: ${"%.1f".format(average)}").apply {
                    lineColor = Color.parseColor("#B39DDB")
                    lineWidth = 2f
                    textColor = Color.WHITE
                    textSize = 12f
                }
                chart.axisLeft.addLimitLine(limitLine)
            }
        }

        chart.animateY(800)
        chart.invalidate()
    }

    private fun showEmotionChart(days: Int) {
        val emotionsMap = mutableMapOf<String, Int>()
        val entries = dbHelper.getAllCigarettesByDateRange(days)
        for ((_, emotion) in entries) {
            val key = emotion ?: "Otro"
            emotionsMap[key] = emotionsMap.getOrDefault(key, 0) + 1
        }

        val pieEntries = emotionsMap.map { PieEntry(it.value.toFloat(), it.key) }

        val dataSet = PieDataSet(pieEntries, "Emociones").apply {
            colors = listOf(
                Color.parseColor("#F44336"), Color.parseColor("#FF9800"),
                Color.parseColor("#FFEB3B"), Color.parseColor("#4CAF50"),
                Color.parseColor("#2196F3"), Color.parseColor("#9C27B0"),
                Color.parseColor("#009688"), Color.parseColor("#795548")
            )
            valueTextColor = Color.WHITE
            valueTextSize = 14f
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return value.toInt().toString()
                }
            }
        }

        pieChart.apply {
            data = PieData(dataSet)
            description.isEnabled = false
            legend.isEnabled = true
            setUsePercentValues(false)
            setEntryLabelColor(Color.WHITE)
            setEntryLabelTextSize(12f)
            animateY(1000)
            invalidate()
        }

        val top3 = emotionsMap.entries.sortedByDescending { it.value }.take(3)
        val resumen = top3.joinToString("\n") { "${it.key}: ${it.value}" }
        tvTopEmotions.text = "Top emociones:\n$resumen"
    }

    private fun updateStats(entries: List<Pair<String, Int>>) {
        if (entries.isEmpty()) return

        val maxEntry = entries.maxByOrNull { it.second }!!
        val minEntry = entries.minByOrNull { it.second }!!

        tvMaxDay.text = "Máximo: ${maxEntry.first} (${maxEntry.second})"
        tvMinDay.text = "Mínimo: ${minEntry.first} (${minEntry.second})"
    }

    override fun onResume() {
        super.onResume()
        updateChart(currentRange)
    }

    override fun onDestroy() {
        AchievementManager.unregisterListener { }
        super.onDestroy()
    }
}
