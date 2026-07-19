package com.framestudio.app.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.framestudio.app.R
import com.framestudio.app.imaging.FilterPreset

/** شريط أفقي للفلاتر الجاهزة بلمسة وحدة (Instagram-style) */
@Composable
fun FilterPresetsBar(activeKey: String, onSelect: (FilterPreset) -> Unit) {
    Row(
        Modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        FilterPreset.PRESETS.forEach { preset ->
            FilterChip(
                selected = activeKey == preset.nameKey,
                onClick = { onSelect(preset) },
                label = { Text(filterLabel(preset.nameKey)) },
                modifier = Modifier.padding(end = 8.dp)
            )
        }
    }
}

@Composable
private fun filterLabel(key: String): String = when (key) {
    "original" -> stringResource(R.string.filter_original)
    "bw" -> stringResource(R.string.filter_bw)
    "vivid" -> stringResource(R.string.filter_vivid)
    "warm" -> stringResource(R.string.filter_warm)
    "cool" -> stringResource(R.string.filter_cool)
    "dramatic" -> stringResource(R.string.filter_dramatic)
    "soft" -> stringResource(R.string.filter_soft)
    else -> key
}
