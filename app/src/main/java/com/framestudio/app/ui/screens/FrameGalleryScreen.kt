package com.framestudio.app.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.framestudio.app.R
import com.framestudio.app.data.FrameEntity
import com.framestudio.app.viewmodel.FrameViewModel
import java.io.File

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun FrameGalleryScreen(
    viewModel: FrameViewModel,
    onBack: () -> Unit,
    onDesignNew: () -> Unit
) {
    val frames by viewModel.frames.collectAsState()
    var showAddSheet by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var pendingUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var frameToDelete by remember { mutableStateOf<FrameEntity?>(null) }

    val pickImage = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            pendingUri = uri
            showImportDialog = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.card_frames_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, null) }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddSheet = true }) {
                Icon(Icons.Filled.Add, contentDescription = null)
            }
        }
    ) { padding ->
        if (frames.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(
                    stringResource(R.string.frames_empty),
                    modifier = Modifier.padding(32.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp).let { PaddingValues(16.dp, padding.calculateTopPadding() + 8.dp, 16.dp, 16.dp) },
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(frames, key = { it.id }) { frame ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .aspectRatio(1f)
                            .combinedClickable(
                                onClick = {},
                                onLongClick = { if (!frame.isBuiltIn) frameToDelete = frame }
                            )
                    ) {
                        Box(Modifier.fillMaxSize().background(checkerBrushColor())) {
                            AsyncImage(
                                model = File(frame.filePath),
                                contentDescription = frame.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            Box(
                                Modifier
                                    .align(Alignment.BottomCenter)
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f))
                                    .padding(6.dp)
                            ) {
                                Text(
                                    frame.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddSheet) {
        ModalBottomSheet(onDismissRequest = { showAddSheet = false }) {
            Column(Modifier.padding(24.dp)) {
                ListItem(
                    headlineContent = { Text(stringResource(R.string.import_frame)) },
                    leadingContent = { Icon(Icons.Filled.Upload, null) },
                    modifier = Modifier.combinedClickable(onClick = {
                        showAddSheet = false
                        pickImage.launch("image/png")
                    })
                )
                ListItem(
                    headlineContent = { Text(stringResource(R.string.design_frame)) },
                    leadingContent = { Icon(Icons.Filled.Brush, null) },
                    modifier = Modifier.combinedClickable(onClick = {
                        showAddSheet = false
                        onDesignNew()
                    })
                )
            }
        }
    }

    if (showImportDialog && pendingUri != null) {
        var name by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = { Text(stringResource(R.string.frame_name)) },
            text = {
                OutlinedTextField(
                    value = name, onValueChange = { name = it },
                    singleLine = true, modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.importFrame(pendingUri!!, name.ifBlank { "إطار مستورد" })
                    showImportDialog = false
                }) { Text(stringResource(R.string.save)) }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }) { Text(stringResource(R.string.cancel)) }
            }
        )
    }

    frameToDelete?.let { frame ->
        AlertDialog(
            onDismissRequest = { frameToDelete = null },
            title = { Text(frame.name) },
            text = { Text(stringResource(R.string.delete) + "?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteFrame(frame)
                    frameToDelete = null
                }) { Text(stringResource(R.string.delete)) }
            },
            dismissButton = {
                TextButton(onClick = { frameToDelete = null }) { Text(stringResource(R.string.cancel)) }
            }
        )
    }
}

@Composable
private fun checkerBrushColor() = MaterialTheme.colorScheme.surfaceVariant
