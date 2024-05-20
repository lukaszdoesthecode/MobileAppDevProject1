package com.example.madness.connections.vaccinations

import com.example.tryingmybest.db.files.vaccinations.VaccinationsData
import java.sql.Connection

class DBqueriesVaccinations(private val connection: Connection): VaccinationsDAO{
    /**
     * Inserts a new vaccination into the database
     * @param vax the vaccination to be inserted
     * @return true if the vaccination was inserted successfully, false otherwise
     */
    override fun insertVaccination(vax: VaccinationsData): Boolean {
        val query = "CALL insert_vaccination(?, ?, ?)"
        val preparedStatement = connection.prepareCall(query)
        vax.vaxAddInfoDataId?.let { preparedStatement.setInt(1, it) }
        vax.noOfDoses?.let { preparedStatement.setInt(2, it) }
        vax.timeBetweenDoses?.let { preparedStatement.setInt(3, it) }

        val result = !preparedStatement.execute()
        preparedStatement.close()

        return result
    }

    /**
     * Deletes a vaccination from the database
     * @param vaxId the id of the vaccination to be deleted
     * @return true if the vaccination was deleted successfully, false otherwise
     */
    override fun deleteVaccination(vaxId: Int): Boolean {
        val query = "CALL delete_vaccination(?)"
        val preparedStatement = connection.prepareCall(query)
        preparedStatement.setInt(1, vaxId)

        return preparedStatement.executeUpdate() > 0
    }

    /**
     * Gets the number of doses of a vaccination
     * @param vaxId the id of the vaccination
     * @return the number of doses of the vaccination
     */
    override fun getNumberOfDoses(vaxId: Int): Int {
        val query = "CALL get_no_of_doses(?)"
        connection.prepareCall(query).use { preparedStatement ->
            preparedStatement.setInt(1, vaxId)
            val resultSet = preparedStatement.executeQuery()
            if (resultSet.next()) {
                return resultSet.getInt("vax_no_of_doses")
            } else {
                throw NoSuchElementException("Vaccination not found with vaxId: $vaxId")
            }
        }
    }

    /**
     * Gets the time between doses of a vaccination
     * @param vaxId the id of the vaccination
     * @return the time between doses of the vaccination
     */
    override fun getTimeBetweenDoses(vaxId: Int): Int {
        val query = "CALL get_time_between_doses(?)"
        connection.prepareCall(query).use { preparedStatement ->
            preparedStatement.setInt(1, vaxId)
            val resultSet = preparedStatement.executeQuery()
            if (resultSet.next()) {
                return resultSet.getInt("time_between_doses")
            } else {
                throw NoSuchElementException("Vaccination not found with vaxId: $vaxId")
            }
        }
    }

    /**
     * Gets a vaccination given its id
     * @param vaxId the id of the vaccination
     * @return the vaccination with the given id
     */
    override fun getVaccination(vaxId: Int): VaccinationsData? {
        val query = "CALL get_vaccination(?)"
        connection.prepareCall(query).use { preparedStatement ->
            preparedStatement.setInt(1, vaxId)
            val resultSet = preparedStatement.executeQuery()
            if (resultSet.next()) {
                return VaccinationsData(
                    resultSet.getInt("vax_id"),
                    resultSet.getInt("no_of_doses"),
                    resultSet.getInt("time_between_doses")
                )
            } else {
                throw NoSuchElementException("Vaccination not found with vaxId: $vaxId")
            }
        }
    }

    /**
     * Gets all the vaccinations in the database
     * @return a set with all the vaccinations in the database
     */
    override fun getAllVaccinations(): Set<VaccinationsData?>? {
        val query = "CALL get_all_vaccinations()"
        connection.prepareCall(query).use { preparedStatement ->
            val resultSet = preparedStatement.executeQuery()
            val vaccinationsData = mutableSetOf<VaccinationsData>()
            while (resultSet.next()) {
                val vaxId = resultSet.getInt("vax_id")
                val noOfDoses = resultSet.getInt("no_of_doses")
                val timeBetweenDoses = resultSet.getInt("time_between_doses")
                val vax = VaccinationsData(vaxId, noOfDoses, timeBetweenDoses)
                vaccinationsData.add(vax)
            }
            return vaccinationsData
        }
    }

