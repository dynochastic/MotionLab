package com.example.motionlab.debug

import android.util.Log
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

/**
 * Enhanced Unity-Android Bridge Debug Logger
 * Provides comprehensive logging for Unity integration debugging
 * Works with both physical devices and emulators
 */
object UnityDebugLogger {
    
    private const val TAG = "UnityDebug"
    private const val UNITY_TAG = "UnityBridge"
    private const val CRASH_TAG = "UnityCrash"
    
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())
    private var logFile: File? = null
    private var isLoggingToFile = false
    
    /**
     * Initialize file logging (optional)
     */
    fun initializeFileLogging(context: android.content.Context) {
        try {
            val logDir = File(context.getExternalFilesDir(null), "unity_logs")
            if (!logDir.exists()) {
                logDir.mkdirs()
            }
            
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            logFile = File(logDir, "unity_debug_$timestamp.log")
            isLoggingToFile = true
            
            Log.i(TAG, "File logging initialized: ${logFile?.absolutePath}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize file logging: ${e.message}")
        }
    }
    
    /**
     * Log Unity bridge events with enhanced details
     */
    fun logUnityBridgeEvent(event: String, details: String = "", throwable: Throwable? = null) {
        val timestamp = dateFormat.format(Date())
        val message = "[$timestamp] $event${if (details.isNotEmpty()) " - $details" else ""}"
        
        Log.i(UNITY_TAG, message)
        if (throwable != null) {
            Log.e(UNITY_TAG, message, throwable)
        }
        
        if (isLoggingToFile) {
            writeToFile("BRIDGE", message, throwable)
        }
    }
    
    /**
     * Log Unity lifecycle events
     */
    fun logUnityLifecycle(event: String, component: String, details: Map<String, Any> = emptyMap()) {
        val timestamp = dateFormat.format(Date())
        val detailsStr = if (details.isNotEmpty()) " - ${details.entries.joinToString(", ") { "${it.key}=${it.value}" }}" else ""
        val message = "[$timestamp] $component: $event$detailsStr"
        
        Log.d(UNITY_TAG, message)
        
        if (isLoggingToFile) {
            writeToFile("LIFECYCLE", message)
        }
    }
    
    /**
     * Log Unity crashes with stack traces
     */
    fun logUnityCrash(event: String, details: String, throwable: Throwable? = null) {
        val timestamp = dateFormat.format(Date())
        val message = "[$timestamp] CRASH: $event - $details"
        
        Log.e(CRASH_TAG, message)
        if (throwable != null) {
            Log.e(CRASH_TAG, message, throwable)
        }
        
        if (isLoggingToFile) {
            writeToFile("CRASH", message, throwable)
        }
    }
    
    /**
     * Log Unity-Android communication
     */
    fun logUnityCommunication(direction: String, event: String, data: Map<String, Any> = emptyMap()) {
        val timestamp = dateFormat.format(Date())
        val dataStr = if (data.isNotEmpty()) " - Data: ${data.entries.joinToString(", ") { "${it.key}=${it.value}" }}" else ""
        val message = "[$timestamp] $direction: $event$dataStr"
        
        Log.d(UNITY_TAG, message)
        
        if (isLoggingToFile) {
            writeToFile("COMM", message)
        }
    }
    
    /**
     * Log system information
     */
    fun logSystemInfo(context: android.content.Context) {
        val deviceInfo = """
            Device: ${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}
            Android: ${android.os.Build.VERSION.RELEASE} (API ${android.os.Build.VERSION.SDK_INT})
            Unity Version: Unity Library Integrated
            App Version: ${context.packageManager.getPackageInfo(context.packageName, 0).versionName}
        """.trimIndent()
        
        Log.i(TAG, "System Info:\n$deviceInfo")
        
        if (isLoggingToFile) {
            writeToFile("SYSTEM", deviceInfo)
        }
    }
    
    /**
     * Log performance metrics
     */
    fun logPerformanceMetrics(metrics: Map<String, Any>) {
        val timestamp = dateFormat.format(Date())
        val metricsStr = metrics.entries.joinToString(", ") { "${it.key}=${it.value}" }
        val message = "[$timestamp] Performance: $metricsStr"
        
        Log.d(TAG, message)
        
        if (isLoggingToFile) {
            writeToFile("PERF", message)
        }
    }
    
    /**
     * Write to log file
     */
    private fun writeToFile(category: String, message: String, throwable: Throwable? = null) {
        try {
            logFile?.let { file ->
                FileWriter(file, true).use { writer ->
                    writer.appendLine("[$category] $message")
                    if (throwable != null) {
                        writer.appendLine("Stack trace: ${throwable.stackTraceToString()}")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to write to log file: ${e.message}")
        }
    }
    
    /**
     * Get log file path
     */
    fun getLogFilePath(): String? = logFile?.absolutePath
    
    /**
     * Clear log file
     */
    fun clearLogFile() {
        try {
            logFile?.delete()
            Log.i(TAG, "Log file cleared")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clear log file: ${e.message}")
        }
    }
}
