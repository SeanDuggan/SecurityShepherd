import java.sql.*;
public class TestDB2 {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/?useUnicode=true&character_set_server=utf8mb4";
        String user = "root";
        String pass = "CowSaysMoo";
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            System.out.println("Driver loaded successfully");
            Connection conn = DriverManager.getConnection(url, user, pass);
            System.out.println("SUCCESS: Connected to database");
            conn.close();
        } catch (Exception e) {
            System.out.println("ERROR: " + e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}
