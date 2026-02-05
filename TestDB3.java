import java.sql.*;
public class TestDB3 {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/?useUnicode=true&characterEncoding=utf8&useOldAliasMetadataBehavior=true&zeroDateTimeBehavior=convertToNull";
        String user = "root";
        String pass = "CowSaysMoo";
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            Connection conn = DriverManager.getConnection(url, user, pass);
            System.out.println("SUCCESS: Connected to database!");
            conn.close();
        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
