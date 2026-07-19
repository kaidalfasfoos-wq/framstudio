package com.framestudio.app.imaging

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode

/** يمسح جزء دائري من البيتماب (يجعله شفافاً بالكامل) بمكان ونصف قطر معيّنين */
object EraserEngine {
    fun erase(bitmap: Bitmap, xPx: Float, yPx: Float, radiusPx: Float) {
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        }
        canvas.drawCircle(xPx, yPx, radiusPx, paint)
    }
}
