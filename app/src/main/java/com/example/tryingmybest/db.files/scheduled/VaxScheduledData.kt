package com.example.tryingmybest.db.files.scheduled

import com.example.tryingmybest.db.files.entities.VaxStatus
import java.sql.Date

/**
 * Data class for the scheduled vax data
 * @param vaxId: Int
 * @param vaxUserId: Int
 * @param vaxDateOfFirstDose: Date?
 * @param vaxStatus: Enum<VaxStatus>?
 */
data class VaxScheduledData(
    /**
     * The id of the vax
     */
    var vaxId: Int,

    /**
     * The id of the user
     */
    var vaxUserId: Int,

    /**
     * The date of the first dose
     */
    var vaxDateOfFirstDose: Date? = null,

    /**
     * The status of the vax
     */
    var vaxStatus: Enum<VaxStatus>? = null
)