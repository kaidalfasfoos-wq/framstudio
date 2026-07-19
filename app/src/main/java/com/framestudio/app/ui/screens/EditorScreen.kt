package com.framestudio.app.ui.screens

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Redo
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.framestudio.app.R
import com.framestudio.app.ui.components.CropRotateBar
import com.framestudio.app.ui.components.EditorCanvas
import com.framestudio.app.ui.components.EditorToolbar
import com.framestudio.app.ui.components.FilterPanel
import com.framestudio.app.ui.components.FilterPresetsBar
import com.framestudio.app.ui.components.LayersPanel
import com.framestudio.app.ui.components.PreviewExportDialog
import com.framestudio.app.ui.components.StickerPicker
import com.framestudio.app.viewmodel.EditorTool
import com.framestudio.app.viewmodel.EditorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    viewModel: EditorViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    var showTextDialog by remember { mutableStateOf(false) }
    var showPreview by remember { mutableStateOf(false) }
    var exportedBitmap by remember { mutableStateOf<Bitmap?>(null) }

    val pickImage = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { viewModel.loadPhoto(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.editor_title)) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, null) } },
                actions = {
                    if (state.baseBitmap != null) {
                        IconButton(onClick = { viewModel.undo() }, enabled = state.canUndo) {
                            Icon(Icons.Filled.Undo, contentDescription = null)
                        }
                        IconButton(onClick = { viewModel.redo() }, enabled = state.canRedo) {
                            Icon(Icons.Filled.Redo, contentDescription = null)
                        }
                        TextButton(onClick = {
                            exportedBitmap = viewModel.exportFinal()
                            showPreview = true
                        }) { Text(stringResource(R.string.preview)) }
                    }
                }
            )
        },
        bottomBar = {
            if (state.baseBitmap != null) {
                Column {
                    when (state.activeTool) {
                        EditorTool.FILTERS -> FilterPresetsBar(activeKey = state.activeFilterKey, onSelect = { viewModel.applyFilterPreset(it) })
                        EditorTool.CROP -> CropRotateBar(onCrop = { viewModel.cropToAspect(it) }, onRotate = { viewModel.rotate90() })
                        else -> {}
                    }
                    EditorToolbar(
                        activeTool = state.activeTool,
                        onToolSelected = { tool ->
                            viewModel.setTool(tool)
                            when (tool) {
                                EditorTool.TEXT -> showTextDialog = true
                                EditorTool.CUTOUT -> viewModel.runMagicDecompose()
                                else -> {}
                            }
                        }
                    )
                }
            }
        }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
            val bmp = state.baseBitmap
            if (bmp == null) {
                Button(onClick = { pickImage.launch("image/*") }) {
                    Text(stringResource(R.string.editor_pick_photo))
                }
            } else {
                EditorCanvas(
                    bitmap = bmp,
                    layers = state.layers,
                    selectedLayerId = state.selectedLayerId,
                    activeTool = state.activeTool,
                    eraserRadius = state.eraserRadius,
                    onErase = { x, y, isStart -> viewModel.eraseAt(x, y, isStart) },
                    onLayerTransform = { id, xr, yr, sc, rot -> viewModel.updateLayerTransform(id, xr, yr, sc, rot) },
                    onLayerTap = { id -> viewModel.selectLayer(id) }
                )
            }

            if (state.isProcessing) {
                Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color.White)
                }
            }
        }
    }

    if (state.activeTool == EditorTool.LAYERS && state.baseBitmap != null) {
        ModalBottomSheet(onDismissRequest = { viewModel.setTool(EditorTool.NONE) }) {
            LayersPanel(
                layers = state.layers,
                selectedLayerId = state.selectedLayerId,
                onSelect = { viewModel.selectLayer(it) },
                onToggleVisibility = { viewModel.toggleLayerVisibility(it) },
                onDelete = { viewModel.deleteLayer(it) },
                onMoveUp = { viewModel.moveLayerUp(it) },
                onMoveDown = { viewModel.moveLayerDown(it) },
                onOpacityChange = { id, o -> viewModel.setLayerOpacity(id, o) }
            )
        }
    }

    if (state.activeTool == EditorTool.STICKER && state.baseBitmap != null) {
        ModalBottomSheet(onDismissRequest = { viewModel.setTool(EditorTool.NONE) }) {
            StickerPicker(onPick = { emoji ->
                viewModel.addStickerLayer(emoji)
                viewModel.setTool(EditorTool.NONE)
            })
        }
    }

    if (showTextDialog) {
        var textValue by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showTextDialog = false },
            title = { Text(stringResource(R.string.add_text)) },
            text = { OutlinedTextField(value = textValue, onValueChange = { textValue = it }, modifier = Modifier.fillMaxWidth()) },
            confirmButton = {
                TextButton(onClick = {
                    if (textValue.isNotBlank()) viewModel.addTextLayer(textValue)
                    showTextDialog = false
                }) { Text(stringResource(R.string.save)) }
            },
            dismissButton = { TextButton(onClick = { showTextDialog = false }) { Text(stringResource(R.string.cancel)) } }
        )
    }

    if (showPreview && exportedBitmap != null) {
        PreviewExportDialog(
            bitmap = exportedBitmap!!,
            onExport = {
                viewModel.saveExported(exportedBitmap!!)
                showPreview = false
            },
            onBackToEdit = { showPreview = false }
        )
    }
}
