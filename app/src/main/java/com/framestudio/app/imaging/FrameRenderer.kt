package com.framestudio.app.imaging

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF

enum class CornerStyle { NONE, ROUNDED, DECO }

/**
 * يولّد صورة إطار (PNG بخلفية شفافة بالكامل ما عدا الحدود) بناءً على إعدادات المستخدم
 * في شاشة "تصميم إطار جديد". هذا الإطار المُولَّد يُحفظ كـ FrameEntity عادي مثل أي إطار مستورد.
 */
object FrameRenderer {

    fun render(
        size: Int = 1080,
        borderWidthDp: Float,
        borderColor: Int,
        cornerStyle: CornerStyle,
        captionText: String?
    ): Bitmap {
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        val density = size / 360f // تحويل تقريبي إلى بيكسل حسب مقاس القماش
        val strokeWidth = borderWidthDp * density

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = borderColor
            style = Paint.Style.STROKE
            this.strokeWidth = strokeWidth
        }

        val inset = strokeWidth / 2f
        val rect = RectF(inset, inset, size - inset, size - inset)

        when (cornerStyle) {
            CornerStyle.NONE -> canvas.drawRect(rect, paint)
            CornerStyle.ROUNDED -> {
                val radius = size * 0.06f
                canvas.drawRoundRect(rect, radius, radius, paint)
            }
            CornerStyle.DECO -> {
                canvas.drawRect(rect, paint)
                // زوايا زخرفية: خطوط مزدوجة قصيرة داخل كل ركن
                val decoPaint = Paint(paint).apply { this.strokeWidth = strokeWidth * 0.6f }
                val armLen = size * 0.14f
                val off = strokeWidth * 1.8f
                val corners = listOf(
                    Pair(inset + off, inset + off) to true,          // أعلى يسار
                    Pair(size - inset - off, inset + off) to false,  // أعلى يمين
                    Pair(inset + off, size - inset - off) to true,   // أسفل يسار
                    Pair(size - inset - off, size - inset - off) to false // أسفل يمين
                )
                val path = Path()
                for ((point, isLeft) in corners) {
                    val (cx, cy) = point
                    val dirX = if (isLeft) 1 else -1
                    path.moveTo(cx, cy)
                    path.lineTo(cx + dirX * armLen, cy)
                    path.moveTo(cx, cy)
                    path.lineTo(cx, cy + armLen)
                }
                canvas.drawPath(path, decoPaint)
            }
        }

        if (!captionText.isNullOrBlank()) {
            val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = borderColor
                textSize = size * 0.045f
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText(captionText, size / 2f, size - strokeWidth * 2.2f, textPaint)
        }

        return bmp
    }
}
