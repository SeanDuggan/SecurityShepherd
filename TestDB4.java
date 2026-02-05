import java.sql.*;
public class TestDB4 {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/";
        String user = "root";
        String pass = "CowSaysMoo";
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            Connection conn = DriverManager.getConnection(url, user, pass);
            System.out.println("SUCCESS: MySQL Connector 5.1.49 works with MariaDB 12.1!");
            conn.close();
        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
        }
    }
}
