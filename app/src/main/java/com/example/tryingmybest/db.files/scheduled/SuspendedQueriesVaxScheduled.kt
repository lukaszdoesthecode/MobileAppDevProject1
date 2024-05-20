package com.example.tryingmybest.db.files.scheduled

import android.util.Log
import com.example.tryingmybest.db.files.DBconnection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Data class for the scheduled vax data
 * @param vaxId: Int
 * @param vaxUserId: Int
 * @param vaxDateOfFirstDose: Date?
 * @param vaxStatus: Enum<VaxStatus>?
 */
object SuspendedQueriesVaxScheduled {

    /**
     * Insert the scheduled vax data
     * @param scheduledVax: VaxScheduledData
     * @return Boolean
     */
    suspend fun insertVaxScheduled(scheduledVax: VaxScheduledData): Boolean {
        return withContext(Dispatchers.IO) {  // Ensure that all operations within are executed on the IO thread
            val connection = DBconnection.getConnection() // It's safe to open connection here because we are already on the background thread

            try {
                val scheduledQueries = DBqueriesVaxScheduled(connection)
                val insertResult = scheduledQueries.insertVaxScheduled(scheduledVax) // Perform the insert operation
                Log.d("DatabaseOperations", "Insert operation performed. Result: $insertResult")

                insertResult  // Return the result of the insert operation
            } catch (e: Exception) {
                Log.e("DatabaseOperations", "Error inserting scheduled vaccination: ${e.message}", e)
                false  // Return false if there is an exception
            } finally {
                try {
                    connection.close()  // Ensure the connection is closed after the operation
                    Log.d("DatabaseOperations", "Database connection closed.")
                } catch (e: Exception) {
                    Log.e("DatabaseOperations", "Error closing database connection: ${e.message}", e)
                }
            }
        }
    }
}
