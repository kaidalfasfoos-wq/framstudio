package com.framestudio.app.data

import kotlinx.coroutines.flow.Flow

class Repository(private val db: AppDatabase) {

    val frames: Flow<List<FrameEntity>> = db.frameDao().observeAll()
    val actions: Flow<List<ActionEntity>> = db.actionDao().observeAll()

    suspend fun getFrame(id: Long): FrameEntity? = db.frameDao().getById(id)
    suspend fun addFrame(frame: FrameEntity): Long = db.frameDao().insert(frame)
    suspend fun deleteFrame(frame: FrameEntity) = db.frameDao().delete(frame)

    suspend fun getAction(id: Long): ActionEntity? = db.actionDao().getById(id)
    suspend fun addAction(action: ActionEntity): Long = db.actionDao().insert(action)
    suspend fun updateAction(action: ActionEntity) = db.actionDao().update(action)
    suspend fun deleteAction(action: ActionEntity) = db.actionDao().delete(action)
}
