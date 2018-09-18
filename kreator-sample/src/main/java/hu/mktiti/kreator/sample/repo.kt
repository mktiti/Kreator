package hu.mktiti.kreator.sample

import hu.mktiti.kreator.annotation.Injectable
import hu.mktiti.kreator.annotation.InjectableType
import hu.mktiti.kreator.api.inject
import java.sql.ResultSet

data class User(val id: Long, val name: String, val email: String, val password: String)

@InjectableType
abstract class Repo<T>(
        val tableName: String,
        val datasource: Datasource = inject()
) {

    abstract fun convertRow(set: ResultSet): T

    fun findOne(query: String): T? {
        val resultSet = datasource.select(query) {} ?: return null

        if (resultSet.next()) {
            return convertRow(resultSet)
        }

        return null
    }

    fun listAll(): List<T> {
        val resultSet = datasource.select("SELECT * FROM $tableName") {} ?: return emptyList()

        val results = ArrayList<T>(resultSet.fetchSize)
        while (resultSet.next()) {
            results.add(convertRow(resultSet))
        }

        return results
    }

    fun dropAll() {
        datasource.update("DELETE FROM $tableName") {}
    }

}

@Injectable
class UserRepo(datasource: Datasource = inject()) : Repo<User>("user", datasource) {

    override fun convertRow(set: ResultSet): User = User(set.getLong("id"), set.getString("name"), set.getString("email"), set.getString("password"))

    fun authenticate(username: String, password: String): Boolean {
        datasource.update("update") {}
        return true
    }

}