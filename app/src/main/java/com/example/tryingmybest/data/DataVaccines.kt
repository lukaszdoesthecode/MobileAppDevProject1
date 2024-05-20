package com.example.tryingmybest.data

/**
 * Data class representing a vaccine with name, doses, duration, description, and expandable status.
 * @property name The name of the vaccine. Defaults to an empty string.
 * @property doses The number of doses required for the vaccine. Defaults to 0.
 * @property duration The duration between doses in days. Defaults to 0.
 * @property desc The description of the vaccine. Defaults to an empty string.
 * @property expandable The expandable status of the vaccine. Defaults to false.
 */
data class DataVaccines(
    /**
     * The name of the vaccine. Defaults to an empty string.
     */
    var name: String = "",

    /**
     * The number of doses required for the vaccine. Defaults to 0.
     */
    var doses: Int = 0,

    /**
     * The duration between doses in days. Defaults to 0.
     */
    var duration: Int = 0,

    /**
     * The description of the vaccine. Defaults to an empty string.
     */
    var desc: String = "",

    /**
     * The expandable status of the vaccine. Defaults to false.
     */
    var expandable: Boolean = false
)