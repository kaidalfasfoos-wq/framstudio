package com.framestudio.app.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.framestudio.app.R
import com.framestudio.app.viewmodel.EditorTool

/** الشريط السفلي لاختيار أداة التحرير: نص / ممحاة / فلاتر / تفكيك ذكي */
@Composable
fun EditorToolbar(
    activeTool: EditorTool,
    onToolSelected: (EditorTool) -> Unit
) {
    NavigationBar {
        NavigationBarItem(
            selected = activeTool == EditorTool.TEXT,
            onClick = { onToolSelected(EditorTool.TEXT) },
            icon = { Icon(Icons.Filled.TextFields, contentDescription = null) },
            label = { Text(stringResource(R.string.tool_text)) }
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
