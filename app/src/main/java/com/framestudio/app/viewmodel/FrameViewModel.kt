package com.framestudio.app.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.framestudio.app.data.FrameEntity
import com.framestudio.app.data.Repository
import com.framestudio.app.imaging.ImageProcessor
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class FrameViewModel(
    application: Application,
    private val repository: Repository
) : AndroidViewModel(application) {

    val frames: StateFlow<List<FrameEntity>> = repository.frames
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private fun framesDir(): File {
        val dir = File(getApplication<Application>().filesDir, "frames")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    /** استيراد صورة إطار PNG جاهزة من المعرض */
    fun importFrame(uri: Uri, name: String) {
        viewModelScope.launch {
            val context = getApplication<Application>()
            val bitmap = ImageProcessor.decodeBitmap(context, uri, maxDimension = 1600)
            val file = File(framesDir(), "frame_${System.currentTimeMillis()}.png")
            FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
            repository.addFrame(FrameEntity(name = name, filePath = file.absolutePath))
        }
    }

    /** حفظ إطار مصمّم داخل التطبيق (من FrameDesignerScreen) */
    fun saveDesignedFrame(name: String, bitmap: Bitmap) {
        viewModelScope.launch {
            val file = File(framesDir(), "frame_${System.currentTimeMillis()}.png")
            FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
            repository.addFrame(FrameEntity(name = name, filePath = file.absolutePath))
        }
    }

    fun deleteFrame(frame: FrameEntity) {
        viewModelScope.launch {
            repository.deleteFrame(frame)
            File(frame.filePath).delete()
        }
    }
}
