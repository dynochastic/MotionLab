package com.example.motionlab.data.local.db

import androidx.room.TypeConverter
import com.example.motionlab.data.local.entity.MaterialType
import com.example.motionlab.domain.model.local.VideoTranscriptSegments
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {

    //Test/Quiz Material
    @TypeConverter
    fun fromStringList(value: List<String>): String = Gson().toJson(value)

    @TypeConverter
    fun toStringList(value: String): List<String> =
        Gson().fromJson(value, object : TypeToken<List<String>>() {}.type)


    //Subtopic Materials
    @TypeConverter
    fun fromMaterialType(value: MaterialType): String = value.name

    @TypeConverter
    fun toMaterialType(value: String): MaterialType = MaterialType.valueOf(value)
}


