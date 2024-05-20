package com.example.tryingmybest.db.files.scheduled

import android.util.Log

/**
 * Data class for the scheduled vax data
 * @param vaxId: Int
 * @param vaxUserId: Int
 * @param vaxDateOfFirstDose: Date?
 * @param vaxStatus: Enum<VaxStatus>?
 */
class DBqueriesVaxScheduled(private val connection: java.sql.Connection) : VaxScheduledDAO {

    /**
     * Insert the scheduled vax data
     * @param vaxScheduled: VaxScheduledData
     * @return Boolean
     */
    override fun insertVaxScheduled(vaxScheduled: VaxScheduledData): Boolean {
        val query = "CALL insert_vax_scheduled(?, ?, ?, ?)"

        val preparedStatement = connection.prepareCall(query)
        try {
            Log.d("DatabaseOperations", "Setting parameters for the query.")
            preparedStatement.setInt(1, vaxScheduled.vaxId)
            preparedStatement.setInt(2, vaxScheduled.vaxUserId)
            preparedStatement.setDate(3, vaxScheduled.vaxDateOfFirstDose)
            preparedStatement.setString(4, vaxScheduled.vaxStatus.toString())

            val result = !preparedStatement.execute()

            return result
        } catch (e: Exception) {
                Log.e("DatabaseOperations", "Error executing query: ${e.message}", e)
            return false
        } finally {
            try {
                preparedStatement.close()
            } catch (e: Exception) {
                Log.e("DatabaseOperations", "Error closing PreparedStatement: ${e.message}", e)
            }
        }
    }
}
