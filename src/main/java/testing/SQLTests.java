package testing;

import java.sql.*;

import static dariusG82.services.sql_lite_services.SQL_Query.*;

public class SQLTests {


    public SQLTests() {

    }

    public void connect() {
        Connection conn = null;
        try {
            String url = "jdbc:sqlite:c:\\Users\\konja\\DataGripProjects\\Learning\\bussines.db";
            conn = DriverManager.getConnection(url);
            System.out.println("Connection started");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                    System.out.println("Connection terminated");
                }
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private Connection connectToDB() {
        Connection conn = null;
        String url = "jdbc:sqlite:c:\\Users\\konja\\DataGripProjects\\Learning\\bussines.db";
        try {
            conn = DriverManager.getConnection(url);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return conn;
    }

    public String getUserPassword(String username) {
        String query = SELECT_USER_BY_USERNAME.getQuery() + "'" + username + "'";

        try {
            Connection connection = this.connectToDB();
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);

            return resultSet.getString("password");
        } catch (SQLException e) {
            return null;
        }
    }
}
