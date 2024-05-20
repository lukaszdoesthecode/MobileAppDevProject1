package com.example.madness.connections.queries.vacxaddinfo

import com.example.tryingmybest.db.files.vacxaddinfo.VaxAddInfoData

/**
 * Interface representing the possible queries for the VaxAddInfo entity
 */
interface VaxAddInfoDAO {
    /**
     * Insert the vax add info
     * @param vaxAddInfo: VaxAddInfoData
     * @return Boolean
     */
    fun insertVaxInfo(vaxAddInfo: VaxAddInfoData): Boolean

    /**
     * Delete the vax add info
     * @param vaxId: Int
     * @return Boolean
     */
    fun deleteVaxInfo(vaxId: Int): Boolean

    /**
     * Get the vax add info
     * @param vaxId: Int
     * @return VaxAddInfoData
     */
    fun getVaxAddInfo(vaxId: Int): VaxAddInfoData

    /**
     * Get the vax id
     * @param vaxNameCompany: String
     * @return Int
     */
    fun getVaxId(vaxNameCompany: String): Int

    /**
     * Get all the vax add info
     * @return Set<VaxAddInfoData>
     */
    fun getAllVaxAddInfo(): Set<VaxAddInfoData>
}