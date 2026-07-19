package com.framestudio.app.imaging

import android.graphics.Bitmap
import android.graphics.Color
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.segmentation.Segmentation
import com.google.mlkit.vision.segmentation.SegmentationMask
import com.google.mlkit.vision.segmentation.selfie.SelfieSegmenterOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * فصل الشخص (الموضوع الرئيسي) عن الخلفية على الجهاز مباشرة عبر ML Kit — بدون إنترنت وبدون مفتاح API.
 * يعمل بشكل أفضل مع الصور اللي فيها أشخاص. إعادة بناء الخلفية بالكامل بالذكاء التوليدي (بعد إزالة
 * الشخص) خطوة مستقبلية تحتاج Gemini API — مو مبنية بهاد الجزء.
 */
object SubjectSegmenter {

    data class Result(val subjectBitmap: Bitmap, val backgroundBitmap: Bitmap)

    suspend fun segment(source: Bitmap): Result = suspendCancellableCoroutine { cont ->
        val options = SelfieSegmenterOptions.Builder()
            .setDetectorMode(SelfieSegmenterOptions.SINGLE_IMAGE_MODE)
            .enableRawSizeMask()
            .build()
        val segmenter = Segmentation.getClient(options)
        val input = InputImage.fromBitmap(source, 0)

        segmenter.process(input)
            .addOnSuccessListener { mask: SegmentationMask ->
                try {
                    val subject = applyMask(source, mask, keepForeground = true)
                    val background = applyMask(source, mask, keepForeground = false)
                    cont.resume(Result(subject, background))
                } catch (e: Exception) {
                    cont.resumeWithException(e)
                }
            }
            .addOnFailureListener { e -> cont.resumeWithException(e) }
    }

    private fun applyMask(source: Bitmap, mask: SegmentationMask, keepForeground: Boolean): Bitmap {
        val maskWidth = mask.width
        val maskHeight = mask.height
        val buffer = mask.buffer

        val scaled = Bitmap.createScaledBitmap(source, maskWidth, maskHeight, true)
        val pixels = IntArray(maskWidth * maskHeight)
        scaled.getPixels(pixels, 0, maskWidth, 0, 0, maskWidth, maskHeight)

        buffer.rewind()
        for (i in 0 until maskWidth * maskHeight) {
            val confidence = buffer.get() // احتمالية أن البكسل جزء من "الشخص" (0..1)
            val isForeground = confidence > 0.5f
            val shouldShow = if (keepForeground) isForeground else !isForeground
            if (!shouldShow) pixels[i] = Color.TRANSPARENT
        }

        val result = Bitmap.createBitmap(maskWidth, maskHeight, Bitmap.Config.ARGB_8888)
        result.setPixels(pixels, 0, maskWidth, 0, 0, maskWidth, maskHeight)
        return result
    }
}
