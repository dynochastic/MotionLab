package com.example.motionlab.repository

import com.example.motionlab.data.local.dao.AccountDao
import com.example.motionlab.data.local.entity.Account

class AuthRepository(private val dao: AccountDao) {
    suspend fun signUp(account: Account) {
        dao.insertAccount(account)
    }

    suspend fun signIn(username: String, password: String): Account? {
        return dao.authenticate(username, password)
    }

    suspend fun accountExists(username: String): Boolean {
        return dao.getAccountByUsername(username) != null
    }

    suspend fun getAccountByUsername(username: String): Account? {
        return dao.getAccountByUsername(username)
    }

    suspend fun updatePassword(username: String, newPassword: String) {
        dao.updatePassword(username, newPassword)
    }

    suspend fun updateProfilePictureUri(username: String, newPictureUri: String) {
        dao.updateProfilePictureUri(username, newPictureUri)
    }
}
