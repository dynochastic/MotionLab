package com.example.motionlab.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.motionlab.data.local.entity.SubtopicMaterialEntity

@Dao
interface SubtopicMaterialDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMaterials(materials: List<SubtopicMaterialEntity>)


    @Query("SELECT * FROM subtopic_materials WHERE subtopicId = :subtopicId")
    suspend fun getMaterialsBySubtopicId(subtopicId: Int): List<SubtopicMaterialEntity>

}

