
package gt.taller.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class Database {

    private static final String URL =
            "jdbc:oracle:thin:@tallerdb_high";

    private static final String USER = "ADMIN";

    private static final String PASSWORD =
            "Yuleidy1234.";

    
       static {
    System.setProperty(
        "oracle.net.tns_admin",
        "C:/OracleTaller/wallet"
    );
}
  
    private Database() {
    }

    public static Connection getConnection()
            throws SQLException {

        return DriverManager.getConnection(
                URL, USER, PASSWORD
        );
    }

    public static void testConnection()
            throws SQLException {

        try (Connection connection = getConnection()) {
            if (!connection.isValid(10)) {
                throw new SQLException(
                        "La conexión con Oracle no es válida."
                );
            }
        }
    }
}
