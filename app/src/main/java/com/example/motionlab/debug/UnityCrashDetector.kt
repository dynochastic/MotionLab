package com.example.motionlab.debug

import android.app.Activity
import android.content.Context
import android.os.Build
import android.util.Log
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

/**
 * Unity Crash Detection and Monitoring System
 * Detects and logs Unity crashes, ANRs, and performance issues
 */
class UnityCrashDetector private constructor() {
    
    companion object {
        private const val TAG = "UnityCrashDetector"
        private const val CRASH_LOG_DIR = "unity_crash_logs"
        
        @Volatile
        private var INSTANCE: UnityCrashDetector? = null
        
        fun getInstance(): UnityCrashDetector {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: UnityCrashDetector().also { INSTANCE = it }
            }
        }
    }
    
    private var isMonitoring = false
    private var crashLogFile: File? = null
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())
    
    /**
     * Start monitoring Unity crashes
     */
    fun startMonitoring(context: Context) {
        if (isMonitoring) return
        
        try {
            setupCrashLogging(context)
            setupUncaughtExceptionHandler()
            setupANRDetection()
            isMonitoring = true
            
            Log.i(TAG, "Unity crash monitoring started")
            UnityDebugLogger.logUnityBridgeEvent("Crash Monitoring Started", "Monitoring enabled for Unity integration")
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start crash monitoring: ${e.message}")
        }
    }
    
    /**
     * Stop monitoring
     */
    fun stopMonitoring() {
        isMonitoring = false
        Log.i(TAG, "Unity crash monitoring stopped")
    }
    
    /**
     * Setup crash logging directory
     */
    private fun setupCrashLogging(context: Context) {
        try {
            val crashDir = File(context.getExternalFilesDir(null), CRASH_LOG_DIR)
            if (!crashDir.exists()) {
                crashDir.mkdirs()
            }
            
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            crashLogFile = File(crashDir, "unity_crash_$timestamp.log")
            
            Log.i(TAG, "Crash logging setup: ${crashLogFile?.absolutePath}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to setup crash logging: ${e.message}")
        }
    }
    
    /**
     * Setup global uncaught exception handler
     */
    private fun setupUncaughtExceptionHandler() {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        
        Thread.setDefaultUncaughtExceptionHandler { thread, exception ->
            try {
                if (isUnityRelatedCrash(exception)) {
                    logUnityCrash(thread, exception)
                }
                
                defaultHandler?.uncaughtException(thread, exception)
                
            } catch (e: Exception) {
                Log.e(TAG, "Error in crash handler: ${e.message}")
                defaultHandler?.uncaughtException(thread, exception)
            }
        }
    }
    
    /**
     * Check if crash is Unity-related
     */
    private fun isUnityRelatedCrash(exception: Throwable): Boolean {
        val stackTrace = exception.stackTraceToString()
        return stackTrace.contains("unity") || 
               stackTrace.contains("Unity") ||
               stackTrace.contains("il2cpp") ||
               stackTrace.contains("mono") ||
               stackTrace.contains("UnityPlayer")
    }
    
    /**
     * Log Unity crash with detailed information
     */
    private fun logUnityCrash(thread: Thread, exception: Throwable) {
        try {
            val timestamp = dateFormat.format(Date())
            val crashInfo = buildString {
                appendLine("=== UNITY CRASH DETECTED ===")
                appendLine("Timestamp: $timestamp")
                appendLine("Thread: ${thread.name}")
                appendLine("Exception: ${exception.javaClass.simpleName}")
                appendLine("Message: ${exception.message}")
                appendLine("Stack Trace:")
                appendLine(exception.stackTraceToString())
                appendLine("=== END CRASH LOG ===")
            }
            
            Log.e(TAG, "Unity crash detected: ${exception.message}")
            Log.e(TAG, crashInfo)
            
            writeCrashToFile(crashInfo)
            
            UnityDebugLogger.logUnityCrash(
                "Unity Crash", 
                "Uncaught exception: ${exception.message}", 
                exception
            )
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to log Unity crash: ${e.message}")
        }
    }
    
    /**
     * Setup ANR (Application Not Responding) detection
     */
    private fun setupANRDetection() {
        val mainThread = Thread.currentThread()
        
        Thread {
            while (isMonitoring) {
                try {
                    Thread.sleep(5000)
                    
                    val startTime = System.currentTimeMillis()
                    mainThread.interrupt()
                    Thread.sleep(100)
                    val endTime = System.currentTimeMillis()
                    
                    if (endTime - startTime > 1000) {
                        Log.w(TAG, "Potential ANR detected")
                        UnityDebugLogger.logUnityBridgeEvent("ANR Warning", "Main thread may be blocked")
                    }
                    
                } catch (e: Exception) {
                    Log.e(TAG, "ANR detection error: ${e.message}")
                }
            }
        }.start()
    }
    
    /**
     * Write crash information to file
     */
    private fun writeCrashToFile(crashInfo: String) {
        try {
            crashLogFile?.let { file ->
                FileWriter(file, true).use { writer ->
                    writer.appendLine(crashInfo)
                    writer.appendLine() // Empty line separator
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to write crash to file: ${e.message}")
        }
    }
    
    /**
     * Log Unity performance metrics
     */
    fun logPerformanceMetrics(context: Context) {
        try {
            val runtime = Runtime.getRuntime()
            val memoryInfo = android.app.ActivityManager.MemoryInfo()
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager
            activityManager.getMemoryInfo(memoryInfo)
            
            val metrics = mapOf(
                "totalMemory" to (runtime.totalMemory() / 1024 / 1024),
                "freeMemory" to (runtime.freeMemory() / 1024 / 1024),
                "maxMemory" to (runtime.maxMemory() / 1024 / 1024),
                "availableMemory" to (memoryInfo.availMem / 1024 / 1024),
                "lowMemory" to memoryInfo.lowMemory,
                "threshold" to (memoryInfo.threshold / 1024 / 1024)
            )
            
            UnityDebugLogger.logPerformanceMetrics(metrics)
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to log performance metrics: ${e.message}")
        }
    }
    
    /**
     * Get crash log file path
     */
    fun getCrashLogPath(): String? {
        return crashLogFile?.absolutePath
    }
    
    /**
     * Clear crash logs
     */
    fun clearCrashLogs() {
        try {
            crashLogFile?.delete()
            Log.i(TAG, "Crash logs cleared")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clear crash logs: ${e.message}")
        }
    }
}
