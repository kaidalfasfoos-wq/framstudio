package com.framestudio.app.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import com.framestudio.app.data.TextLayerData
import com.framestudio.app.viewmodel.EditorTool
import kotlin.math.roundToInt

/**
 * لوحة عرض الصورة داخل المحرر: تعرض البيتماب، طبقات النص القابلة للسحب،
 * وتلتقط لمسات أداة الممحاة وتحوّلها لإحداثيات بكسل بالبيتماب الأصلي (وليس بأبعاد الشاشة).
 */
@Composable
fun EditorCanvas(
    bitmap: Bitmap,
    textLayers: List<TextLayerData>,
    activeTool: EditorTool,
    eraserRadius: Float,
    onErase: (xPx: Float, yPx: Float, isStart: Boolean) -> Unit,
    onTextMoved: (id: String, xRatio: Float, yRatio: Float) -> Unit,
    onTextTap: (id: String) -> Unit
) {
    // عدّاد بسيط نغيّره بعد كل ضربة ممحاة لإجبار إعادة رسم الصورة، لأن EraserEngine
    // يعدّل بكسلات البيتماب الأصلي مباشرة (in-place) بدل ما ينشئ نسخة جديدة.
    var eraseTick by remember { mutableIntStateOf(0) }
    val imageBitmap = remember(bitmap, eraseTick) { bitmap.asImageBitmap() }
    val density = LocalDensity.current

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val maxWidthPx = with(density) { maxWidth.toPx() }
        val maxHeightPx = with(density) { maxHeight.toPx() }
        val bitmapAspect = bitmap.width.toFloat() / bitmap.height.toFloat()
        val boxAspect = if (maxHeightPx > 0f) maxWidthPx / maxHeightPx else bitmapAspect

        val displayWidthPx: Float
        val displayHeightPx: Float
        if (bitmapAspect > boxAspect) {
            displayWidthPx = maxWidthPx
            displayHeightPx = maxWidthPx / bitmapAspect
        } else {
            displayHeightPx = maxHeightPx
            displayWidthPx = maxHeightPx * bitmapAspect
        }

        val displayWidthDp = with(density) { displayWidthPx.toDp() }
        val displayHeightDp = with(density) { displayHeightPx.toDp() }

        // نسبة تحويل من بكسل معروض على الشاشة إلى بكسل حقيقي بالبيتماب الأصلي
        val scaleToBitmap = if (displayWidthPx > 0f) bitmap.width / displayWidthPx else 1f

        Box(
            modifier = Modifier
                .size(displayWidthDp, displayHeightDp)
                .align(Alignment.Center)
        ) {
            Image(
                bitmap = imageBitmap,
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(activeTool, eraserRadius, scaleToBitmap) {
                        if (activeTool == EditorTool.ERASER) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    onErase(offset.x * scaleToBitmap, offset.y * scaleToBitmap, true)
                                    eraseTick++
                                },
                                onDrag = { change, _ ->
                                    change.consume()
                                    onErase(change.position.x * scaleToBitmap, change.position.y * scaleToBitmap, false)
                                    eraseTick++
                                }
                            )
                        }
                    }
            )

            textLayers.forEach { layer ->
                key(layer.id) {
                    DraggableTextLayer(
                        layer = layer,
                        containerWidthPx = displayWidthPx,
                        containerHeightPx = displayHeightPx,
                        onMoved = { xr, yr -> onTextMoved(layer.id, xr, yr) },
                        onTap = { onTextTap(layer.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun DraggableTextLayer(
    layer: TextLayerData,
    containerWidthPx: Float,
    containerHeightPx: Float,
    onMoved: (xRatio: Float, yRatio: Float) -> Unit,
    onTap: () -> Unit
) {
    var offsetXPx by remember(layer.id, containerWidthPx) { mutableFloatStateOf(layer.xRatio * containerWidthPx) }
    var offsetYPx by remember(layer.id, containerHeightPx) { mutableFloatStateOf(layer.yRatio * containerHeightPx) }
    val density = LocalDensity.current

    Text(
        text = layer.text,
        color = Color(layer.color),
        fontSize = with(density) { (containerWidthPx * layer.fontSizeRatio).toSp() },
        modifier = Modifier
            .offset { IntOffset(offsetXPx.roundToInt(), offsetYPx.roundToInt()) }
            .pointerInput(layer.id, containerWidthPx, containerHeightPx) {
                detectDragGestures(
                    onDragStart = { onTap() },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        offsetXPx = (offsetXPx + dragAmount.x).coerceIn(0f, containerWidthPx)
                        offsetYPx = (offsetYPx + dragAmount.y).coerceIn(0f, containerHeightPx)
                        onMoved(offsetXPx / containerWidthPx, offsetYPx / containerHeightPx)
                    }
                )
            }
    )
}
