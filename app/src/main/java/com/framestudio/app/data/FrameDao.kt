package com.framestudio.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FrameDao {
    @Query("SELECT * FROM frames ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<FrameEntity>>

    @Query("SELECT * FROM frames WHERE id = :id")
    suspend fun getById(id: Long): FrameEntity?

    @Insert
    suspend fun insert(frame: FrameEntity): Long

    @Delete
    suspend fun delete(frame: FrameEntity)
}
