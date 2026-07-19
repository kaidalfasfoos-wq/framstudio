package com.framestudio.app.viewmodel

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.framestudio.app.data.ActionEntity
import com.framestudio.app.data.Repository
import com.framestudio.app.imaging.BatchProcessor
import com.framestudio.app.imaging.BatchStep
import com.framestudio.app.imaging.ExportQuality
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class BatchUiState(
    val selectedPhotos: List<Uri> = emptyList(),
    val selectedAction: ActionEntity? = null,
    val quality: ExportQuality = ExportQuality.HIGH,
    val isProcessing: Boolean = false,
    val done: Int = 0,
    val total: Int = 0,
    val resultUris: List<Uri> = emptyList(),
    val error: String? = null
)

class BatchViewModel(
    application: Application,
    private val repository: Repository
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(BatchUiState())
    val uiState: StateFlow<BatchUiState> = _uiState.asStateFlow()

    fun setPhotos(uris: List<Uri>) {
        _uiState.value = _uiState.value.copy(selectedPhotos = uris, resultUris = emptyList())
    }

    fun setAction(action: ActionEntity) {
        _uiState.value = _uiState.value.copy(selectedAction = action)
    }

    fun setQuality(quality: ExportQuality) {
        _uiState.value = _uiState.value.copy(quality = quality)
    }

    /** يجهّز معاينة سريعة (أول صورة فقط) بعد تطبيق الأكشن المختار، قبل تشغيل معالجة الدفعة الكاملة */
    suspend fun generatePreview(context: Context): Bitmap? {
        val state = _uiState.value
        val action = state.selectedAction ?: return null
        val uri = state.selectedPhotos.firstOrNull() ?: return null
        return withContext(Dispatchers.IO) {
            var bmp = com.framestudio.app.imaging.ImageProcessor.decodeBitmap(context, uri, maxDimension = state.quality.maxDimension)
            bmp = com.framestudio.app.imaging.ImageProcessor.cropToAspect(bmp, action.cropAspect)
            bmp = com.framestudio.app.imaging.ImageProcessor.adjustColors(bmp, action.brightness, action.contrast, action.saturation)
            val frameFilePath = action.frameId?.let { repository.getFrame(it)?.filePath }
            if (frameFilePath != null) {
                val frameBmp = android.graphics.BitmapFactory.decodeFile(frameFilePath)
                bmp = com.framestudio.app.imaging.ImageProcessor.applyFrame(bmp, frameBmp)
            }
            if (!action.watermarkText.isNullOrBlank()) {
                bmp = com.framestudio.app.imaging.ImageProcessor.addWatermark(bmp, action.watermarkText)
            }
            bmp
        }
    }

    fun runBatch() {
        val state = _uiState.value
        val action = state.selectedAction ?: return
        if (state.selectedPhotos.isEmpty()) return

        viewModelScope.launch {
            _uiState.value = state.copy(isProcessing = true, done = 0, total = state.selectedPhotos.size, error = null)
            val frameFilePath = action.frameId?.let { repository.getFrame(it)?.filePath }

            BatchProcessor.run(getApplication(), state.selectedPhotos, action, frameFilePath, state.quality)
                .collect { step ->
                    when (step) {
                        is BatchStep.Progress -> _uiState.value =
                            _uiState.value.copy(done = step.done, total = step.total)
                        is BatchStep.Finished -> _uiState.value =
                            _uiState.value.copy(isProcessing = false, resultUris = step.savedUris)
                        is BatchStep.Error -> _uiState.value =
                            _uiState.value.copy(isProcessing = false, error = step.message)
                    }
                }
        }
    }

    fun reset() {
        _uiState.value = BatchUiState()
    }
}
