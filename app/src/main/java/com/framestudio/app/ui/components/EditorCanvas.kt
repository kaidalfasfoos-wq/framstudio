package com.framestudio.app.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.framestudio.app.data.EditorLayer
import com.framestudio.app.viewmodel.EditorTool
import kotlin.math.roundToInt

/** يبني ColorMatrix لمعاينة السطوع/التباين/التشبع حيّة على الشاشة، بدون لمس بكسلات البيتماب الأصلي */
private fun buildPreviewColorMatrix(brightness: Float, contrast: Float, saturation: Float): ColorMatrix {
    val satMatrix = ColorMatrix().apply { setToSaturation(1f + saturation / 100f) }
    val c = 1f + contrast / 100f
    val b = brightness / 100f * 255f
    val translate = (-0.5f * c + 0.5f) * 255f + b
    val contrastMatrix = ColorMatrix(
        floatArrayOf(
            c, 0f, 0f, 0f, translate,
            0f, c, 0f, 0f, translate,
            0f, 0f, c, 0f, translate,
            0f, 0f, 0f, 1f, 0f
        )
    )
    val result = ColorMatrix()
    result *= satMatrix
    result *= contrastMatrix
    return result
}

@Composable
fun EditorCanvas(
    bitmap: Bitmap,
    layers: List<EditorLayer>,
    selectedLayerId: String?,
    activeTool: EditorTool,
    eraserRadius: Float,
    brightness: Float,
    contrast: Float,
    saturation: Float,
    pendingCropAspect: String?,
    onErase: (xPx: Float, yPx: Float, isStart: Boolean) -> Unit,
    onLayerTransform: (id: String, xRatio: Float?, yRatio: Float?, scale: Float?, rotationDeg: Float?) -> Unit,
    onLayerTap: (id: String) -> Unit
) {
    var eraseTick by remember { mutableIntStateOf(0) }
    val imageBitmap = remember(bitmap, eraseTick) { bitmap.asImageBitmap() }
    val density = LocalDensity.current
    val previewColorFilter = remember(brightness, contrast, saturation) {
        ColorFilter.colorMatrix(buildPreviewColorMatrix(brightness, contrast, saturation))
    }

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
                colorFilter = previewColorFilter,
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

            // الطبقات تبقى قابلة للسحب دائماً إلا وقت الممحاة أو القص (يحتاجوا كل اللمس لحالهم)
            val layersInteractive = activeTool != EditorTool.ERASER && activeTool != EditorTool.CROP
            layers.forEach { layer ->
                key(layer.id) {
                    if (layer.visible) {
                        TransformableLayerView(
                            layer = layer,
                            containerWidthPx = displayWidthPx,
                            containerHeightPx = displayHeightPx,
                            interactionEnabled = layersInteractive,
                            onTransform = { xr, yr, sc, rot -> onLayerTransform(layer.id, xr, yr, sc, rot) },
                            onTap = { onLayerTap(layer.id) }
                        )
                    }
                }
            }

            // معاينة منطقة القص المقترحة قبل التأكيد
            if (activeTool == EditorTool.CROP && pendingCropAspect != null && pendingCropAspect != "original") {
                CropGuideOverlay(pendingCropAspect, displayWidthPx, displayHeightPx)
            }
        }
    }
}

@Composable
private fun CropGuideOverlay(aspect: String, containerWidthPx: Float, containerHeightPx: Float) {
    val parts = aspect.split(":")
    val targetRatio = if (parts.size == 2) parts[0].toFloat() / parts[1].toFloat() else 1f
    val containerRatio = containerWidthPx / containerHeightPx

    val rectWidthPx: Float
    val rectHeightPx: Float
    if (containerRatio > targetRatio) {
        rectHeightPx = containerHeightPx
        rectWidthPx = containerHeightPx * targetRatio
    } else {
        rectWidthPx = containerWidthPx
        rectHeightPx = containerWidthPx / targetRatio
    }
    val left = (containerWidthPx - rectWidthPx) / 2f
    val top = (containerHeightPx - rectHeightPx) / 2f

    Canvas(modifier = Modifier.fillMaxSize()) {
        val dim = Color.Black.copy(alpha = 0.55f)
        // تعتيم كل شي، وترك مستطيل القص شفاف بمنتصف الصورة
        drawRect(color = dim, topLeft = Offset(0f, 0f), size = androidx.compose.ui.geometry.Size(size.width, top))
        drawRect(color = dim, topLeft = Offset(0f, top + rectHeightPx), size = androidx.compose.ui.geometry.Size(size.width, size.height - top - rectHeightPx))
        drawRect(color = dim, topLeft = Offset(0f, top), size = androidx.compose.ui.geometry.Size(left, rectHeightPx))
        drawRect(color = dim, topLeft = Offset(left + rectWidthPx, top), size = androidx.compose.ui.geometry.Size(size.width - left - rectWidthPx, rectHeightPx))
        drawRect(
            color = Color.White,
            topLeft = Offset(left, top),
            size = androidx.compose.ui.geometry.Size(rectWidthPx, rectHeightPx),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f)
        )
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
    var contentSize by remember { mutableStateOf(IntSize.Zero) }
    val density = LocalDensity.current

    // نفس مرجعية التمركز المستخدمة بالتصدير النهائي: (offsetXPx, offsetYPx) هي منتصف العنصر تماماً
    Box(
        modifier = Modifier
            .offset {
                IntOffset(
                    (offsetXPx - contentSize.width / 2f).roundToInt(),
                    (offsetYPx - contentSize.height / 2f).roundToInt()
                )
            }
            .onSizeChanged { contentSize = it }
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
