package com.example.motionlab.utils

object PasswordValidator {
    
    /**
     * Validates if password meets minimum requirements:
     * - At least 8 characters
     * - Contains at least one special character
     */
    fun isValidPassword(password: String): Boolean {
        return password.length >= 8 && containsSpecialCharacter(password)
    }
    
    /**
     * Checks if password contains at least one special character
     */
    private fun containsSpecialCharacter(password: String): Boolean {
        val specialChars = "!@#$%^&*()_+-=[]{}|;':\",./<>?~`"
        return password.any { it in specialChars }
    }
    
    /**
     * Gets validation error message for password
     */
    fun getPasswordErrorMessage(password: String): String? {
        return when {
            password.length < 8 -> "Password must be at least 8 characters long"
            !containsSpecialCharacter(password) -> "Password must contain at least one special character (!@#$%^&*()_+-=[]{}|;':\",./<>?~`)"
            else -> null
        }
    }
    
    /**
     * Gets password strength feedback
     */
    fun getPasswordStrength(password: String): PasswordStrength {
        return when {
            password.isEmpty() -> PasswordStrength.EMPTY
            password.length < 8 -> PasswordStrength.WEAK
            password.length >= 8 && !containsSpecialCharacter(password) -> PasswordStrength.MEDIUM
            password.length >= 8 && containsSpecialCharacter(password) -> PasswordStrength.STRONG
            else -> PasswordStrength.WEAK
        }
    }
}

enum class PasswordStrength {
    EMPTY,
    WEAK,
    MEDIUM,
    STRONG
}
