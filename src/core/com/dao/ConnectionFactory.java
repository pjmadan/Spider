/**
 * 
 */
package univision.com.dao;


import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import univision.com.utilities.Configuration;
 
public class ConnectionFactory {
    //static reference to itself
    private static ConnectionFactory instance = new ConnectionFactory();
    
    
    public   static String URL;
    public   static String USER ;
    public   static String PASSWORD ;
    public   static String DRIVER_CLASS;
    
    public HashMap<String, String> config = new HashMap<String, String>();
   
    //private constructor
    private ConnectionFactory() {
        try {
            //get database connections info from configuration file
    		Configuration configuration = new Configuration();
    		config = configuration.getConfigPropValues();
    		URL=config.get("URL");
    		USER=config.get("USER");
    		PASSWORD=config.get("PASSWORD");
    		DRIVER_CLASS=config.get("DRIVERCLASS");
            Class.forName(DRIVER_CLASS);
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }
    }
     
    private Connection createConnection() {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            System.out.println("ERROR: Unable to Connect to Database.");
        }
        return connection;
    }  
     
    public static Connection getConnection() {
        return instance.createConnection();
    }
}