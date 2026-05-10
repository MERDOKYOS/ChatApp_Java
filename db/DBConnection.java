package db;

import java.sql.Connection;
import java.sql.DriverManager;

public class DBConnection {

    public static Connection getConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
    
            Connection con = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/chatapp",
                    "root",
                    ""
            );
    
            return con;
    
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("❌ DB CONNECTION FAILED");
            return null;
        }
    }
}