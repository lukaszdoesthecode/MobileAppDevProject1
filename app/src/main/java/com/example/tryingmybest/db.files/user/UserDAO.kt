package com.example.madness.connections.queries.user

import com.example.tryingmybest.db.files.user.UserData

/**
 * Interface representing the possible queries for the User entity

 */
interface UserDAO {
    /**
     * Insert the user data
     * @param user: UserData
     * @return Boolean
     */
    fun insertUser(user: UserData): Boolean

    /**
     * Get the user id
     * @param username: String
     * @return Int
     */
    fun getUserId(username: String): Int

    /**
     * Get the user data
     * @param userId: Int
     * @return UserData
     */
    fun getAllUsers(): List<UserData>

    /**
     * Get the user data
     * @param userId: Int
     * @return UserData
     */
    fun deleteUser(userMail: String): Boolean
}