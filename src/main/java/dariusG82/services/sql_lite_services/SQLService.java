package dariusG82.services.sql_lite_services;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SQLService {

    final SessionFactory sessionFactory = new Configuration().configure().buildSessionFactory();

    protected Connection connectToDB() {
        Connection conn = null;
        String url = "jdbc:sqlite:c:\\Users\\konja\\DataGripProjects\\Learning\\bussines.db";
        try {
            conn = DriverManager.getConnection(url);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return conn;
    }
}
