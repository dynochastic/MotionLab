package com.example.motionlab.utils

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.tasks.await
import java.util.UUID

object FirebaseStorageUtils {
    
    private val storage = FirebaseStorage.getInstance()
    private val profilePicturesRef = storage.reference.child("profile_pictures")
    
    /**
     * Upload profile picture to Firebase Storage
     * @param username The username of the user
     * @param imageUri Local URI of the image
     * @return Firebase Storage download URL
     */
    suspend fun uploadProfilePicture(username: String, imageUri: Uri): String? {
        return try {
            val filename = "${username}_${System.currentTimeMillis()}.jpg"
            val imageRef = profilePicturesRef.child(filename)
            
            android.util.Log.d("FirebaseStorageUtils", "Uploading profile picture for $username")
            android.util.Log.d("FirebaseStorageUtils", "Filename: $filename")
            android.util.Log.d("FirebaseStorageUtils", "Full path: profile_pictures/$filename")
            
            android.util.Log.d("FirebaseStorageUtils", "Starting upload to Firebase Storage...")
            val uploadTask = imageRef.putFile(imageUri).await()
            android.util.Log.d("FirebaseStorageUtils", "Upload task completed, getting download URL...")
            
            val downloadUrl = imageRef.downloadUrl.await()
            val downloadUrlString = downloadUrl.toString()
            
            android.util.Log.d("FirebaseStorageUtils", "Upload successful!")
            android.util.Log.d("FirebaseStorageUtils", "Download URL: $downloadUrlString")
            
            downloadUrlString
        } catch (e: Exception) {
            android.util.Log.e("FirebaseStorageUtils", "Upload failed for $username", e)
            android.util.Log.e("FirebaseStorageUtils", "Error type: ${e.javaClass.simpleName}")
            android.util.Log.e("FirebaseStorageUtils", "Error message: ${e.message}")
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Get profile picture download URL from Firebase Storage
     * @param username The username of the user
     * @return Firebase Storage download URL or null if not found
     */
    suspend fun getProfilePictureUrl(username: String): String? {
        return try {
            android.util.Log.d("FirebaseStorageUtils", "Getting profile picture for $username")
            
            val listResult = profilePicturesRef.listAll().await()
            android.util.Log.d("FirebaseStorageUtils", "Found ${listResult.items.size} total files in profile_pictures folder")
            
            val userFiles = listResult.items.filter { 
                it.name.startsWith("${username}_") 
            }
            
            android.util.Log.d("FirebaseStorageUtils", "Found ${userFiles.size} files for user $username")
            userFiles.forEach { file ->
                android.util.Log.d("FirebaseStorageUtils", "User file: ${file.name}")
            }
            
            if (userFiles.isNotEmpty()) {
                val latestFile = userFiles.maxByOrNull { it.name }
                val downloadUrl = latestFile?.downloadUrl?.await()?.toString()
                android.util.Log.d("FirebaseStorageUtils", "Latest file: ${latestFile?.name}")
                android.util.Log.d("FirebaseStorageUtils", "Download URL: $downloadUrl")
                downloadUrl
            } else {
                android.util.Log.d("FirebaseStorageUtils", "No profile picture found for $username")
                null
            }
        } catch (e: Exception) {
            android.util.Log.e("FirebaseStorageUtils", "Error getting profile picture for $username", e)
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Delete old profile pictures for a user (keep only the latest)
     * @param username The username of the user
     */
    suspend fun cleanupOldProfilePictures(username: String) {
        try {
            val listResult = profilePicturesRef.listAll().await()
            val userFiles = listResult.items.filter { 
                it.name.startsWith("${username}_") 
            }
            
            if (userFiles.size > 1) {
                val sortedFiles = userFiles.sortedByDescending { it.name }
                val filesToDelete = sortedFiles.drop(1)
                
                filesToDelete.forEach { fileRef ->
                    try {
                        fileRef.delete().await()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * Delete all profile pictures for a user
     * @param username The username of the user
     */
    suspend fun deleteAllProfilePictures(username: String) {
        try {
            val listResult = profilePicturesRef.listAll().await()
            val userFiles = listResult.items.filter { 
                it.name.startsWith("${username}_") 
            }
            
            userFiles.forEach { fileRef ->
                try {
                    fileRef.delete().await()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
