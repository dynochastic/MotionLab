package com.example.motionlab.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.motionlab.data.local.entity.LessonProgressEntity

@Dao
interface LessonProgressDao {

    @Query("SELECT * FROM lesson_progress WHERE LOWER(username) = LOWER(:username)")
    suspend fun getProgressForUser(username: String): List<LessonProgressEntity>

    @Query("SELECT * FROM lesson_progress WHERE LOWER(username) = LOWER(:username) AND lessonId = :lessonId")
    suspend fun getLessonProgress(username: String, lessonId: Int): LessonProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProgress(progress: LessonProgressEntity)

    @Update
    suspend fun updateProgress(progress: LessonProgressEntity)

    @Query("SELECT * FROM lesson_progress WHERE LOWER(username) = LOWER(:username)")
    fun getProgressForUserFlow(username: String): kotlinx.coroutines.flow.Flow<List<LessonProgressEntity>>
}
