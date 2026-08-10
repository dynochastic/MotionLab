package com.example.motionlab.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.motionlab.data.local.entity.SubtopicEntity

@Dao
interface SubtopicDao {

    // Get all subtopics for a specific lesson (ordered)
    @Query("SELECT * FROM subtopics WHERE lessonId = :lessonId ORDER BY `order` ASC")
    suspend fun getSubtopicsForLesson(lessonId: Int): List<SubtopicEntity>

    @Query("SELECT * FROM subtopics")
    suspend fun getAllSubtopics(): List<SubtopicEntity>


    // Insert one or many subtopics
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubtopics(subtopics: List<SubtopicEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubtopic(subtopic: SubtopicEntity)

    @Query("SELECT lessonId FROM subtopics WHERE subtopicId = :subtopicId LIMIT 1")
    suspend fun getLessonIdForSubtopic(subtopicId: Int): Int

    @Query("SELECT * FROM subtopics WHERE lessonId = :lessonId")
    suspend fun getSubtopicsByLessonId(lessonId: Int): List<SubtopicEntity>

    @Query("SELECT * FROM subtopics WHERE subtopicId = :id LIMIT 1")
    suspend fun getSubtopicById(id: Int): SubtopicEntity


}
