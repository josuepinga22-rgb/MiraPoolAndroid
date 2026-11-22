package com.example.mirapool

import android.app.Activity
import android.os.Bundle
import android.graphics.*
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Button
import android.graphics.Typeface

class MainActivity : Activity() {
    private lateinit var board: PoolBoard
    private lateinit var status: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.rgb(8, 10, 11))
            setPadding(22, 28, 22, 18)
        }
        val title = TextView(this).apply {
            text = "MIRA POOL"
            textSize = 25f
            setTextColor(Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD
        }
        val subtitle = TextView(this).apply {
            text = "Assistente de trajetória • versão de teste"
            textSize = 13f
            setTextColor(Color.LTGRAY)
            setPadding(0, 5, 0, 16)
        }
        status = TextView(this).apply {
            text = "Toque na mesa para posicionar a bola branca."
            textSize = 14f
            setTextColor(Color.rgb(85, 220, 125))
            setPadding(0, 4, 0, 12)
        }
        board = PoolBoard()
        val reset = Button(this).apply {
            text = "RECOMEÇAR"
            setOnClickListener {
                board.reset()
                status.text = "Toque na mesa para posicionar a bola branca."
            }
        }
        root.addView(title)
        root.addView(subtitle)
        root.addView(status)
        root.addView(board, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f
        ))
        root.addView(reset)
        setContentView(root)
    }

    inner class PoolBoard : View(this@MainActivity) {
        private val felt = Paint(Paint.ANTI_ALIAS_FLAG)
        private val line = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(95, 235, 135); strokeWidth = 4f; style = Paint.Style.STROKE
        }
        private val ballPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        private var cue: PointF? = null
        private var target: PointF? = null

        fun reset() { cue = null; target = null; invalidate() }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            canvas.drawColor(Color.rgb(8, 10, 11))
            // Protege o desenho em telas/medidas ainda pequenas.
            val w = width.toFloat().coerceAtLeast(1f)
            val h = height.toFloat().coerceAtLeast(1f)
            val pad = minOf(18f, w / 4f, h / 4f)
            val l = pad
            val t = minOf(24f, h / 4f)
            val r = (w - pad).coerceAtLeast(l + 1f)
            val b = (h - t).coerceAtLeast(t + 1f)
            felt.color = Color.rgb(104, 66, 39)
            canvas.drawRoundRect(l, t, r, b, 24f, 24f, felt)
            felt.color = Color.rgb(13, 91, 59)
            canvas.drawRoundRect(l + 17, t + 17, r - 17, b - 17, 14f, 14f, felt)

            val pocketR = 15f
            val pockets = listOf(
                PointF(l + 18, t + 18), PointF((l+r)/2, t+12),
                PointF(r-18, t+18), PointF(l+18,b-18),
                PointF((l+r)/2,b-12), PointF(r-18,b-18)
            )
            felt.color = Color.BLACK
            pockets.forEach { canvas.drawCircle(it.x, it.y, pocketR, felt) }

            cue?.let { c ->
                target?.let { q ->
                    canvas.drawLine(c.x, c.y, q.x, q.y, line)
                    val dx = q.x-c.x; val dy=q.y-c.y
                    val len = kotlin.math.sqrt(dx*dx+dy*dy).coerceAtLeast(1f)
                    val ex = q.x + dx/len*34f
                    val ey = q.y + dy/len*34f
                    canvas.drawLine(q.x, q.y, ex, ey, line)
                }
                drawBall(canvas, c, Color.WHITE)
            }
            target?.let { drawBall(canvas, it, Color.rgb(245, 190, 35)) }
            if (cue == null) {
                felt.color = Color.WHITE
                felt.textSize = 17f
                canvas.drawText("1. Toque: bola branca", l+28, b-55, felt)
            } else if (target == null) {
                felt.color = Color.WHITE
                felt.textSize = 17f
                canvas.drawText("2. Toque: bola alvo", l+28, b-55, felt)
            }
        }

        private fun drawBall(canvas: Canvas, p: PointF, color: Int) {
            ballPaint.color = Color.BLACK
            canvas.drawCircle(p.x+2, p.y+3, 13f, ballPaint)
            ballPaint.color = color
            canvas.drawCircle(p.x, p.y, 13f, ballPaint)
            ballPaint.color = Color.argb(90,255,255,255)
            canvas.drawCircle(p.x-4, p.y-5, 4f, ballPaint)
        }

        override fun onTouchEvent(event: MotionEvent): Boolean {
            if (event.action == MotionEvent.ACTION_DOWN) {
                // coerceIn exige que o limite inferior não seja maior que o superior.
                val maxX = (width.toFloat() - 45f).coerceAtLeast(45f)
                val maxY = (height.toFloat() - 50f).coerceAtLeast(50f)
                val minX = if (width >= 90) 45f else 0f
                val minY = if (height >= 100) 50f else 0f
                val p = PointF(
                    event.x.coerceIn(minX, maxX),
                    event.y.coerceIn(minY, maxY)
                )
                if (cue == null || (cue != null && target != null)) {
                    cue = p; target = null
                    status.text = "Agora toque para posicionar a bola-alvo."
                } else {
                    target = p
                    status.text = "Linha calculada. Ajuste os pontos tocando novamente."
                }
                invalidate()
                return true
            }
            return event.action == MotionEvent.ACTION_DOWN ||
                event.action == MotionEvent.ACTION_MOVE ||
                event.action == MotionEvent.ACTION_UP ||
                event.action == MotionEvent.ACTION_CANCEL
        }
    }
}
