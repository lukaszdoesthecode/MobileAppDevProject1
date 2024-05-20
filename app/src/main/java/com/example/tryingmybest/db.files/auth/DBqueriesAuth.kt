package com.example.madness.connections.queries.auth

import com.example.tryingmybest.db.files.auth.AuthData
import java.sql.Connection

/**
 * Class representing the DBqueriesAuth in suspended forms for the AuthDAO.
 */
class DBqueriesAuth(private val connection: Connection): AuthDAO {
    /**
     * Inserts an AuthData object into the database
     * @param auth the AuthData object to insert
     */
    override fun insertAuth(auth: AuthData): Boolean {
        val query = "CALL insert_auth(?, ?, ?, ?)"
        val preparedStatement = connection.prepareCall(query)

        preparedStatement.setInt(1, auth.userId)
        preparedStatement.setString(2, auth.mail)
        preparedStatement.setString(3, auth.password)
        preparedStatement.setString(4, auth.role)

        val result = !preparedStatement.execute()
        preparedStatement.close()

        return result

    }

    /**
     * Gets the user id of the user with the given mail and password
     * @param mail the mail of the user
     * @param password the password of the user
     * @return the user id of the user with the given mail and password
     */
    override fun getAuth(mail: String, password: String): Int {
        val query = "CALL get_auth(?, ?)"
        val preparedStatement = connection.prepareCall(query)
        preparedStatement.setString(1, mail)
        preparedStatement.setString(2, password)

        val resultSet = preparedStatement.executeQuery()
        return if (resultSet.next()) {
            resultSet.getInt("user_id")
        } else {
            -1
        }
    }

    /**
     * Gets the role of the user with the given user id
     * @param userMail the mail of the user
     * @return the role of the user with the given user id
     */
    override fun getRole(userMail: String): String {
        val query = "CALL get_auth_role(?)"
        val preparedStatement = connection.prepareCall(query)
        preparedStatement.setString(1, userMail)

        val resultSet = preparedStatement.executeQuery()
        return if (resultSet.next()) {
            resultSet.getString("role")
        } else {
            ""
        }
    }

}