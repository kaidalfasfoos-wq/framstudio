package com.framestudio.app.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import com.framestudio.app.ui.components.PreviewExportDialog
import kotlinx.coroutines.launch
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.framestudio.app.R
import com.framestudio.app.imaging.ExportQuality
import com.framestudio.app.viewmodel.ActionViewModel
import com.framestudio.app.viewmodel.BatchViewModel

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun BatchProcessScreen(
    batchViewModel: BatchViewModel,
    actionViewModel: ActionViewModel,
    onBack: () -> Unit,
    onFinished: () -> Unit,
    onNeedNewAction: () -> Unit
    previewBitmap?.let { bmp ->
        PreviewExportDialog(
            bitmap = bmp,
            onExport = { previewBitmap = null; batchViewModel.runBatch() },
            onBackToEdit = { previewBitmap = null }
        )
    }
) {
    val state by batchViewModel.uiState.collectAsState()
    val actions by actionViewModel.actions.collectAsState()
    var actionMenuExpanded by remember { mutableStateOf(false) }

    val pickPhotos = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(maxItems = 30)
    ) { uris -> if (uris.isNotEmpty()) batchViewModel.setPhotos(uris) }
    var previewBitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(state.resultUris) {
        if (state.resultUris.isNotEmpty() && !state.isProcessing) onFinished()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.nav_batch)) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, null) } }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .padding(20.dp)
                .fillMaxSize()
        ) {
            OutlinedButton(
                onClick = { pickPhotos.launch(androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.PhotoLibrary, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.batch_select_photos))
            }

            if (state.selectedPhotos.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    stringResource(R.string.batch_selected_count, state.selectedPhotos.size),
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(12.dp))
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    modifier = Modifier.height(200.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(state.selectedPhotos) { uri ->
                        AsyncImage(
                            model = uri,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(10.dp))
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            Text(stringResource(R.string.batch_choose_action), style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            if (actions.isEmpty()) {
                OutlinedButton(onClick = onNeedNewAction, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.new_action))
                }
            } else {
                ExposedDropdownMenuBox(expanded = actionMenuExpanded, onExpandedChange = { actionMenuExpanded = it }) {
                    OutlinedTextField(
                        readOnly = true,
                        value = state.selectedAction?.name ?: "",
                        onValueChange = {},
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = actionMenuExpanded) }
                    )
                    ExposedDropdownMenu(expanded = actionMenuExpanded, onDismissRequest = { actionMenuExpanded = false }) {
                        actions.forEach { a ->
                            DropdownMenuItem(text = { Text(a.name) }, onClick = {
                                batchViewModel.setAction(a); actionMenuExpanded = false
                            })
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            Text(stringResource(R.string.quality_title), style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text(
                stringResource(R.string.quality_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
            Spacer(Modifier.height(8.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = state.quality == ExportQuality.LOW,
                    onClick = { batchViewModel.setQuality(ExportQuality.LOW) },
                    label = { Text(stringResource(R.string.quality_low)) }
                )
                FilterChip(
                    selected = state.quality == ExportQuality.MEDIUM,
                    onClick = { batchViewModel.setQuality(ExportQuality.MEDIUM) },
                    label = { Text(stringResource(R.string.quality_medium)) }
                )
                FilterChip(
                    selected = state.quality == ExportQuality.HIGH,
                    onClick = { batchViewModel.setQuality(ExportQuality.HIGH) },
                    label = { Text(stringResource(R.string.quality_high)) }
                )
                FilterChip(
                    selected = state.quality == ExportQuality.MAX,
                    onClick = { batchViewModel.setQuality(ExportQuality.MAX) },
                    label = { Text(stringResource(R.string.quality_max)) }
                )
            }

            Spacer(Modifier.weight(1f))

            if (state.isProcessing) {
                LinearProgressIndicator(
                    progress = if (state.total > 0) state.done.toFloat() / state.total else 0f,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    stringResource(R.string.batch_processing, state.done, state.total),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            } else {
                OutlinedButton(
                    onClick = { scope.launch { previewBitmap = batchViewModel.generatePreview(context) } },
                    enabled = state.selectedPhotos.isNotEmpty() && state.selectedAction != null,
                    modifier = Modifier.fillMaxWidth()
                ) { Text(stringResource(R.string.preview_before_export)) }
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = { batchViewModel.runBatch() },
                    enabled = state.selectedPhotos.isNotEmpty() && state.selectedAction != null,
                    modifier = Modifier.fillMaxWidth()
                ) { Text(stringResource(R.string.batch_apply)) }
            }
        }
    }
}
