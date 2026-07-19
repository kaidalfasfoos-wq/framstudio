package com.framestudio.app.imaging

/** فلاتر جاهزة بلمسة وحدة — كل فلتر مجرد تركيبة محددة مسبقاً من سطوع/تباين/تشبع */
data class FilterPreset(
    val nameKey: String,
    val brightness: Float,
    val contrast: Float,
    val saturation: Float
) {
    companion object {
        val PRESETS = listOf(
            FilterPreset("original", 0f, 0f, 0f),
            FilterPreset("bw", 0f, 10f, -100f),
            FilterPreset("vivid", 5f, 20f, 35f),
            FilterPreset("warm", 8f, 5f, 15f),
            FilterPreset("cool", -5f, 5f, -10f),
            FilterPreset("dramatic", -5f, 35f, -20f),
            FilterPreset("soft", 10f, -15f, -10f)
        )
    }
}
