package com.framestudio.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.framestudio.app.R
import com.framestudio.app.data.ActionEntity
import com.framestudio.app.data.FrameEntity
import com.framestudio.app.viewmodel.ActionViewModel
import com.framestudio.app.viewmodel.FrameViewModel

private val aspectOptions = listOf("original" to "الأصلية", "1:1" to "1:1", "4:5" to "4:5", "16:9" to "16:9")

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun ActionEditorScreen(
    actionId: Long?,
    actionViewModel: ActionViewModel,
    frameViewModel: FrameViewModel,
    onBack: () -> Unit,
    onSaved: () -> Unit
) {
    val frames by frameViewModel.frames.collectAsState()

    var name by remember { mutableStateOf("") }
    var selectedFrame by remember { mutableStateOf<FrameEntity?>(null) }
    var brightness by remember { mutableStateOf(0f) }
    var contrast by remember { mutableStateOf(0f) }
    var saturation by remember { mutableStateOf(0f) }
    var aspect by remember { mutableStateOf("original") }
    var watermark by remember { mutableStateOf("") }
    var loadedId by remember { mutableStateOf(0L) }

    LaunchedEffect(actionId, frames) {
        if (actionId != null) {
            val existing = actionViewModel.getAction(actionId)
            if (existing != null) {
                name = existing.name
                brightness = existing.brightness
                contrast = existing.contrast
                saturation = existing.saturation
                aspect = existing.cropAspect
                watermark = existing.watermarkText ?: ""
                selectedFrame = frames.find { it.id == existing.frameId }
                loadedId = existing.id
            }
        }
    }

    var frameMenuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (actionId == null) stringResource(R.string.new_action) else name) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, null) } }
            )
        },
        bottomBar = {
            Surface(shadowElevation = 8.dp) {
                Button(
                    onClick = {
                        val entity = ActionEntity(
                            id = loadedId,
                            name = name.ifBlank { "أكشن بدون اسم" },
                            frameId = selectedFrame?.id,
                            brightness = brightness,
                            contrast = contrast,
                            saturation = saturation,
                            cropAspect = aspect,
                            watermarkText = watermark.ifBlank { null }
                        )
                        actionViewModel.saveAction(entity) { onSaved() }
                    },
                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                ) { Text(stringResource(R.string.save)) }
            }
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .padding(20.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollStateCompat())
        ) {
            OutlinedTextField(
                value = name, onValueChange = { name = it },
                label = { Text(stringResource(R.string.action_name)) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(20.dp))
            Text(stringResource(R.string.choose_frame), style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            ExposedDropdownMenuBox(expanded = frameMenuExpanded, onExpandedChange = { frameMenuExpanded = it }) {
                OutlinedTextField(
                    readOnly = true,
                    value = selectedFrame?.name ?: stringResource(R.string.no_frame),
                    onValueChange = {},
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = frameMenuExpanded) }
                )
                ExposedDropdownMenu(expanded = frameMenuExpanded, onDismissRequest = { frameMenuExpanded = false }) {
                    DropdownMenuItem(text = { Text(stringResource(R.string.no_frame)) }, onClick = {
                        selectedFrame = null; frameMenuExpanded = false
                    })
                    frames.forEach { f ->
                        DropdownMenuItem(text = { Text(f.name) }, onClick = {
                            selectedFrame = f; frameMenuExpanded = false
                        })
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
            Text("${stringResource(R.string.brightness)}: ${brightness.toInt()}", style = MaterialTheme.typography.titleMedium)
            Slider(value = brightness, onValueChange = { brightness = it }, valueRange = -100f..100f)

            Text("${stringResource(R.string.contrast)}: ${contrast.toInt()}", style = MaterialTheme.typography.titleMedium)
            Slider(value = contrast, onValueChange = { contrast = it }, valueRange = -100f..100f)

            Text("${stringResource(R.string.saturation)}: ${saturation.toInt()}", style = MaterialTheme.typography.titleMedium)
            Slider(value = saturation, onValueChange = { saturation = it }, valueRange = -100f..100f)

            Spacer(Modifier.height(12.dp))
            Text(stringResource(R.string.aspect_ratio), style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                aspectOptions.forEach { (value, label) ->
                    FilterChip(
                        selected = aspect == value,
                        onClick = { aspect = value },
                        label = { Text(label) }
                    )
                }
            }

            Spacer(Modifier.height(20.dp))
            OutlinedTextField(
                value = watermark, onValueChange = { watermark = it },
                label = { Text(stringResource(R.string.watermark)) },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
private fun rememberScrollStateCompat() = androidx.compose.foundation.rememberScrollState()
