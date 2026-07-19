package com.framestudio.app.imaging

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.exifinterface.media.ExifInterface
import java.io.OutputStream

/**
 * كل عمليات معالجة الصورة الأساسية: فك التشفير الآمن للذاكرة، تعديل السطوع/التباين/التشبع،
 * القص حسب نسبة معينة، تركيب إطار فوق الصورة، وحفظ الناتج في المعرض.
 */
object ImageProcessor {

    /** يفك تشفير الصورة مع تصغيرها لتفادي OutOfMemory، ويصحح الدوران حسب EXIF */
    fun decodeBitmap(context: Context, uri: Uri, maxDimension: Int = 4500): Bitmap {
        val input = context.contentResolver.openInputStream(uri)
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeStream(input, null, bounds)
        input?.close()

        var sample = 1
        while ((bounds.outWidth / sample) > maxDimension || (bounds.outHeight / sample) > maxDimension) {
            sample *= 2
        }

        val opts = BitmapFactory.Options().apply { inSampleSize = sample }
        val input2 = context.contentResolver.openInputStream(uri)
        var bitmap = BitmapFactory.decodeStream(input2, null, opts)
            ?: throw IllegalStateException("تعذر قراءة الصورة")
        input2?.close()

        // تصحيح الدوران حسب بيانات EXIF
        try {
            val exifStream = context.contentResolver.openInputStream(uri)
            if (exifStream != null) {
                val exif = ExifInterface(exifStream)
                val orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL
                )
                exifStream.close()
                val rotation = when (orientation) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                    ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                    else -> 0f
                }
                if (rotation != 0f) {
                    val matrix = android.graphics.Matrix().apply { postRotate(rotation) }
                    val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
                    if (rotated != bitmap) bitmap = rotated
                }
            }
        } catch (_: Exception) { /* تجاهل مشاكل EXIF غير الحرجة */ }

        return bitmap
    }

    /** يطبّق سطوع/تباين/تشبع عبر ColorMatrix — القيم بمدى -100..100 */
    fun adjustColors(src: Bitmap, brightness: Float, contrast: Float, saturation: Float): Bitmap {
        val result = Bitmap.createBitmap(src.width, src.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        val cm = ColorMatrix()

        // تشبع
        val satMatrix = ColorMatrix()
        satMatrix.setSaturation(1f + saturation / 100f)
        cm.postConcat(satMatrix)

        // تباين وسطوع
        val c = 1f + contrast / 100f
        val b = brightness / 100f * 255f
        val translate = (-0.5f * c + 0.5f) * 255f + b
        val contrastMatrix = ColorMatrix(
            floatArrayOf(
                c, 0f, 0f, 0f, translate,
                0f, c, 0f, 0f, translate,
                0f, 0f, c, 0f, translate,
                0f, 0f, 0f, 1f, 0f
            )
        )
        cm.postConcat(contrastMatrix)

        paint.colorFilter = ColorMatrixColorFilter(cm)
        canvas.drawBitmap(src, 0f, 0f, paint)
        return result
    }

    /** يقص الصورة لنسبة معيّنة من المنتصف. aspect بصيغة "original" أو "1:1" أو "4:5" أو "16:9" */
    fun cropToAspect(src: Bitmap, aspect: String): Bitmap {
        if (aspect == "original") return src
        val parts = aspect.split(":")
        if (parts.size != 2) return src
        val targetRatio = parts[0].toFloat() / parts[1].toFloat()
        val srcRatio = src.width.toFloat() / src.height.toFloat()

        return if (srcRatio > targetRatio) {
            val newWidth = (src.height * targetRatio).toInt().coerceAtMost(src.width)
            val x = (src.width - newWidth) / 2
            Bitmap.createBitmap(src, x, 0, newWidth, src.height)
        } else {
            val newHeight = (src.width / targetRatio).toInt().coerceAtMost(src.height)
            val y = (src.height - newHeight) / 2
            Bitmap.createBitmap(src, 0, y, src.width, newHeight)
        }
    }

    /** يركّب صورة إطار (بخلفية شفافة) فوق الصورة الأساسية بنفس المقاس */
    fun applyFrame(photo: Bitmap, frame: Bitmap): Bitmap {
        val result = Bitmap.createBitmap(photo.width, photo.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        canvas.drawBitmap(photo, 0f, 0f, null)
        val scaledFrame = Bitmap.createScaledBitmap(frame, photo.width, photo.height, true)
        canvas.drawBitmap(scaledFrame, 0f, 0f, null)
        return result
    }

    /** يضيف توقيع نصي بالزاوية السفلية */
    fun addWatermark(src: Bitmap, text: String): Bitmap {
        val result = src.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(result)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.WHITE
            textSize = result.width * 0.035f
            setShadowLayer(6f, 0f, 0f, android.graphics.Color.BLACK)
            textAlign = Paint.Align.RIGHT
        }
        val bounds = Rect()
        paint.getTextBounds(text, 0, text.length, bounds)
        val x = result.width - (result.width * 0.04f)
        val y = result.height - (result.height * 0.04f)
        canvas.drawText(text, x, y, paint)
        return result
    }

    /** يحفظ البيتماب في معرض الصور ضمن ألبوم Frame Studio حسب مستوى الجودة المختار، ويرجع الـ Uri الناتج */
    fun saveToGallery(context: Context, bitmap: Bitmap, displayName: String, quality: ExportQuality): Uri {
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, displayName)
            put(MediaStore.Images.Media.MIME_TYPE, quality.mimeType)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/FrameStudio")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }
        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            ?: throw IllegalStateException("تعذر إنشاء ملف في المعرض")

        var out: OutputStream? = null
        try {
            out = resolver.openOutputStream(uri)
                ?: throw IllegalStateException("تعذر فتح مجرى الكتابة للملف")
            val format = if (quality.losslessPng) Bitmap.CompressFormat.PNG else Bitmap.CompressFormat.JPEG
            bitmap.compress(format, quality.jpegQuality, out)
        } finally {
            out?.close()
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.clear()
            values.put(MediaStore.Images.Media.IS_PENDING, 0)
            resolver.update(uri, values, null, null)
        }
        return uri
    }
}
