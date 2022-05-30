package dariusG82.services.sql_lite_services;

import dariusG82.custom_exeptions.UserNotFoundException;
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

    @Override
    public boolean isUsernameUnique(String username) {

        return getUserByUsername(username) == null;
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
    public List<User> getAllUsers() {
        Session session = sessionFactory.openSession();

        Query<User> userQuery = session.createQuery("select data from User data", User.class);
        List<User> users = userQuery.getResultList();

        session.close();

        return users;
    }

    @Override
    public User getUser(String username, String password, UserType type) throws UserNotFoundException {
        Session session = sessionFactory.openSession();

        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<User> criteriaQuery = criteriaBuilder.createQuery(User.class);
        Root<User> root = criteriaQuery.from(User.class);
        criteriaQuery.select(root).where(criteriaBuilder.and(
                criteriaBuilder.equal(root.get("username"),username),
                criteriaBuilder.equal(root.get("password"), password),
                criteriaBuilder.equal(root.get("userType"), type.toString())
        ));

        Query<User> userQuery = session.createQuery(criteriaQuery);

        User user = userQuery.getSingleResult();

        session.close();

        if(user != null){
            return user;
        } else {
            throw new UserNotFoundException();
        }
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
