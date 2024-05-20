package com.example.tryingmybest.data

/**
 * Data class representing a user with email and username.
 * @property email The email of the user. Defaults to an empty string.
 * @property username The username of the user. Defaults to an empty string.
 */

data class DataUser(
    /**
     * The email of the user. Defaults to an empty string.
     */
    val email: String = "",

    /**
     * The username of the user. Defaults to an empty string.
     */
    val username: String = ""
)
