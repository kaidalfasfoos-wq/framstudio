package com.framestudio.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.framestudio.app.R
import com.framestudio.app.imaging.CornerStyle
import com.framestudio.app.imaging.FrameRenderer
import com.framestudio.app.viewmodel.FrameViewModel

private val presetColors = listOf(
    Color(0xFF5B4FE9), Color(0xFF000000), Color(0xFFFFFFFF),
    Color(0xFFFF7A59), Color(0xFFD4AF37), Color(0xFF2E7D32)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FrameDesignerScreen(
    viewModel: FrameViewModel,
    onBack: () -> Unit,
    onSaved: () -> Unit
) {
    var borderWidth by remember { mutableStateOf(14f) }
    var borderColor by remember { mutableStateOf(presetColors[0]) }
    var cornerStyle by remember { mutableStateOf(CornerStyle.ROUNDED) }
    var caption by remember { mutableStateOf("") }
    var frameName by remember { mutableStateOf("") }
    var showSaveDialog by remember { mutableStateOf(false) }

    val previewBitmap = remember(borderWidth, borderColor, cornerStyle, caption) {
        FrameRenderer.render(
            size = 600,
            borderWidthDp = borderWidth,
            borderColor = borderColor.toArgb(),
            cornerStyle = cornerStyle,
            captionText = caption
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.designer_title)) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, null) } }
            )
        },
        bottomBar = {
            Surface(shadowElevation = 8.dp) {
                Button(
                    onClick = { showSaveDialog = true },
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
        ) {
            // معاينة حية
            Box(
                Modifier
                    .align(Alignment.CenterHorizontally)
                    .size(220.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        androidx.compose.ui.graphics.Brush.linearGradient(
                            listOf(Color(0xFFEDEBFB), Color(0xFFDCD8F7))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    bitmap = previewBitmap.asImageBitmap(),
                    contentDescription = stringResource(R.string.preview),
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(Modifier.height(24.dp))

            Text(stringResource(R.string.border_width), style = MaterialTheme.typography.titleMedium)
            Slider(value = borderWidth, onValueChange = { borderWidth = it }, valueRange = 2f..40f)

            Spacer(Modifier.height(12.dp))
            Text(stringResource(R.string.border_color), style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                presetColors.forEach { color ->
                    val selected = color == borderColor
                    Box(
                        Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(50))
                            .background(color)
                            .border(
                                width = if (selected) 3.dp else 0.dp,
                                color = MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(50)
                            )
                            .clickable { borderColor = color }
                    )
                }
            }

            Spacer(Modifier.height(20.dp))
            Text(stringResource(R.string.corner_style), style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            SingleChoiceSegmented(
                options = listOf(
                    CornerStyle.NONE to stringResource(R.string.corner_none),
                    CornerStyle.ROUNDED to stringResource(R.string.corner_rounded),
                    CornerStyle.DECO to stringResource(R.string.corner_deco)
                ),
                selected = cornerStyle,
                onSelect = { cornerStyle = it }
            )

            Spacer(Modifier.height(20.dp))
            OutlinedTextField(
                value = caption,
                onValueChange = { caption = it },
                label = { Text(stringResource(R.string.caption_text)) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = { Text(stringResource(R.string.frame_name)) },
            text = {
                OutlinedTextField(
                    value = frameName,
                    onValueChange = { frameName = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val finalBitmap = FrameRenderer.render(
                        size = 1080,
                        borderWidthDp = borderWidth,
                        borderColor = borderColor.toArgb(),
                        cornerStyle = cornerStyle,
                        captionText = caption
                    )
                    viewModel.saveDesignedFrame(frameName.ifBlank { "إطار مخصص" }, finalBitmap)
                    showSaveDialog = false
                    onSaved()
                }) { Text(stringResource(R.string.save)) }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) { Text(stringResource(R.string.cancel)) }
            }
        )
    }
}

@Composable
private fun SingleChoiceSegmented(
    options: List<Pair<CornerStyle, String>>,
    selected: CornerStyle,
    onSelect: (CornerStyle) -> Unit
) {
    Row(Modifier.fillMaxWidth()) {
        options.forEachIndexed { index, (style, label) ->
            val isSelected = style == selected
            OutlinedButton(
                onClick = { onSelect(style) },
                modifier = Modifier.weight(1f).padding(horizontal = 2.dp),
                colors = if (isSelected) ButtonDefaults.outlinedButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                ) else ButtonDefaults.outlinedButtonColors()
            ) { Text(label, style = MaterialTheme.typography.bodyMedium) }
        }
    }
}
