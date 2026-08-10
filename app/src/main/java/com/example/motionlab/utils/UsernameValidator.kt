package com.example.motionlab.utils

object UsernameValidator {
    
    private const val MIN_LENGTH = 3
    private const val MAX_LENGTH = 20
    
    /**
     * Validates if username meets requirements:
     * - Between 3-20 characters
     * - Only alphanumeric characters and underscores
     * - Cannot start or end with underscore
     * - No consecutive underscores
     */
    fun isValidUsername(username: String): Boolean {
        return username.length in MIN_LENGTH..MAX_LENGTH &&
                isValidFormat(username) &&
                !startsWithUnderscore(username) &&
                !endsWithUnderscore(username) &&
                !hasConsecutiveUnderscores(username)
    }
    
    /**
     * Checks if username contains only valid characters (alphanumeric and underscore)
     */
    private fun isValidFormat(username: String): Boolean {
        return username.all { it.isLetterOrDigit() || it == '_' }
    }
    
    /**
     * Checks if username starts with underscore
     */
    private fun startsWithUnderscore(username: String): Boolean {
        return username.startsWith("_")
    }
    
    /**
     * Checks if username ends with underscore
     */
    private fun endsWithUnderscore(username: String): Boolean {
        return username.endsWith("_")
    }
    
    /**
     * Checks if username has consecutive underscores
     */
    private fun hasConsecutiveUnderscores(username: String): Boolean {
        return username.contains("__")
    }
    
    /**
     * Gets validation error message for username
     */
    fun getUsernameErrorMessage(username: String): String? {
        return when {
            username.length < MIN_LENGTH -> "Username must be at least $MIN_LENGTH characters long"
            username.length > MAX_LENGTH -> "Username must be no more than $MAX_LENGTH characters long"
            !isValidFormat(username) -> "Username can only contain letters, numbers, and underscores"
            startsWithUnderscore(username) -> "Username cannot start with underscore"
            endsWithUnderscore(username) -> "Username cannot end with underscore"
            hasConsecutiveUnderscores(username) -> "Username cannot have consecutive underscores"
            else -> null
        }
    }
    
    /**
     * Gets username strength feedback
     */
    fun getUsernameStrength(username: String): UsernameStrength {
        return when {
            username.isEmpty() -> UsernameStrength.EMPTY
            username.length < MIN_LENGTH -> UsernameStrength.TOO_SHORT
            username.length > MAX_LENGTH -> UsernameStrength.TOO_LONG
            !isValidUsername(username) -> UsernameStrength.INVALID_FORMAT
            else -> UsernameStrength.VALID
        }
    }
    
    /**
     * Gets character count for display
     */
    fun getCharacterCount(username: String): String {
        return "${username.length}/$MAX_LENGTH"
    }
}

enum class UsernameStrength {
    EMPTY,
    TOO_SHORT,
    TOO_LONG,
    INVALID_FORMAT,
    VALID
}

