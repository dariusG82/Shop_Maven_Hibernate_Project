package dariusG82.services.sql_lite_services;

import dariusG82.data.interfaces.BusinessInterface;
import dariusG82.partners.Client;
import org.hibernate.Session;

import java.util.List;

public class BusinessDatabaseService extends SQLService implements BusinessInterface {

    private final DataFromSQLiteService dataService;

    public BusinessDatabaseService(DataFromSQLiteService dataService) {
        this.dataService = dataService;
    }

    @Override
    public boolean isClientNameUnique(String clientName) {
        List<Client> allClients = dataService.getAllClients();

        return allClients.stream().noneMatch(client -> client.getClientName().equals(clientName));
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

    public Client getClientByName(String businessName) {
        List<Client> clients = dataService.getAllClients();

        return clients.stream()
                .filter(client -> client.getClientName().equals(businessName))
                .findFirst()
                .orElse(null);
    }
}
