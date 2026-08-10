package com.example.motionlab.data.local.dao


import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.motionlab.data.local.entity.SubtopicProgressEntity

@Dao
interface SubtopicProgressDao {

    @Query("SELECT * FROM subtopic_progress WHERE LOWER(username) = LOWER(:username) AND subtopicId = :subtopicId")
    suspend fun getProgress(username: String, subtopicId: Int): SubtopicProgressEntity?

    @Query("SELECT * FROM subtopic_progress WHERE LOWER(username) = LOWER(:username)")
    suspend fun getAllProgressForUser(username: String): List<SubtopicProgressEntity>

    @Query("SELECT * FROM subtopic_progress WHERE LOWER(username) = LOWER(:username)")
    fun getAllProgressForUserFlow(username: String): kotlinx.coroutines.flow.Flow<List<SubtopicProgressEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProgress(progress: SubtopicProgressEntity)

    @Update
    suspend fun updateProgress(progress: SubtopicProgressEntity)
}
