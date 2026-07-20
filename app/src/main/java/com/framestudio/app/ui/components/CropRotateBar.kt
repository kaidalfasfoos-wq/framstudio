package com.framestudio.app.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.framestudio.app.R

private val aspectOptions = listOf("1:1" to "1:1", "4:5" to "4:5", "16:9" to "16:9", "9:16" to "9:16")

/** شريط أدوات القص: يعرض معاينة أول شي (ما يقص فوراً)، وفيه تأكيد/إلغاء صريحين */
@Composable
fun CropRotateBar(
    pendingAspect: String?,
    onPickAspect: (String) -> Unit,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    onRotate: () -> Unit
) {
    Row(Modifier.padding(16.dp)) {
        OutlinedButton(onClick = onRotate) {
            Icon(Icons.Filled.RotateRight, contentDescription = null)
            Spacer(Modifier.width(6.dp))
            Text(stringResource(R.string.rotate_90))
        }
        Spacer(Modifier.width(12.dp))
        aspectOptions.forEach { (value, label) ->
            FilterChip(
                selected = pendingAspect == value,
                onClick = { onPickAspect(value) },
                label = { Text(label) },
                modifier = Modifier.padding(end = 6.dp)
            )
        }
        if (pendingAspect != null) {
            Spacer(Modifier.width(8.dp))
            Button(onClick = onConfirm) { Icon(Icons.Filled.Check, contentDescription = stringResource(R.string.apply_crop)) }
            Spacer(Modifier.width(6.dp))
            OutlinedButton(onClick = onCancel) { Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.cancel)) }
        }
    }
}
