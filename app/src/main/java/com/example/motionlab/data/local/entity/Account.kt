package com.example.motionlab.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "accounts")
data class Account(
    @PrimaryKey val username: String,
    val firstname: String,
    val lastname: String,
    val password: String,
    val profilePictureUri: String = "default_profile_picture_uri"
)
