package com.framestudio.app.imaging

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import com.framestudio.app.data.ActionEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

sealed class BatchStep {
    data class Progress(val done: Int, val total: Int) : BatchStep()
    data class Finished(val savedUris: List<Uri>) : BatchStep()
    data class Error(val message: String) : BatchStep()
}

/**
 * "وضع الأكشن على أكثر من صورة": يطبّق نفس الأكشن (تعديلات + إطار) على قائمة صور
 * دفعة وحدة، ويصدر تقدّم العملية عبر Flow ليعرض شريط التقدم بالواجهة.
 */
object BatchProcessor {

    fun run(
        context: Context,
        photoUris: List<Uri>,
        action: ActionEntity,
        frameFilePath: String?,
        quality: ExportQuality
    ): Flow<BatchStep> = flow {
        val saved = mutableListOf<Uri>()
        val frameBitmap = frameFilePath?.let { path ->
            BitmapFactory.decodeFile(path)
        }

        photoUris.forEachIndexed { index, uri ->
            var bmp = ImageProcessor.decodeBitmap(context, uri, maxDimension = quality.maxDimension)
            bmp = ImageProcessor.cropToAspect(bmp, action.cropAspect)
            bmp = ImageProcessor.adjustColors(bmp, action.brightness, action.contrast, action.saturation)
            if (frameBitmap != null) {
                bmp = ImageProcessor.applyFrame(bmp, frameBitmap)
            }
            if (!action.watermarkText.isNullOrBlank()) {
                bmp = ImageProcessor.addWatermark(bmp, action.watermarkText)
            }
            val name = "framestudio_${System.currentTimeMillis()}_$index.${quality.fileExtension}"
            val outUri = ImageProcessor.saveToGallery(context, bmp, name, quality)
            saved.add(outUri)
            emit(BatchStep.Progress(index + 1, photoUris.size))
        }
        emit(BatchStep.Finished(saved))
    }.flowOn(Dispatchers.IO)
}
