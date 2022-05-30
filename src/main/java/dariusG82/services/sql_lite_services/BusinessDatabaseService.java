package dariusG82.services.sql_lite_services;

import dariusG82.data.interfaces.BusinessInterface;
import dariusG82.partners.Client;
import org.hibernate.Session;
import org.hibernate.query.Query;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

public class BusinessDatabaseService extends SQLService implements BusinessInterface {

    @Override
    public boolean isClientNameUnique(String clientName) {

        return getClientByName(clientName) == null;
    }

    @Override
    public void addNewClientToDatabase(Client client) {
        Session session = sessionFactory.openSession();

        session.beginTransaction();
        session.persist(client);
        session.getTransaction().commit();

        session.close();
    }

    @Override
    public void deleteClientFromDatabase(Client client) {
        Session session = sessionFactory.openSession();

        session.beginTransaction();
        session.delete(client);
        session.getTransaction().commit();

        session.close();
    }

    @Override
    public List<Client> getAllClients() {
        Session session = sessionFactory.openSession();

        Query<Client> clientQuery = session.createQuery("select data from Client data", Client.class);
        List<Client> clients = clientQuery.getResultList();

        session.close();

        return clients;
    }

    public Client getClientByName(String businessName) {
        Session session = sessionFactory.openSession();

        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<Client> criteriaQuery = criteriaBuilder.createQuery(Client.class);
        Root<Client> root = criteriaQuery.from(Client.class);

        criteriaQuery.select(root).where(criteriaBuilder.equal(root.get("clientName"), businessName));
        Query<Client> clientQuery = session.createQuery(criteriaQuery);

        Client client = clientQuery.getSingleResult();

        session.close();

        return client;
    }
}
