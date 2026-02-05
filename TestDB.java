import java.sql.*;
public class TestDB {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/?useUnicode=true&character_set_server=utf8mb4";
        String user = "root";
        String pass = "CowSaysMoo";
        try {
            Class.forName("org.gjt.mm.mysql.Driver").newInstance();
            Connection conn = DriverManager.getConnection(url, user, pass);
            System.out.println("SUCCESS: Connected to database");
            conn.close();
        } catch (Exception e) {
            System.out.println("ERROR: " + e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}
