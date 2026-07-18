package com.framestudio.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * يمثل إطار صورة واحد: إما مستورد من صورة PNG جاهزة أو مصمم داخل التطبيق.
 * filePath: مسار ملف PNG بخلفية شفافة محفوظ داخل مجلد files/frames الخاص بالتطبيق.
 */
@Entity(tableName = "frames")
data class FrameEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val filePath: String,
    val isBuiltIn: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
