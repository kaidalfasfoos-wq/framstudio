package com.framestudio.app.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.framestudio.app.R
import com.framestudio.app.data.EditorLayer

/** لوحة الطبقات: ترتيب فوق/تحت، إخفاء/إظهار، حذف، وشفافية لكل طبقة */
@Composable
fun LayersPanel(
    layers: List<EditorLayer>,
    selectedLayerId: String?,
    onSelect: (String) -> Unit,
    onToggleVisibility: (String) -> Unit,
    onDelete: (String) -> Unit,
    onMoveUp: (String) -> Unit,
    onMoveDown: (String) -> Unit,
    onOpacityChange: (String, Float) -> Unit
) {
    Column(Modifier.padding(16.dp)) {
        Text(stringResource(R.string.tool_layers), style = MaterialTheme.typography.titleMedium)
        if (layers.isEmpty()) {
            Text(
                stringResource(R.string.layers_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                modifier = Modifier.padding(vertical = 16.dp)
            )
        } else {
            LazyColumn {
                items(layers.reversed(), key = { it.id }) { layer ->
                    val label = when (layer) {
                        is EditorLayer.TextLayer -> layer.text
                        is EditorLayer.StickerLayer -> layer.emoji
                    }
                    ListItem(
                        headlineContent = { Text(label, maxLines = 1) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(Modifier),
                        trailingContent = {
                            Row {
                                IconButton(onClick = { onMoveUp(layer.id) }) { Icon(Icons.Filled.ArrowUpward, null) }
                                IconButton(onClick = { onMoveDown(layer.id) }) { Icon(Icons.Filled.ArrowDownward, null) }
                                IconButton(onClick = { onToggleVisibility(layer.id) }) {
                                    Icon(if (layer.visible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff, null)
                                }
                                IconButton(onClick = { onDelete(layer.id) }) { Icon(Icons.Filled.Delete, null) }
                            }
                        }
                    )
                    if (layer.id == selectedLayerId) {
                        Row(Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(stringResource(R.string.layer_opacity), style = MaterialTheme.typography.bodySmall)
                        }
                        Slider(
                            value = layer.opacity,
                            onValueChange = { onOpacityChange(layer.id, it) },
                            valueRange = 0f..1f,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            }
        }
    }
}
