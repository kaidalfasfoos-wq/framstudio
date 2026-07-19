package com.framestudio.app.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.framestudio.app.data.TextLayerData
import com.framestudio.app.imaging.EraserEngine
import com.framestudio.app.imaging.ImageProcessor
import com.framestudio.app.imaging.LayerRenderer
import com.framestudio.app.imaging.SubjectSegmenter
import com.framestudio.app.imaging.TextDetector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

enum class EditorTool { NONE, TEXT, ERASER, FILTERS, CUTOUT }

data class EditorUiState(
    val baseBitmap: Bitmap? = null,
    val textLayers: List<TextLayerData> = emptyList(),
    val selectedLayerId: String? = null,
    val brightness: Float = 0f,
    val contrast: Float = 0f,
    val saturation: Float = 0f,
    val eraserRadius: Float = 40f,
    val activeTool: EditorTool = EditorTool.NONE,
    val subjectBitmap: Bitmap? = null,
    val backgroundBitmap: Bitmap? = null,
    val isProcessing: Boolean = false,
    val statusMessage: String? = null
)

class EditorViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(EditorUiState())
    val uiState: StateFlow<EditorUiState> = _uiState.asStateFlow()

    private val undoStack = ArrayDeque<Bitmap>()

    fun loadPhoto(uri: Uri) {
        viewModelScope.launch {
            val bmp = ImageProcessor.decodeBitmap(getApplication(), uri, maxDimension = 2200)
                .copy(Bitmap.Config.ARGB_8888, true)
            _uiState.value = EditorUiState(baseBitmap = bmp)
            undoStack.clear()
        }
    }

    fun setTool(tool: EditorTool) {
        _uiState.value = _uiState.value.copy(activeTool = tool)
    }

    fun addTextLayer(text: String) {
        val layer = TextLayerData(
            id = UUID.randomUUID().toString(),
            text = text, xRatio = 0.5f, yRatio = 0.5f, color = Color.WHITE
        )
        _uiState.value = _uiState.value.copy(
            textLayers = _uiState.value.textLayers + layer,
            selectedLayerId = layer.id
        )
    }

    fun updateTextLayer(id: String, xRatio: Float? = null, yRatio: Float? = null) {
        _uiState.value = _uiState.value.copy(
            textLayers = _uiState.value.textLayers.map {
                if (it.id == id) it.copy(xRatio = xRatio ?: it.xRatio, yRatio = yRatio ?: it.yRatio) else it
            }
        )
    }

    fun deleteTextLayer(id: String) {
        _uiState.value = _uiState.value.copy(
            textLayers = _uiState.value.textLayers.filterNot { it.id == id },
            selectedLayerId = null
        )
    }

    fun selectLayer(id: String?) {
        _uiState.value = _uiState.value.copy(selectedLayerId = id)
    }

    fun setEraserRadius(radius: Float) {
        _uiState.value = _uiState.value.copy(eraserRadius = radius)
    }

    fun eraseAt(xPx: Float, yPx: Float, pushUndo: Boolean) {
        val bmp = _uiState.value.baseBitmap ?: return
        if (pushUndo) {
            if (undoStack.size >= 15) undoStack.removeFirst()
            undoStack.addLast(bmp.copy(Bitmap.Config.ARGB_8888, true))
        }
        EraserEngine.erase(bmp, xPx, yPx, _uiState.value.eraserRadius)
        _uiState.value = _uiState.value.copy(baseBitmap = bmp)
    }

    fun undoErase() {
        if (undoStack.isNotEmpty()) {
            _uiState.value = _uiState.value.copy(baseBitmap = undoStack.removeLast())
        }
    }

    fun setFilters(brightness: Float, contrast: Float, saturation: Float) {
        _uiState.value = _uiState.value.copy(brightness = brightness, contrast = contrast, saturation = saturation)
    }

    fun runMagicDecompose() {
        val bmp = _uiState.value.baseBitmap ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isProcessing = true, statusMessage = null)
            try {
                val detectedTexts = TextDetector.detect(bmp)
                val newLayers = detectedTexts.map { d ->
                    TextLayerData(
                        id = UUID.randomUUID().toString(),
                        text = d.text,
                        xRatio = (d.boxRatio.left + d.boxRatio.right) / 2f,
                        yRatio = (d.boxRatio.top + d.boxRatio.bottom) / 2f,
                        fontSizeRatio = (d.boxRatio.bottom - d.boxRatio.top).coerceAtLeast(0.03f),
                        color = Color.WHITE
                    )
                }
                val segResult = try { SubjectSegmenter.segment(bmp) } catch (e: Exception) { null }

                _uiState.value = _uiState.value.copy(
                    textLayers = _uiState.value.textLayers + newLayers,
                    subjectBitmap = segResult?.subjectBitmap,
                    backgroundBitmap = segResult?.backgroundBitmap,
                    isProcessing = false,
                    statusMessage = "تم العثور على ${newLayers.size} نص" +
                        if (segResult != null) " وتم فصل الموضوع عن الخلفية" else ""
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isProcessing = false, statusMessage = "تعذر التحليل: ${e.message}")
            }
        }
    }

    fun exportFinal(): Bitmap? {
        val bmp = _uiState.value.baseBitmap ?: return null
        val s = _uiState.value
        val colorAdjusted = ImageProcessor.adjustColors(bmp, s.brightness, s.contrast, s.saturation)
        return LayerRenderer.flatten(colorAdjusted, s.textLayers)
    }

    fun saveExported(bitmap: Bitmap) {
        viewModelScope.launch {
            val name = "framestudio_edit_${System.currentTimeMillis()}.jpg"
            ImageProcessor.saveToGallery(getApplication(), bitmap, name)
            _uiState.value = _uiState.value.copy(statusMessage = "تم الحفظ بالمعرض")
        }
    }
}
