package dariusG82.services.file_services;

import dariusG82.custom_exeptions.ClientDoesNotExistExeption;
import dariusG82.custom_exeptions.WrongDataPathExeption;
import dariusG82.data.interfaces.BusinessInterface;
import dariusG82.partners.Client;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static dariusG82.services.file_services.DataPath.CLIENT_PATH;

public class BusinessFileService implements BusinessInterface {

    public Client getClientByName(String name) throws ClientDoesNotExistExeption, WrongDataPathExeption {
        List<Client> clients = getAllClients();

        if (clients != null) {
            for (Client client : clients) {
                if (client.getClientName().equals(name)) {
                    return client;
                }
            }
        } else {
            throw new WrongDataPathExeption();
        }
        throw new ClientDoesNotExistExeption(name);
    }

    @Override
    public boolean isClientNameUnique(String clientName) throws WrongDataPathExeption {
        List<Client> clients = getAllClients();

        if (clients == null) {
            throw new WrongDataPathExeption();
        }

        return clients.stream().noneMatch(client -> client.getClientName().equals(clientName));
    }

    @Override
    public void addNewClientToDatabase(Client client) throws WrongDataPathExeption {
        List<Client> clients = getAllClients();

        if (clients != null) {
            clients.add(client);
            updateClientsDatabase(clients);
        } else {
            throw new WrongDataPathExeption();
        }
    }

    @Override
    public void deleteClientFromDatabase(Client clientToDelete) throws WrongDataPathExeption {
        List<Client> clients = getAllClients();


        if (clients != null) {
            if (clients.stream().anyMatch(client -> client.equals(clientToDelete))) {
                clients.remove(clientToDelete);
            }
            updateClientsDatabase(clients);
        } else {
            throw new WrongDataPathExeption();
        }
    }

    public void updateClientsDatabase(List<Client> clients) {
        try {
            PrintWriter printWriter = new PrintWriter(new FileWriter(CLIENT_PATH.getPath()));

            clients.forEach(partner -> {
                printWriter.println(partner.getClientName());
                printWriter.println(partner.getClientID());
                printWriter.println(partner.getClientStreetAddress());
                printWriter.println(partner.getClientCityAddress());
                printWriter.println(partner.getClientCountryAddress());
                printWriter.println();
            });

            printWriter.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public List<Client> getAllClients() {
        try {
            Scanner scanner = new Scanner(new File(CLIENT_PATH.getPath()));
            ArrayList<Client> allClients = new ArrayList<>();

            while (scanner.hasNext()) {
                String partnerName = scanner.nextLine();
                String businessID = scanner.nextLine();
                String streetAddress = scanner.nextLine();
                String city = scanner.nextLine();
                String country = scanner.nextLine();
                scanner.nextLine();

                allClients.add(new Client(partnerName, businessID, streetAddress, city, country));
            }

            return allClients;
        } catch (FileNotFoundException e) {
            return null;
        }
    }
}
