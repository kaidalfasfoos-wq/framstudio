package com.framestudio.app.imaging

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import com.framestudio.app.data.TextLayerData

/** يدمج كل الطبقات (صورة أساسية + نصوص + إطار اختياري) بصورة واحدة نهائية */
object LayerRenderer {
    fun flatten(base: Bitmap, textLayers: List<TextLayerData>, frame: Bitmap? = null): Bitmap {
        val result = base.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(result)

        textLayers.forEach { layer ->
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = layer.color
                textSize = result.width * layer.fontSizeRatio
                textAlign = Paint.Align.CENTER
            }
            val x = layer.xRatio * result.width
            val y = layer.yRatio * result.height
            canvas.save()
            canvas.rotate(layer.rotationDeg, x, y)
            canvas.drawText(layer.text, x, y, paint)
            canvas.restore()
        }

        if (frame != null) {
            val scaledFrame = Bitmap.createScaledBitmap(frame, result.width, result.height, true)
            canvas.drawBitmap(scaledFrame, 0f, 0f, null)
        }
        return result
    }
}
