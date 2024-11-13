import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class databaseconnect {
    public static Connection connect() {
        Connection conn = null;
        try {
            String url = "jdbc:mysql://localhost:3306/bengkel";
            String user = "root";
            String password = ""; // sesuaikan dengan password XAMPP Anda
            conn = DriverManager.getConnection(url, user, password);
            System.out.println("Connected to the database.");
        } catch (SQLException e) {
            System.out.println("Connection failed.");
            e.printStackTrace();
        }
        return conn;
    }
}
