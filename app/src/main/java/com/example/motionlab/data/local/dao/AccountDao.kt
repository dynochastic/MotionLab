package com.example.motionlab.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.motionlab.data.local.entity.Account

@Dao
interface AccountDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAccount(account: Account)

    @Query("SELECT * FROM accounts WHERE LOWER(username) = LOWER(:username) AND password = :password LIMIT 1")
    suspend fun authenticate(username: String, password: String): Account?

    @Query("SELECT * FROM accounts WHERE LOWER(username) = LOWER(:username) LIMIT 1")
    suspend fun getAccountByUsername(username: String): Account?

    // Update password for a user
    @Query("UPDATE accounts SET password = :newPassword WHERE LOWER(username) = LOWER(:username)")
    suspend fun updatePassword(username: String, newPassword: String)

    // Update profile picture URI for a user
    @Query("UPDATE accounts SET profilePictureUri = :newPictureUri WHERE LOWER(username) = LOWER(:username)")
    suspend fun updateProfilePictureUri(username: String, newPictureUri: String)
}
