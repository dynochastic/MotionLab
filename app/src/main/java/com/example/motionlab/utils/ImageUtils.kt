package com.example.motionlab.utils

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID

object ImageUtils {
    
    /**
     * Copy image from URI to app's internal storage and return the local file path
     */
    fun copyImageToInternalStorage(context: Context, imageUri: Uri): String? {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(imageUri)
            inputStream?.use { input ->
                val filename = "profile_${UUID.randomUUID()}.jpg"
                val file = File(context.filesDir, filename)
                
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
                
                file.absolutePath
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Get the profile image file path for a user
     */
    fun getProfileImagePath(context: Context, username: String): String? {
        val filesDir = context.filesDir
        val profileDir = File(filesDir, "profiles")
        
        if (!profileDir.exists()) {
            return null
        }
        
        val userProfileDir = File(profileDir, username)
        if (userProfileDir.exists()) {
            val imageFiles = userProfileDir.listFiles { file ->
                file.isFile && (file.extension.equals("jpg", ignoreCase = true) || 
                               file.extension.equals("jpeg", ignoreCase = true) || 
                               file.extension.equals("png", ignoreCase = true))
            }
            
            return imageFiles?.firstOrNull()?.absolutePath
        }
        
        return null
    }
    
    /**
     * Save profile image to user-specific directory
     */
    fun saveProfileImage(context: Context, username: String, imageUri: Uri): String? {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(imageUri)
            inputStream?.use { input ->
                val filesDir = context.filesDir
                val profileDir = File(filesDir, "profiles")
                val userProfileDir = File(profileDir, username)
                
                if (!userProfileDir.exists()) {
                    userProfileDir.mkdirs()
                }
                
                userProfileDir.listFiles()?.forEach { it.delete() }
                
                val filename = "profile_${System.currentTimeMillis()}.jpg"
                val file = File(userProfileDir, filename)
                
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
                
                file.absolutePath
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Delete old profile images to save space
     */
    fun cleanupOldProfileImages(context: Context, username: String) {
        try {
            val filesDir = context.filesDir
            val profileDir = File(filesDir, "profiles")
            val userProfileDir = File(profileDir, username)
            
            if (userProfileDir.exists()) {
                val imageFiles = userProfileDir.listFiles { file ->
                    file.isFile && (file.extension.equals("jpg", ignoreCase = true) || 
                                   file.extension.equals("jpeg", ignoreCase = true) || 
                                   file.extension.equals("png", ignoreCase = true))
                }
                
                imageFiles?.sortedByDescending { it.lastModified() }
                    ?.drop(1)
                    ?.forEach { it.delete() }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
