package com.example.tryingmybest.db.files.vacxaddinfo


/**
 * Class representing the VaxAddInfo entity
 **/

data class VaxAddInfoData(
    /**
     * The id of the vax
     */
    var vaxId: Int? = null,

    /**
     * The name of the vax
     */
    var vaxNameCompany: String? = null,

    /**
     * The description of the vax
     */
    var vaxDescription: String? = null
)
