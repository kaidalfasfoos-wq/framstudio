package com.framestudio.app.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.framestudio.app.R

private val aspectOptions = listOf("original" to "الأصلية", "1:1" to "1:1", "4:5" to "4:5", "16:9" to "16:9")

/** شريط أدوات القص (بالنسب الجاهزة) والتدوير 90 درجة */
@Composable
fun CropRotateBar(onCrop: (String) -> Unit, onRotate: () -> Unit) {
    Row(Modifier.padding(16.dp)) {
        OutlinedButton(onClick = onRotate) {
            Icon(Icons.Filled.RotateRight, contentDescription = null)
            Spacer(Modifier.width(6.dp))
            Text(stringResource(R.string.rotate_90))
        }
        Spacer(Modifier.width(12.dp))
        aspectOptions.forEach { (value, label) ->
            FilterChip(selected = false, onClick = { onCrop(value) }, label = { Text(label) }, modifier = Modifier.padding(end = 6.dp))
        }
    }
}
