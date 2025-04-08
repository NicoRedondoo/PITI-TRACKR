package com.example.pititrackr

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import kotlin.random.Random

class ConfettiView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val confettiPieces = mutableListOf<ConfettiPiece>()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val colors = listOf(
        Color.YELLOW, Color.GREEN, Color.MAGENTA,
        Color.CYAN, Color.RED, Color.BLUE
    )
    private val shapes = listOf(RectF::class.java, OvalShape::class.java)
    private var animator: ValueAnimator? = null

    init {
        paint.style = Paint.Style.FILL
    }

    fun startConfetti() {
        confettiPieces.clear()
        for (i in 0..100) {
            confettiPieces.add(createConfettiPiece())
        }

        animator?.cancel()
        animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 3000
            interpolator = LinearInterpolator()
            addUpdateListener { animation ->
                updateConfetti(animation.animatedFraction)
                invalidate()
            }
            start()
        }
    }

    private fun createConfettiPiece(): ConfettiPiece {
        return ConfettiPiece(
            x = Random.nextFloat() * width,
            y = -Random.nextFloat() * height / 4f,
            size = 10f + Random.nextFloat() * 20f,
            color = colors.random(),
            shape = shapes.random(),
            speed = 5f + Random.nextFloat() * 15f,
            rotation = Random.nextFloat() * 360f,
            rotationSpeed = -5f + Random.nextFloat() * 10f
        )
    }

    private fun updateConfetti(progress: Float) {
        confettiPieces.forEach { piece ->
            piece.y += piece.speed
            piece.rotation += piece.rotationSpeed
            // Recycle pieces that fall off screen
            if (piece.y > height) {
                piece.y = -piece.size
                piece.x = Random.nextFloat() * width
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        confettiPieces.forEach { piece ->
            paint.color = piece.color
            canvas.save()
            canvas.translate(piece.x, piece.y)
            canvas.rotate(piece.rotation, piece.size / 2, piece.size / 2)

            when (piece.shape) {
                RectF::class.java -> canvas.drawRect(0f, 0f, piece.size, piece.size, paint)
                OvalShape::class.java -> canvas.drawOval(0f, 0f, piece.size, piece.size, paint)
            }

            canvas.restore()
        }
    }

    private data class ConfettiPiece(
        var x: Float,
        var y: Float,
        val size: Float,
        val color: Int,
        val shape: Class<*>,
        val speed: Float,
        var rotation: Float,
        val rotationSpeed: Float
    )

    private class OvalShape
}