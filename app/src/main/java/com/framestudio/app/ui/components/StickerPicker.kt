package com.framestudio.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val STICKERS = listOf(
    "😀", "😍", "🔥", "⭐", "❤️", "👍", "🎉", "✨", "💯", "🌟",
    "☀️", "🌙", "🌸", "🍀", "🎂", "📸", "🏆", "💐", "🕊️", "🌈"
)

/** منتقي ملصقات (إيموجي) بسيط — يُضاف كطبقة قابلة للتحريك فوق الصورة */
@Composable
fun StickerPicker(onPick: (String) -> Unit) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(5),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(STICKERS) { emoji ->
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { onPick(emoji) },
                contentAlignment = Alignment.Center
            ) {
                Text(emoji, fontSize = 26.sp)
            }
        }
    }
}
