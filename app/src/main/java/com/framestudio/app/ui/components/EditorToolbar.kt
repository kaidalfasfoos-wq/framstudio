package com.framestudio.app.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.framestudio.app.R
import com.framestudio.app.viewmodel.EditorTool

/** الشريط السفلي لاختيار أداة التحرير */
@Composable
fun EditorToolbar(
    activeTool: EditorTool,
    onToolSelected: (EditorTool) -> Unit
) {
    NavigationBar {
        NavigationBarItem(
            selected = activeTool == EditorTool.LAYERS,
            onClick = { onToolSelected(EditorTool.LAYERS) },
            icon = { Icon(Icons.Filled.Layers, contentDescription = null) },
            label = { Text(stringResource(R.string.tool_layers)) }
        )
        NavigationBarItem(
            selected = activeTool == EditorTool.TEXT,
            onClick = { onToolSelected(EditorTool.TEXT) },
            icon = { Icon(Icons.Filled.TextFields, contentDescription = null) },
            label = { Text(stringResource(R.string.tool_text)) }
        )
        NavigationBarItem(
            selected = activeTool == EditorTool.STICKER,
            onClick = { onToolSelected(EditorTool.STICKER) },
            icon = { Icon(Icons.Filled.EmojiEmotions, contentDescription = null) },
            label = { Text(stringResource(R.string.tool_sticker)) }
        )
        NavigationBarItem(
            selected = activeTool == EditorTool.CROP,
            onClick = { onToolSelected(EditorTool.CROP) },
            icon = { Icon(Icons.Filled.Crop, contentDescription = null) },
            label = { Text(stringResource(R.string.tool_crop)) }
        )
        NavigationBarItem(
            selected = activeTool == EditorTool.ERASER,
            onClick = { onToolSelected(EditorTool.ERASER) },
            icon = { Icon(Icons.Filled.Backspace, contentDescription = null) },
            label = { Text(stringResource(R.string.tool_eraser)) }
        )
        NavigationBarItem(
            selected = activeTool == EditorTool.FILTERS,
            onClick = { onToolSelected(EditorTool.FILTERS) },
            icon = { Icon(Icons.Filled.Tune, contentDescription = null) },
            label = { Text(stringResource(R.string.tool_filters)) }
        )
        NavigationBarItem(
            selected = activeTool == EditorTool.CUTOUT,
            onClick = { onToolSelected(EditorTool.CUTOUT) },
            icon = { Icon(Icons.Filled.AutoFixHigh, contentDescription = null) },
            label = { Text(stringResource(R.string.tool_magic)) }
        )
    }
}