    /**
     * Gets all the vaccinations in the database with additional information
     * @return a set with all the vaccinations in the database with additional information
     */
    override fun getAllVaccinationAndAddInfo(): Set<VaccinationsData?>? {
        val query = "CALL get_all_vaccinations_and_add_info()"
        connection.prepareCall(query).use { preparedStatement ->
            val resultSet = preparedStatement.executeQuery()
            val vaccinationsData = mutableSetOf<VaccinationsData>()
            while (resultSet.next()) {
                val vaxId = resultSet.getInt("vax_id")
                val noOfDoses = resultSet.getInt("no_of_doses")
                val timeBetweenDoses = resultSet.getInt("time_between_doses")
                val vax = VaccinationsData(vaxId, noOfDoses, timeBetweenDoses)
                vaccinationsData.add(vax)
            }
            return vaccinationsData
        }
    }

    /**
     * Gets the id of a vaccination given its name
     * @return the id of the vaccination
     */
    override fun getVaxIdByName(): Int {
        val query = "CALL get_vax_id_by_name(?)"
        connection.prepareCall(query).use { preparedStatement ->
            val resultSet = preparedStatement.executeQuery()
            if (resultSet.next()) {
                return resultSet.getInt("vax_id")
            } else {
                throw NoSuchElementException("Vaccination not found with vaxId")
            }
        }
    }

    /**
     * Updates the data of a vaccination
     * @param vaxId the id of the vaccination
     * @param vaxName the name of the vaccination
     * @param noOfDoses the number of doses of the vaccination
     * @param timeBetweenDoses the time between doses of the vaccination
     * @return true if the vaccination was updated successfully, false otherwise
     */
    override fun updateVaxAddInfo(
        vaxName: String,
        noOfDoses: Int,
        timeBetweenDoses: Int,
        description: String
    ): Boolean {
        val query = "CALL update_vax_add_info(?, ?, ?, ?)"
        val preparedStatement = connection.prepareCall(query)
        preparedStatement.setString(1, vaxName)
        preparedStatement.setInt(2, noOfDoses)
        preparedStatement.setInt(3, timeBetweenDoses)
        preparedStatement.setString(4, description)

        return preparedStatement.executeUpdate() > 0
    }

    /**
     * Gets the upcoming vaccinations for a user
     * @param userId the id of the user
     * @return a set with the upcoming vaccinations for the user
     */
    override fun getUpcomingVax(userId: Int): Set<VaccinationsData?>? {
        val query = "CALL get_upcoming_vax(?)"
        connection.prepareCall(query).use { preparedStatement ->
            preparedStatement.setInt(1, userId)
            val resultSet = preparedStatement.executeQuery()
            val vaccinationsData = mutableSetOf<VaccinationsData>()
            while (resultSet.next()) {
                val vaxId = resultSet.getInt("vax_id")
                val noOfDoses = resultSet.getInt("no_of_doses")
                val timeBetweenDoses = resultSet.getInt("time_between_doses")
                val vax = VaccinationsData(vaxId, noOfDoses, timeBetweenDoses)
                vaccinationsData.add(vax)
            }
            return vaccinationsData
        }
    }

    /**
     * Gets the history of vaccinations for a user
     * @param userId the id of the user
     * @return a set with the history of vaccinations for the user
     */
    override fun getHistoryVax(userId: Int): Set<VaccinationsData?>? {
        val query = "CALL get_history_vax(?)"
        connection.prepareCall(query).use { preparedStatement ->
            preparedStatement.setInt(1, userId)
            val resultSet = preparedStatement.executeQuery()
            val vaccinationsData = mutableSetOf<VaccinationsData>()
            while (resultSet.next()) {
                val vaxId = resultSet.getInt("vax_id")
                val noOfDoses = resultSet.getInt("no_of_doses")
                val timeBetweenDoses = resultSet.getInt("time_between_doses")
                val vax = VaccinationsData(vaxId, noOfDoses, timeBetweenDoses)
                vaccinationsData.add(vax)
            }
            return vaccinationsData
        }
    }
}