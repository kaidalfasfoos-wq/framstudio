package com.framestudio.app.ui.screens

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.framestudio.app.R
import com.framestudio.app.ui.components.EditorCanvas
import com.framestudio.app.ui.components.EditorToolbar
import com.framestudio.app.ui.components.FilterPanel
import com.framestudio.app.ui.components.PreviewExportDialog
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
    var showFilters by remember { mutableStateOf(false) }
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
                EditorToolbar(
                    activeTool = state.activeTool,
                    onToolSelected = { tool ->
                        viewModel.setTool(tool)
                        when (tool) {
                            EditorTool.TEXT -> showTextDialog = true
                            EditorTool.FILTERS -> showFilters = true
                            EditorTool.CUTOUT -> viewModel.runMagicDecompose()
                            else -> {}
                        }
                    }
                )
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
                    textLayers = state.textLayers,
                    activeTool = state.activeTool,
                    eraserRadius = state.eraserRadius,
                    onErase = { x, y, isStart -> viewModel.eraseAt(x, y, isStart) },
                    onTextMoved = { id, xr, yr -> viewModel.updateTextLayer(id, xRatio = xr, yRatio = yr) },
                    onTextTap = { id -> viewModel.selectLayer(id) }
                )
            }

            if (state.isProcessing) {
                Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color.White)
                }
            }
        }
    }

    state.statusMessage?.let { msg ->
        LaunchedEffect(msg) {
            // ممكن تربطها بـ Snackbar لاحقاً — حالياً نص بسيط
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

    if (showFilters) {
        ModalBottomSheet(onDismissRequest = { showFilters = false }) {
            FilterPanel(state = state, onChange = { b, c, s -> viewModel.setFilters(b, c, s) })
        }
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
