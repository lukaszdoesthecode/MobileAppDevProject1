package com.example.tryingmybest.db.files.auth

import com.example.tryingmybest.db.files.entities.Role

/**
 * Class representing the AuthData entity
 */

data class AuthData(
    /**
     * The user id of the user
     */
    var userId: Int,

    /**
     * The mail of the user
     */
    var mail: String? = null,

    /**
     * The password of the user
     */
    var password: String? = null,

    /**
     * The role of the user
     */
    var role: String? = null
)
