package com.framestudio.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * "أكشن" = مجموعة تعديلات محفوظة (إطار + سطوع/تباين/تشبع + نسبة قص + توقيع نصي)
 * تُطبَّق لاحقاً على أكثر من صورة دفعة وحدة.
 */
@Entity(tableName = "actions")
data class ActionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val frameId: Long? = null,
    val brightness: Float = 0f,      // -100..100
    val contrast: Float = 0f,        // -100..100
    val saturation: Float = 0f,      // -100..100
    val cropAspect: String = "original", // original | 1:1 | 4:5 | 16:9
    val watermarkText: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
