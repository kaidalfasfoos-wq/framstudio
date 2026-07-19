package com.framestudio.app.imaging

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import com.framestudio.app.data.EditorLayer

/** يدمج الصورة الأساسية مع كل الطبقات (نصوص/ملصقات) المرئية بترتيبها الصحيح، فوق إطار اختياري */
object LayerRenderer {
    fun flatten(base: Bitmap, layers: List<EditorLayer>, frame: Bitmap? = null): Bitmap {
        val result = base.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(result)

        layers.filter { it.visible }.forEach { layer ->
            val alpha = (layer.opacity.coerceIn(0f, 1f) * 255).toInt()
            val x = layer.xRatio * result.width
            val y = layer.yRatio * result.height

            canvas.save()
            canvas.rotate(layer.rotationDeg, x, y)

            when (layer) {
                is EditorLayer.TextLayer -> {
                    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = layer.color
                        this.alpha = alpha
                        textSize = result.width * layer.fontSizeRatio * layer.scale
                        textAlign = Paint.Align.CENTER
                    }
                    canvas.drawText(layer.text, x, y, paint)
                }
                is EditorLayer.StickerLayer -> {
                    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        this.alpha = alpha
                        textSize = result.width * layer.sizeRatio * layer.scale
                        textAlign = Paint.Align.CENTER
                    }
                    val fm = paint.fontMetrics
                    canvas.drawText(layer.emoji, x, y - (fm.ascent + fm.descent) / 2, paint)
                }
            }
            canvas.restore()
        }

        if (frame != null) {
            val scaledFrame = Bitmap.createScaledBitmap(frame, result.width, result.height, true)
            canvas.drawBitmap(scaledFrame, 0f, 0f, null)
        }
        return result
    }
}
