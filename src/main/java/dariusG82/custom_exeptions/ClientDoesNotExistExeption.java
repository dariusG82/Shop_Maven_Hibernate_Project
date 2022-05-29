package dariusG82.custom_exeptions;

public class ClientDoesNotExistExeption extends Exception {

    public ClientDoesNotExistExeption(String clientName) {
        super(String.format("Client %s is not in database, ask your accountant to add new client\n", clientName));
    }
}
