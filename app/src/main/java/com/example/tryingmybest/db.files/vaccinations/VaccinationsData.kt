package com.example.tryingmybest.db.files.vaccinations

/**
 * Class representing the Vaccinations entity
 */
data class VaccinationsData(
    /**
     * The id of the vax
     */
    var vaxAddInfoDataId: Int? = null,

    /**
     * The name of the vax
     */
    var noOfDoses: Int? = null,

    /**
     * The name of the vax
     */
    var timeBetweenDoses: Int? = null,
)
