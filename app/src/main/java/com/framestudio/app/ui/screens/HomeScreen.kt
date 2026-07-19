package com.framestudio.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.DynamicForm
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.framestudio.app.R

@Composable
fun HomeScreen(
    onOpenFrames: () -> Unit,
    onOpenActions: () -> Unit,
    onOpenBatch: () -> Unit,
    onOpenEditor: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp)
    ) {
        Spacer(Modifier.height(24.dp))
        Text(
            stringResource(R.string.home_title),
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Right,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        Text(
            stringResource(R.string.home_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            textAlign = TextAlign.Right,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(32.dp))

        HomeActionCard(
            icon = Icons.Filled.Collections,
            title = stringResource(R.string.card_frames_title),
            desc = stringResource(R.string.card_frames_desc),
            onClick = onOpenFrames
        )
        Spacer(Modifier.height(16.dp))
        HomeActionCard(
            icon = Icons.Filled.DynamicForm,
            title = stringResource(R.string.card_actions_title),
            desc = stringResource(R.string.card_actions_desc),
            onClick = onOpenActions
        )
        Spacer(Modifier.height(16.dp))
        HomeActionCard(
            icon = Icons.Filled.PhotoLibrary,
            title = stringResource(R.string.card_batch_title),
            desc = stringResource(R.string.card_batch_desc),
            onClick = onOpenBatch
        )
        Spacer(Modifier.height(16.dp))
        HomeActionCard(
            icon = Icons.Filled.Brush,
            title = stringResource(R.string.editor_title),
            desc = "أضف نصوص، امسح، وطبّق فلاتر، وفكّك الصورة لطبقات",
            onClick = onOpenEditor
        )
    }
}

@Composable
private fun HomeActionCard(icon: ImageVector, title: String, desc: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Right, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(4.dp))
                Text(
                    desc,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}
