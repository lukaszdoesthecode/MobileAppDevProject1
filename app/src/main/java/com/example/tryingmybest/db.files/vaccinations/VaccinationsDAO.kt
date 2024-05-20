package com.example.madness.connections.vaccinations

import com.example.tryingmybest.db.files.vaccinations.VaccinationsData

/**
 * Interface representing the possible queries for the Vaccinations entity
 */
interface VaccinationsDAO {
    /**
     * Insert the vax data
     * @param vax: VaccinationsData
     * @return Boolean
     */
    fun insertVaccination(vax: VaccinationsData): Boolean

    /**
     * Delete the vax data
     * @param vaxId: Int
     * @return Boolean
     */
    fun deleteVaccination(vaxId: Int): Boolean

    /**
     * Get the number of doses
     * @param vaxId: Int
     * @return Int
     */
    fun getNumberOfDoses(vaxId: Int): Int

    /**
     * Get the time between doses
     * @param vaxId: Int
     * @return Int
     */
    fun getTimeBetweenDoses(vaxId: Int): Int

    /**
     * Get the vax data
     * @param vaxId: Int
     * @return VaccinationsData
     */
    fun getVaccination(vaxId: Int): VaccinationsData?

    /**
     * Get all the vax data
     * @return Set<VaccinationsData?>
     */
    fun getAllVaccinations(): Set<VaccinationsData?>?

    /**
     * Get all the vax data and add info
     * @return Set<VaccinationsData?>
     */
    fun getAllVaccinationAndAddInfo(): Set<VaccinationsData?>?

    /**
     * Get the vax id by name
     * @return Int
     */
    fun getVaxIdByName(): Int

    /**
     * Update the vax data
     * @param vaxId: Int
     * @param vaxName: String
     * @param noOfDoses: Int
     * @param timeBetweenDoses: Int
     * @return Boolean
     */
    fun updateVaxAddInfo(vaxName: String, noOfDoses: Int, timeBetweenDoses: Int, description: String): Boolean

    /**
     * Get the upcoming vax
     * @param userId: Int
     * @return Set<VaccinationsData?>
     */
    fun getUpcomingVax(userId: Int): Set<VaccinationsData?>?

    /**
     * Get the history vax
     * @param userId: Int
     * @return Set<VaccinationsData?>
     */
    fun getHistoryVax(userId: Int): Set<VaccinationsData?>?
}