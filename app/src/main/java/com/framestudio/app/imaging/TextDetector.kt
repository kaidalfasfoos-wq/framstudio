package com.framestudio.app.imaging

import android.graphics.Bitmap
import android.graphics.RectF
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/** كشف النصوص داخل الصورة تلقائياً (OCR) على الجهاز مباشرة، بدون إنترنت */
object TextDetector {
    data class DetectedText(val text: String, val boxRatio: RectF)

    suspend fun detect(bitmap: Bitmap): List<DetectedText> = suspendCancellableCoroutine { cont ->
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        val image = InputImage.fromBitmap(bitmap, 0)
        recognizer.process(image)
            .addOnSuccessListener { result ->
                val list = result.textBlocks.mapNotNull { block ->
                    val box = block.boundingBox ?: return@mapNotNull null
                    DetectedText(
                        text = block.text,
                        boxRatio = RectF(
                            box.left / bitmap.width.toFloat(),
                            box.top / bitmap.height.toFloat(),
                            box.right / bitmap.width.toFloat(),
                            box.bottom / bitmap.height.toFloat()
                        )
                    )
                }
                cont.resume(list)
            }
            .addOnFailureListener { e -> cont.resumeWithException(e) }
    }
}
