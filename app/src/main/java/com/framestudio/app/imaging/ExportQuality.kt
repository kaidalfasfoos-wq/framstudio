package com.framestudio.app.imaging

/**
 * مستويات جودة التصدير المتاحة للمستخدم — من الأصغر (أسرع وأخف بالحجم)
 * إلى الأكبر (أعلى جودة ممكنة، أبطأ وتستهلك ذاكرة/تخزين أكتر).
 *
 * maxDimension: أقصى بعد (طول أو عرض) للصورة الناتجة بالبكسل.
 * jpegQuality: تُستخدم فقط عندما losslessPng = false.
 * losslessPng: إذا true يُحفظ الناتج بصيغة PNG بدون أي فقدان بالجودة إطلاقاً.
 */
enum class ExportQuality(
    val maxDimension: Int,
    val jpegQuality: Int,
    val losslessPng: Boolean
) {
    LOW(maxDimension = 1080, jpegQuality = 85, losslessPng = false),
    MEDIUM(maxDimension = 2048, jpegQuality = 92, losslessPng = false),
    HIGH(maxDimension = 3000, jpegQuality = 97, losslessPng = false),
    MAX(maxDimension = 4096, jpegQuality = 100, losslessPng = true);

    val mimeType: String get() = if (losslessPng) "image/png" else "image/jpeg"
    val fileExtension: String get() = if (losslessPng) "png" else "jpg"
}
