package com.example.tryingmybest.db.files.scheduled

/**
 * Data class for the scheduled vax data
 * @param vaxId: Int
 * @param vaxUserId: Int
 * @param vaxDateOfFirstDose: Date?
 * @param vaxStatus: Enum<VaxStatus>?
 */
interface VaxScheduledDAO {
    /**
     * Insert the scheduled vax data
     * @param vaxScheduled: VaxScheduledData
     * @return Boolean
     */
    fun insertVaxScheduled(vaxScheduled: VaxScheduledData): Boolean
}