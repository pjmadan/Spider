package univision.com.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;

public class DatabaseDAO {
	 
    private Connection connection;
    private Statement statement;
 
    public DatabaseDAO() { }
 
    public void createDatabase(String dbName) throws Exception  {
        String query = "CREATE DATABASE IF NOT EXISTS " + dbName;
        try {
            connection = ConnectionFactory.getConnection();
            statement = connection.createStatement();
            statement.executeUpdate(query);
            SQLWarning warning = statement.getWarnings();
            if (warning != null)
                throw new Exception(warning.getMessage());
        } catch (SQLException e) {
           
        } finally {
            DbUtil.close(statement);
            DbUtil.close(connection);
        }
    }
}
