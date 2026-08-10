package com.example.motionlab.ui.screens.simulation

/**
 * Maps subtopic titles to Unity scene identifiers for the 9 simulations
 */
object SimulationMapper {
    
    // Map of subtopic titles to Unity scene names 
    private val subtopicToSceneMap = mapOf(
        "uniformly accelerated motion" to "UAM",
        "projectile motion" to "PM",
        "momentum" to "momentum",
        "law of inertia" to "1st Law",
        "force = mass x acceleration" to "2nd Law",
        "action-reaction" to "3rd Law",
        "work" to "Work",
        "power" to "Power",
        "energy" to "Energy"
    )
    
    /**
     * Converts a subtopic title to a Unity scene identifier
     * @param subtopicTitle The title of the subtopic
     * @return The Unity scene identifier, or null if not found
     */
    fun getSceneId(subtopicTitle: String): String? {
        val normalizedTitle = subtopicTitle.lowercase().trim()
        println("🔍 Looking for simulation for subtopic: '$subtopicTitle' -> normalized: '$normalizedTitle'")
        val sceneId = subtopicToSceneMap[normalizedTitle]
        println("🎯 Found scene ID: $sceneId")
        return sceneId
    }
    
    /**
     * Gets all available simulation scene IDs
     */
    fun getAllSceneIds(): List<String> = subtopicToSceneMap.values.toList()
    
    /**
     * Checks if a subtopic has a corresponding simulation
     */
    fun hasSimulation(subtopicTitle: String): Boolean {
        return getSceneId(subtopicTitle) != null
    }
}
