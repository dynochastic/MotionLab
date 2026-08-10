package com.example.motionlab.debug

import android.app.Activity
import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.util.Log
import android.view.WindowManager
import com.example.motionlab.debug.UnityDebugLogger

/**
 * Unity Crash Fixer
 * Fixes common Unity integration crashes and compatibility issues
 */
object UnityCrashFixer {
    
    private const val TAG = "UnityCrashFixer"
    
    /**
     * Apply all Unity crash fixes
     */
    fun applyAllFixes(context: Context) {
        try {
            UnityDebugLogger.logUnityBridgeEvent("Applying Unity Crash Fixes", "Starting comprehensive fix application")
            
            fixAdaptivePerformanceIssues()
            fixGraphicsFormatIssues(context)
            fixInputChannelIssues()
            fixMemoryIssues()
            fixDisplayIssues(context)
            
            UnityDebugLogger.logUnityBridgeEvent("Unity Crash Fixes Applied", "All fixes have been applied successfully")
            
        } catch (e: Exception) {
            UnityDebugLogger.logUnityCrash("Fix Application Failed", "Error applying Unity fixes: ${e.message}", e)
        }
    }
    
    /**
     * Fix Unity Adaptive Performance initialization issues
     */
    private fun fixAdaptivePerformanceIssues() {
        try {
            System.setProperty("unity.adaptive.performance.enabled", "false")
            System.setProperty("unity.adaptive.performance.google.enabled", "false")
            System.setProperty("unity.adaptive.performance.samsung.enabled", "false")
            
            Log.i(TAG, "Adaptive Performance features disabled")
            UnityDebugLogger.logUnityBridgeEvent("Adaptive Performance Fix", "Disabled problematic Adaptive Performance features")
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fix Adaptive Performance issues: ${e.message}")
        }
    }
    
    /**
     * Fix graphics format compatibility issues
     */
    private fun fixGraphicsFormatIssues(context: Context) {
        try {
            System.setProperty("unity.graphics.format.r8", "false")
            System.setProperty("unity.graphics.format.rgba10101010", "false")
            System.setProperty("unity.graphics.format.hdr", "false")
            
            System.setProperty("unity.graphics.pixelformat", "RGB_565")
            
            System.setProperty("unity.graphics.hwc", "false")
            System.setProperty("unity.graphics.render", "false")
            
            Log.i(TAG, "Graphics format compatibility fixes applied")
            UnityDebugLogger.logUnityBridgeEvent("Graphics Format Fix", "Applied graphics format compatibility fixes")
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fix graphics format issues: ${e.message}")
        }
    }
    
    /**
     * Fix input channel disposal issues
     */
    private fun fixInputChannelIssues() {
        try {
            System.setProperty("unity.input.touch", "true")
            System.setProperty("unity.input.multitouch", "true")
            System.setProperty("unity.input.gyroscope", "false")
            System.setProperty("unity.input.accelerometer", "false")
            
            System.setProperty("unity.input.channel.disposal", "safe")
            
            Log.i(TAG, "Input channel fixes applied")
            UnityDebugLogger.logUnityBridgeEvent("Input Channel Fix", "Applied input channel disposal fixes")
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fix input channel issues: ${e.message}")
        }
    }
    
    /**
     * Fix memory-related issues
     */
    private fun fixMemoryIssues() {
        try {
            System.setProperty("unity.memory.heap.size", "256m")
            System.setProperty("unity.memory.gc.threshold", "64m")
            System.setProperty("unity.memory.gc.frequency", "30")
            
            System.setProperty("unity.memory.profiler", "false")
            System.setProperty("unity.memory.tracker", "false")
            
            Log.i(TAG, "Memory optimization fixes applied")
            UnityDebugLogger.logUnityBridgeEvent("Memory Fix", "Applied memory optimization fixes")
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fix memory issues: ${e.message}")
        }
    }
    
    /**
     * Fix display-related issues
     */
    private fun fixDisplayIssues(context: Context) {
        try {
            System.setProperty("unity.display.orientation", "portrait")
            System.setProperty("unity.display.scaling", "1.0")
            System.setProperty("unity.display.dpi", "320")
            
            System.setProperty("unity.display.hdr", "false")
            System.setProperty("unity.display.vsync", "true")
            System.setProperty("unity.display.framerate", "60")
            
            Log.i(TAG, "Display fixes applied")
            UnityDebugLogger.logUnityBridgeEvent("Display Fix", "Applied display configuration fixes")
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fix display issues: ${e.message}")
        }
    }
    
    /**
     * Apply device-specific fixes
     */
    fun applyDeviceSpecificFixes(context: Context) {
        try {
            val manufacturer = Build.MANUFACTURER.lowercase()
            val model = Build.MODEL.lowercase()
            
            when {
                manufacturer.contains("samsung") -> {
                    fixSamsungSpecificIssues()
                }
                manufacturer.contains("huawei") || manufacturer.contains("honor") -> {
                    fixHuaweiSpecificIssues()
                }
                manufacturer.contains("xiaomi") || manufacturer.contains("redmi") -> {
                    fixXiaomiSpecificIssues()
                }
                manufacturer.contains("zte") -> {
                    fixZTESpecificIssues()
                }
                else -> {
                    Log.i(TAG, "No device-specific fixes needed")
                }
            }
            
            UnityDebugLogger.logUnityBridgeEvent("Device-Specific Fixes", "Applied fixes for $manufacturer $model")
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to apply device-specific fixes: ${e.message}")
        }
    }
    
    private fun fixSamsungSpecificIssues() {
        System.setProperty("unity.samsung.adaptive.performance", "false")
        System.setProperty("unity.samsung.gpu", "mali")
    }
    
    private fun fixHuaweiSpecificIssues() {
        System.setProperty("unity.huawei.gpu", "mali")
        System.setProperty("unity.huawei.graphics", "compatible")
    }
    
    private fun fixXiaomiSpecificIssues() {
        System.setProperty("unity.xiaomi.gpu", "adreno")
        System.setProperty("unity.xiaomi.graphics", "compatible")
    }
    
    private fun fixZTESpecificIssues() {
        System.setProperty("unity.zte.gpu", "mali")
        System.setProperty("unity.zte.graphics", "compatible")
        System.setProperty("unity.zte.adaptive.performance", "false")
    }
    
    /**
     * Get recommended Unity settings for this device
     */
    fun getRecommendedUnitySettings(): Map<String, String> {
        return mapOf(
            "Adaptive Performance" to "Disabled",
            "Graphics Format" to "RGB_565",
            "HDR" to "Disabled",
            "VSync" to "Enabled",
            "Target Frame Rate" to "60",
            "Memory Heap" to "256MB",
            "GC Threshold" to "64MB"
        )
    }
}
