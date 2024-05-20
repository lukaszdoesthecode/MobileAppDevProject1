package com.example.madness.connections.queries.auth

import com.example.tryingmybest.db.files.auth.AuthData

/**
 * Interface for the AuthDAO
 */
interface AuthDAO {
    /**
     * Inserts an AuthData object into the database
     * @param auth the AuthData object to insert
     */
    fun insertAuth(auth: AuthData): Boolean

    /**
     * Gets the user id of the user with the given mail and password
     * @param mail the mail of the user
     * @param password the password of the user
     * @return the user id of the user with the given mail and password
     */
    fun getAuth(mail: String, password: String): Int

    /**
     * Gets the role of the user with the given user id
     * @param userMail the mail of the user
     * @return the role of the user with the given user id
     */
    fun getRole(userMail: String): String


}