package server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {

    private static final String URL = "jdbc:mysql://localhost:3306/chatapp";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    public static void main(String[] args) {

        System.out.println("🚀 Program started...");

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("✔ Driver loaded");

            Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);

            System.out.println("🎉 Database Connected Successfully!");
            System.out.println(connection);

        } catch (ClassNotFoundException e) {
            System.out.println("❌ Driver not found!");
            e.printStackTrace();

        } catch (SQLException e) {
            System.out.println("❌ Database connection failed!");
            e.printStackTrace();
        }

        System.out.println("🏁 Program ended");
    }
}