package com.framestudio.app.data

/**
 * أي عنصر قابل للتحريك/التكبير/التدوير فوق الصورة داخل المحرر: نص أو ملصق (ستيكر).
 * ترتيب العناصر بالقائمة يحدد ترتيب الطبقات (index أصغر = تحت، أكبر = فوق).
 */
sealed class EditorLayer {
    abstract val id: String
    abstract val xRatio: Float
    abstract val yRatio: Float
    abstract val scale: Float
    abstract val rotationDeg: Float
    abstract val opacity: Float
    abstract val visible: Boolean

    data class TextLayer(
        override val id: String,
        override val xRatio: Float = 0.5f,
        override val yRatio: Float = 0.5f,
        override val scale: Float = 1f,
        override val rotationDeg: Float = 0f,
        override val opacity: Float = 1f,
        override val visible: Boolean = true,
        val text: String,
        val fontSizeRatio: Float = 0.06f,
        val color: Int
    ) : EditorLayer()

    data class StickerLayer(
        override val id: String,
        override val xRatio: Float = 0.5f,
        override val yRatio: Float = 0.5f,
        override val scale: Float = 1f,
        override val rotationDeg: Float = 0f,
        override val opacity: Float = 1f,
        override val visible: Boolean = true,
        val emoji: String,
        val sizeRatio: Float = 0.15f
    ) : EditorLayer()

    fun withTransform(
        xRatio: Float = this.xRatio,
        yRatio: Float = this.yRatio,
        scale: Float = this.scale,
        rotationDeg: Float = this.rotationDeg,
        opacity: Float = this.opacity,
        visible: Boolean = this.visible
    ): EditorLayer = when (this) {
        is TextLayer -> copy(xRatio = xRatio, yRatio = yRatio, scale = scale, rotationDeg = rotationDeg, opacity = opacity, visible = visible)
        is StickerLayer -> copy(xRatio = xRatio, yRatio = yRatio, scale = scale, rotationDeg = rotationDeg, opacity = opacity, visible = visible)
    }
}
