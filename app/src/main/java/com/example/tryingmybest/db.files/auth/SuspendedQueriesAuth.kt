package com.example.madness.connections.queries.auth

import android.util.Log
import com.example.tryingmybest.db.files.DBconnection
import com.example.tryingmybest.db.files.auth.AuthData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.SQLException

/**
 * Class representing the suspended queries for the AuthDAO
 */
object SuspendedQueriesAuth {
    private const val TAG = "SuspendedQueriesAuth"

    /**
     * Inserts an AuthData object into the database
     * @param auth the AuthData object to insert
     * @return true if the insert operation was successful, false otherwise
     */
    suspend fun insertAuth(auth: AuthData): Boolean {
        return withContext(Dispatchers.IO) {  // Ensure that all operations within are executed on the IO thread
            val connection = try {
                DBconnection.getConnection()  // Attempt to open a database connection
            } catch (e: Exception) {
                Log.e(TAG, "Failed to obtain database connection: ${e.message}", e)
                return@withContext false  // Return early with false if connection fails
            }

            try {
                val authQueries = DBqueriesAuth(connection)
                authQueries.insertAuth(auth)  // Perform the insert operation
                true  // Return true to indicate success
            } catch (e: SQLException) {
                Log.e(TAG, "SQLException occurred while inserting auth: ${e.message}", e)
                false  // Return false to indicate SQL failure
            } finally {
                try {
                    connection.close()  // Attempt to close the database connection
                } catch (e: SQLException) {
                    Log.e(TAG, "SQLException during connection close: ${e.message}", e)
                } catch (e: Exception) {
                    Log.e(TAG, "Exception during connection close: ${e.message}", e)
                }
            }
        }
    }

    /**
     * Gets the user id of the user with the given mail and password
     * @param mail the mail of the user
     * @param password the password of the user
     * @return the user id of the user with the given mail and password
     */
    suspend fun getAuth(mail: String, password: String): Int {
        return withContext(Dispatchers.IO) {  // Ensures execution on the IO dispatcher
            val connection = try {
                DBconnection.getConnection()  // Attempt to open a database connection
            } catch (e: Exception) {
                Log.e(TAG, "Failed to obtain database connection: ${e.message}", e)
                return@withContext -1  // Return -1 if connection fails
            }

            try {
                val query = "CALL get_auth(?, ?)"
                val preparedStatement = connection.prepareCall(query)
                preparedStatement.setString(1, mail)
                preparedStatement.setString(2, password)

                val resultSet = preparedStatement.executeQuery()
                if (resultSet.next()) {
                    resultSet.getInt("user_id")  // Return the user ID if found
                } else {
                    -1  // Return -1 if no record is found
                }
            }  catch (e: Exception) {
                Log.e(TAG, "General exception occurred while fetching auth: ${e.message}", e)
                -1  // Return -1 to indicate general failure
            } finally {
                try {
                    connection.close()  // Attempt to close the database connection
                } catch (e: Exception) {
                    Log.e(TAG, "Exception during connection close: ${e.message}", e)
                }
            }
        }
    }

    /**
     * Gets the role of the user with the given user id
     * @param userMail the mail of the user
     * @return the role of the user with the given user id
     */
    suspend fun getRole(userMail: String): String{
        return withContext(Dispatchers.IO) {
            val connection = try {
                DBconnection.getConnection()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to obtain database connection: ${e.message}", e)
                return@withContext ""
            }

            try {
                val query = "CALL get_auth_role(?)"
                val preparedStatement = connection.prepareCall(query)
                preparedStatement.setString(1, userMail)

                val resultSet = preparedStatement.executeQuery()
                if (resultSet.next()) {
                    resultSet.getString("role")
                } else {
                    ""
                }
            } catch (e: SQLException) {
                Log.e(TAG, "SQLException occurred while fetching role: ${e.message}", e)
                ""
            }finally {
                try {
                    connection.close()
                    Log.d(TAG, "Connection closed successfully")
                } catch (e: Exception) {
                    Log.e(TAG, "Exception during connection close: ${e.message}", e)
                }
            }
        }
    }

}

