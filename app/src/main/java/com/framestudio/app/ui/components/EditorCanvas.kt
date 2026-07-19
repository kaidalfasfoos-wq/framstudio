package com.framestudio.app.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import com.framestudio.app.data.EditorLayer
import com.framestudio.app.viewmodel.EditorTool
import kotlin.math.roundToInt

/** لوحة عرض الصورة داخل المحرر: تعرض البيتماب وكل طبقات النص/الملصقات القابلة للسحب والتكبير والتدوير */
@Composable
fun EditorCanvas(
    bitmap: Bitmap,
    layers: List<EditorLayer>,
    selectedLayerId: String?,
    activeTool: EditorTool,
    eraserRadius: Float,
    onErase: (xPx: Float, yPx: Float, isStart: Boolean) -> Unit,
    onLayerTransform: (id: String, xRatio: Float?, yRatio: Float?, scale: Float?, rotationDeg: Float?) -> Unit,
    onLayerTap: (id: String) -> Unit
) {
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

            layers.forEach { layer ->
                key(layer.id) {
                    if (layer.visible) {
                        TransformableLayerView(
                            layer = layer,
                            containerWidthPx = displayWidthPx,
                            containerHeightPx = displayHeightPx,
                            interactionEnabled = activeTool == EditorTool.LAYERS || activeTool == EditorTool.TEXT || activeTool == EditorTool.STICKER,
                            onTransform = { xr, yr, sc, rot -> onLayerTransform(layer.id, xr, yr, sc, rot) },
                            onTap = { onLayerTap(layer.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TransformableLayerView(
    layer: EditorLayer,
    containerWidthPx: Float,
    containerHeightPx: Float,
    interactionEnabled: Boolean,
    onTransform: (xRatio: Float?, yRatio: Float?, scale: Float?, rotationDeg: Float?) -> Unit,
    onTap: () -> Unit
) {
    var offsetXPx by remember(layer.id, containerWidthPx) { mutableFloatStateOf(layer.xRatio * containerWidthPx) }
    var offsetYPx by remember(layer.id, containerHeightPx) { mutableFloatStateOf(layer.yRatio * containerHeightPx) }
    var scaleValue by remember(layer.id) { mutableFloatStateOf(layer.scale) }
    var rotationValue by remember(layer.id) { mutableFloatStateOf(layer.rotationDeg) }
    val density = LocalDensity.current

    Box(
        modifier = Modifier
            .offset { IntOffset(offsetXPx.roundToInt(), offsetYPx.roundToInt()) }
            .scale(scaleValue)
            .rotate(rotationValue)
            .pointerInput(layer.id, interactionEnabled, containerWidthPx, containerHeightPx) {
                if (interactionEnabled) {
                    detectTransformGestures { _, pan, zoom, rotationChange ->
                        onTap()
                        offsetXPx = (offsetXPx + pan.x).coerceIn(0f, containerWidthPx)
                        offsetYPx = (offsetYPx + pan.y).coerceIn(0f, containerHeightPx)
                        scaleValue = (scaleValue * zoom).coerceIn(0.3f, 5f)
                        rotationValue += rotationChange
                        onTransform(
                            offsetXPx / containerWidthPx,
                            offsetYPx / containerHeightPx,
                            scaleValue,
                            rotationValue
                        )
                    }
                }
            }
    ) {
        when (layer) {
            is EditorLayer.TextLayer -> Text(
                text = layer.text,
                color = Color(layer.color).copy(alpha = layer.opacity),
                fontSize = with(density) { (containerWidthPx * layer.fontSizeRatio).toSp() }
            )
            is EditorLayer.StickerLayer -> Text(
                text = layer.emoji,
                fontSize = with(density) { (containerWidthPx * layer.sizeRatio).toSp() }
            )
        }
    }
}
