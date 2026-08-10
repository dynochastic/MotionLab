package com.example.motionlab.presentation.exercise

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.motionlab.domain.model.local.HandsOnExercise
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HandsOnExerciseViewModel @Inject constructor(
    private val app: Application
) : AndroidViewModel(app) {

    private val _exercises = MutableStateFlow<List<HandsOnExercise>>(emptyList())
    val exercises: StateFlow<List<HandsOnExercise>> = _exercises
    
    private val _currentExercise = MutableStateFlow<HandsOnExercise?>(null)
    val currentExercise: StateFlow<HandsOnExercise?> = _currentExercise

    fun loadExercisesFromAssets(jsonPath: String) {

        if (jsonPath.isBlank()) {
            println("❌ Empty or null jsonPath provided")
            _exercises.value = emptyList()
            return
        }
        println("📁 Loading exercises from assets: $jsonPath")
        viewModelScope.launch {
            try {
                val inputStream = app.assets.open(jsonPath)
                val jsonString = inputStream.bufferedReader().use { it.readText() }
                println("📄 JSON loaded successfully, length: ${jsonString.length}")

                val gson = Gson()
                val listType = object : TypeToken<List<HandsOnExercise>>() {}.type
                val exerciseList: List<HandsOnExercise> = gson.fromJson(jsonString, listType)
                println("✅ Parsed ${exerciseList.size} exercises")
                
                if (exerciseList.isNotEmpty()) {
                    // Randomly select one exercise from the list
                    val randomIndex = (0 until exerciseList.size).random()
                    val selectedExercise = exerciseList[randomIndex]
                    println("🎲 Selected exercise: ${selectedExercise.title}")
                    
                    _currentExercise.value = selectedExercise
                }

                _exercises.value = exerciseList
            } catch (e: Exception) {
                println("❌ Error loading exercises: ${e.message}")
                e.printStackTrace()
                _exercises.value = emptyList()
            }
        }
    }
    
    fun getNewRandomExercise() {
        val currentList = _exercises.value
        if (currentList.isNotEmpty()) {
            val randomIndex = (0 until currentList.size).random()
            val selectedExercise = currentList[randomIndex]
            println("🎲 New random exercise selected: ${selectedExercise.title}")
            _currentExercise.value = selectedExercise
        }
    }
}

