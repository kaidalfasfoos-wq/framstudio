package com.framestudio.app.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.framestudio.app.data.EditorLayer
import com.framestudio.app.imaging.EraserEngine
import com.framestudio.app.imaging.ExportQuality
import com.framestudio.app.imaging.FilterPreset
import com.framestudio.app.imaging.ImageProcessor
import com.framestudio.app.imaging.LayerRenderer
import com.framestudio.app.imaging.SubjectSegmenter
import com.framestudio.app.imaging.TextDetector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

enum class EditorTool { NONE, LAYERS, TEXT, STICKER, ERASER, FILTERS, CROP, CUTOUT }

data class EditorUiState(
    val baseBitmap: Bitmap? = null,
    val layers: List<EditorLayer> = emptyList(),
    val selectedLayerId: String? = null,
    val brightness: Float = 0f,
    val contrast: Float = 0f,
    val saturation: Float = 0f,
    val activeFilterKey: String = "original",
    val eraserRadius: Float = 40f,
    val activeTool: EditorTool = EditorTool.NONE,
    val pendingCropAspect: String? = null,
    val subjectBitmap: Bitmap? = null,
    val backgroundBitmap: Bitmap? = null,
    val isProcessing: Boolean = false,
    val statusMessage: String? = null,
    val canUndo: Boolean = false,
    val canRedo: Boolean = false
)

class EditorViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(EditorUiState())
    val uiState: StateFlow<EditorUiState> = _uiState.asStateFlow()

    private val undoStack = ArrayDeque<Bitmap>()
    private val redoStack = ArrayDeque<Bitmap>()

    fun loadPhoto(uri: Uri) {
        viewModelScope.launch {
            val bmp = ImageProcessor.decodeBitmap(getApplication(), uri, maxDimension = 3000)
                .copy(Bitmap.Config.ARGB_8888, true)
            _uiState.value = EditorUiState(baseBitmap = bmp)
            undoStack.clear()
            redoStack.clear()
        }
    }

    fun setTool(tool: EditorTool) {
        // الخروج من أداة القص بدون تأكيد يلغي أي قص معلّق
        _uiState.value = _uiState.value.copy(activeTool = tool, pendingCropAspect = if (tool == EditorTool.CROP) _uiState.value.pendingCropAspect else null)
    }

    fun addTextLayer(text: String, color: Int = Color.WHITE) {
        val layer = EditorLayer.TextLayer(id = UUID.randomUUID().toString(), text = text, color = color)
        _uiState.value = _uiState.value.copy(layers = _uiState.value.layers + layer, selectedLayerId = layer.id)
    }

    fun addStickerLayer(emoji: String) {
        val layer = EditorLayer.StickerLayer(id = UUID.randomUUID().toString(), emoji = emoji)
        _uiState.value = _uiState.value.copy(layers = _uiState.value.layers + layer, selectedLayerId = layer.id)
    }

    fun updateLayerTransform(id: String, xRatio: Float? = null, yRatio: Float? = null, scale: Float? = null, rotationDeg: Float? = null) {
        _uiState.value = _uiState.value.copy(
            layers = _uiState.value.layers.map {
                if (it.id == id) it.withTransform(
                    xRatio = xRatio ?: it.xRatio,
                    yRatio = yRatio ?: it.yRatio,
                    scale = scale ?: it.scale,
                    rotationDeg = rotationDeg ?: it.rotationDeg
                ) else it
            }
        )
    }

    fun setLayerOpacity(id: String, opacity: Float) {
        _uiState.value = _uiState.value.copy(
            layers = _uiState.value.layers.map { if (it.id == id) it.withTransform(opacity = opacity) else it }
        )
    }

    fun toggleLayerVisibility(id: String) {
        _uiState.value = _uiState.value.copy(
            layers = _uiState.value.layers.map { if (it.id == id) it.withTransform(visible = !it.visible) else it }
        )
    }

    fun deleteLayer(id: String) {
        _uiState.value = _uiState.value.copy(
            layers = _uiState.value.layers.filterNot { it.id == id },
            selectedLayerId = null
        )
    }

    fun moveLayerUp(id: String) {
        val list = _uiState.value.layers.toMutableList()
        val index = list.indexOfFirst { it.id == id }
        if (index in 0 until list.size - 1) {
            val tmp = list[index]; list[index] = list[index + 1]; list[index + 1] = tmp
            _uiState.value = _uiState.value.copy(layers = list)
        }
    }

    fun moveLayerDown(id: String) {
        val list = _uiState.value.layers.toMutableList()
        val index = list.indexOfFirst { it.id == id }
        if (index > 0) {
            val tmp = list[index]; list[index] = list[index - 1]; list[index - 1] = tmp
            _uiState.value = _uiState.value.copy(layers = list)
        }
    }

    fun selectLayer(id: String?) {
        _uiState.value = _uiState.value.copy(selectedLayerId = id)
    }

    fun setEraserRadius(radius: Float) {
        _uiState.value = _uiState.value.copy(eraserRadius = radius)
    }

    fun eraseAt(xPx: Float, yPx: Float, pushUndo: Boolean) {
        val bmp = _uiState.value.baseBitmap ?: return
        if (pushUndo) pushUndoSnapshot(bmp)
        EraserEngine.erase(bmp, xPx, yPx, _uiState.value.eraserRadius)
        _uiState.value = _uiState.value.copy(baseBitmap = bmp)
    }

    fun rotate90() {
        val bmp = _uiState.value.baseBitmap ?: return
        pushUndoSnapshot(bmp)
        _uiState.value = _uiState.value.copy(baseBitmap = ImageProcessor.rotate90(bmp))
    }

    /** يعرض معاينة القص فقط، ما يعدّل الصورة الفعلية إلا بعد تأكيد confirmCrop() */
    fun setPendingCropAspect(aspect: String) {
        _uiState.value = _uiState.value.copy(pendingCropAspect = aspect)
    }

    fun cancelCrop() {
        _uiState.value = _uiState.value.copy(pendingCropAspect = null)
    }

    fun confirmCrop() {
        val bmp = _uiState.value.baseBitmap ?: return
        val aspect = _uiState.value.pendingCropAspect ?: return
        pushUndoSnapshot(bmp)
        _uiState.value = _uiState.value.copy(
            baseBitmap = ImageProcessor.cropToAspect(bmp, aspect),
            pendingCropAspect = null
        )
    }

    private fun pushUndoSnapshot(bmp: Bitmap) {
        if (undoStack.size >= 12) undoStack.removeFirst()
        undoStack.addLast(bmp.copy(Bitmap.Config.ARGB_8888, true))
        redoStack.clear()
        updateUndoRedoFlags()
    }

    fun undo() {
        val current = _uiState.value.baseBitmap ?: return
        if (undoStack.isEmpty()) return
        redoStack.addLast(current.copy(Bitmap.Config.ARGB_8888, true))
        _uiState.value = _uiState.value.copy(baseBitmap = undoStack.removeLast())
        updateUndoRedoFlags()
    }

    fun redo() {
        val current = _uiState.value.baseBitmap ?: return
        if (redoStack.isEmpty()) return
        undoStack.addLast(current.copy(Bitmap.Config.ARGB_8888, true))
        _uiState.value = _uiState.value.copy(baseBitmap = redoStack.removeLast())
        updateUndoRedoFlags()
    }

    private fun updateUndoRedoFlags() {
        _uiState.value = _uiState.value.copy(canUndo = undoStack.isNotEmpty(), canRedo = redoStack.isNotEmpty())
    }

    fun setFilters(brightness: Float, contrast: Float, saturation: Float) {
        _uiState.value = _uiState.value.copy(brightness = brightness, contrast = contrast, saturation = saturation, activeFilterKey = "custom")
    }

    fun applyFilterPreset(preset: FilterPreset) {
        _uiState.value = _uiState.value.copy(
            brightness = preset.brightness,
            contrast = preset.contrast,
            saturation = preset.saturation,
            activeFilterKey = preset.nameKey
        )
    }

    fun runMagicDecompose() {
        val bmp = _uiState.value.baseBitmap ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isProcessing = true, statusMessage = null)
            try {
                val detectedTexts = TextDetector.detect(bmp)
                val newLayers: List<EditorLayer> = detectedTexts.map { d ->
                    EditorLayer.TextLayer(
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
                    layers = _uiState.value.layers + newLayers,
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
        return LayerRenderer.flatten(colorAdjusted, s.layers)
    }

    fun saveExported(bitmap: Bitmap) {
        viewModelScope.launch {
            val quality = ExportQuality.HIGH
            val name = "framestudio_edit_${System.currentTimeMillis()}.${quality.fileExtension}"
            ImageProcessor.saveToGallery(getApplication(), bitmap, name, quality)
            _uiState.value = _uiState.value.copy(statusMessage = "تم الحفظ بالمعرض")
        }
    }
}
