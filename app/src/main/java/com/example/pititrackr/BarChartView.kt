package com.example.pititrackr

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.View
import java.text.SimpleDateFormat
import java.util.*

class BarChartView(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    private val paint = Paint().apply {
        color = Color.BLUE
        style = Paint.Style.FILL
    }

    private val data = mutableListOf<Pair<String, Int>>()

    fun setData(prefs: Context) {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())

        data.clear()
        for (i in 6 downTo 0) {
            calendar.add(Calendar.DAY_OF_YEAR, -i)
            val date = dateFormat.format(calendar.time)
            val count = prefs.getSharedPreferences("SmokePrefs", Context.MODE_PRIVATE).getInt(date, 0)
            data.add(Pair(dayFormat.format(calendar.time), count))
        }

        // Verifica si los datos están siendo cargados correctamente
        Log.d("BarChartView", "Datos cargados: $data")
        invalidate() // Actualiza la vista para mostrar los datos
    }



    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Definir el color y grosor de las líneas
        val axisPaint = Paint().apply {
            color = Color.BLACK
            style = Paint.Style.STROKE
            strokeWidth = 3f
        }

        val textPaint = Paint().apply {
            color = Color.BLACK
            textSize = 40f
            textAlign = Paint.Align.CENTER
        }

        // Dibujar los ejes X e Y
        canvas.drawLine(0f, height.toFloat(), width.toFloat(), height.toFloat(), axisPaint) // Eje X
        canvas.drawLine(0f, 0f, 0f, height.toFloat(), axisPaint) // Eje Y

        // Dibujar las etiquetas de los días en el eje X
        val daySpacing = width / data.size.toFloat()
        data.forEachIndexed { index, (day, _) ->
            canvas.drawText(
                day,
                index * daySpacing + daySpacing / 2,
                height - 20f, // Ajusta la posición del texto en el eje X
                textPaint
            )
        }

        // Dibuja las barras si los datos están disponibles
        val maxVal = (data.maxOfOrNull { it.second } ?: 1).toFloat()
        val barWidth = width / data.size.toFloat()

        data.forEachIndexed { index, (_, value) ->
            val barHeight = (value / maxVal) * (height - 100) // Restamos 100 para no tocar el eje
            canvas.drawRect(
                index * barWidth, height - barHeight - 60f, // Ajustamos la altura para que no toque el eje X
                (index + 1) * barWidth, height - 60f,
                paint
            )
        }
    }
}
