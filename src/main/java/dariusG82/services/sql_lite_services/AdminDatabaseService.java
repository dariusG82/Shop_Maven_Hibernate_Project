package dariusG82.services.sql_lite_services;

import dariusG82.data.interfaces.AdminInterface;
import dariusG82.users.User;
import dariusG82.users.UserType;
import org.hibernate.Session;
import org.hibernate.query.Query;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

public class AdminDatabaseService extends SQLService implements AdminInterface {

    private final DataFromSQLiteService dataService;

    public AdminDatabaseService(DataFromSQLiteService dataService) {
        this.dataService = dataService;
    }

    @Override
    public boolean isUsernameUnique(String username) {
        List<User> users = dataService.getAllUsers();

        return users.stream().noneMatch(user -> user.getUsername().equals(username));
    }

    @Override
    public void addNewUser(User user){
        Session session = sessionFactory.openSession();

        session.beginTransaction();
        session.persist(user);
        session.getTransaction().commit();

        session.close();
    }

    @Override
    public void removeUser(String username) {
        User user = getUserByUsername(username);

        Session session = sessionFactory.openSession();

        session.beginTransaction();
        session.delete(user);
        session.getTransaction().commit();

        session.close();
    }

    @Override
    public User getUserByType(String username, String password, UserType type) {
        User user = getUserByUsername(username);

        if(user == null || !(user.getPassword().equals(password)) || !(user.getUserType().equals(type.toString()))){
            return null;
        }

        return user;
    }

    @Override
    public User getUserByUsername(String username) {
        Session session = sessionFactory.openSession();

        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<User> criteriaQuery = criteriaBuilder.createQuery(User.class);
        Root<User> root = criteriaQuery.from(User.class);
        criteriaQuery.select(root).where(criteriaBuilder.equal(root.get("username"),username));

        Query<User> userQuery = session.createQuery(criteriaQuery);

        List<User> users = userQuery.getResultList();

        session.close();

        return users.size() > 0 ? users.get(0) : null;
    }
}
