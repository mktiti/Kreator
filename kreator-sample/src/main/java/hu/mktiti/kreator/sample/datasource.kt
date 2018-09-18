package hu.mktiti.kreator.sample

import hu.mktiti.kreator.annotation.Injectable
import hu.mktiti.kreator.annotation.InjectableType
import hu.mktiti.kreator.api.injectOpt
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet

@InjectableType
interface Datasource {

    fun select(query: String, setter: PreparedStatement.() -> Unit): ResultSet?

    fun update(query: String, setter: PreparedStatement.() -> Unit)

}

@Injectable
class JdbcDatasource(
        private val connection: Connection? = injectOpt()
) : Datasource {

    override fun select(query: String, setter: PreparedStatement.() -> Unit): ResultSet? {
        if (connection == null) return null

        with (connection.prepareStatement(query)) {
            setter()
            return executeQuery()
        }
    }

    override fun update(query: String, setter: PreparedStatement.() -> Unit) {
        if (connection == null) return

        with (connection.prepareStatement(query)) {
            setter()
            executeUpdate()
        }
    }

}
