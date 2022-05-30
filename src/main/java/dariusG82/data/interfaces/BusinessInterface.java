package dariusG82.data.interfaces;

import dariusG82.custom_exeptions.ClientDoesNotExistExeption;
import dariusG82.custom_exeptions.WrongDataPathExeption;
import dariusG82.partners.Client;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public interface BusinessInterface {

    List<Client> getAllClients();

    Client getClientByName(String name) throws ClientDoesNotExistExeption, WrongDataPathExeption, SQLException;

    void addNewClientToDatabase(Client client) throws IOException, WrongDataPathExeption, SQLException;

    void deleteClientFromDatabase(Client client) throws WrongDataPathExeption, ClientDoesNotExistExeption, SQLException;

    boolean isClientNameUnique(String clientName) throws WrongDataPathExeption;
}
