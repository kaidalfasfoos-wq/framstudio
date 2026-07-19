package com.framestudio.app.data

/** طبقة نص فوق الصورة داخل محرر الصور — مو محفوظة بقاعدة البيانات، فقط أثناء جلسة التحرير */
data class TextLayerData(
    val id: String,
    val text: String,
    val xRatio: Float,          // الموقع الأفقي كنسبة من عرض الصورة 0..1
    val yRatio: Float,          // الموقع العمودي كنسبة من ارتفاع الصورة 0..1
    val fontSizeRatio: Float = 0.06f, // حجم الخط كنسبة من عرض الصورة
    val color: Int,
    val rotationDeg: Float = 0f
)
