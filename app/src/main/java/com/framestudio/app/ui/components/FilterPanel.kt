package com.framestudio.app.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.framestudio.app.R
import com.framestudio.app.viewmodel.EditorUiState

/** لوحة تعديل السطوع/التباين/التشبع — تُعرض بورقة سفلية عند اختيار أداة "فلاتر" بالمحرر */
@Composable
fun FilterPanel(
    state: EditorUiState,
    onChange: (brightness: Float, contrast: Float, saturation: Float) -> Unit
) {
    var brightness by remember(state.brightness) { mutableFloatStateOf(state.brightness) }
    var contrast by remember(state.contrast) { mutableFloatStateOf(state.contrast) }
    var saturation by remember(state.saturation) { mutableFloatStateOf(state.saturation) }

    Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
        Text(stringResource(R.string.brightness), style = MaterialTheme.typography.labelLarge)
        Slider(
            value = brightness,
            onValueChange = { brightness = it; onChange(brightness, contrast, saturation) },
            valueRange = -100f..100f
        )
        Text(stringResource(R.string.contrast), style = MaterialTheme.typography.labelLarge)
        Slider(
            value = contrast,
            onValueChange = { contrast = it; onChange(brightness, contrast, saturation) },
            valueRange = -100f..100f
        )
        Text(stringResource(R.string.saturation), style = MaterialTheme.typography.labelLarge)
        Slider(
            value = saturation,
            onValueChange = { saturation = it; onChange(brightness, contrast, saturation) },
            valueRange = -100f..100f
        )
    }
}
