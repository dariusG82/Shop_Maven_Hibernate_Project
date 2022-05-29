package dariusG82.custom_exeptions;

public class ClientAlreadyInDatabaseException extends Exception{

    public ClientAlreadyInDatabaseException(String clientName){
        super(String.format("Client %s is already in database", clientName));
    }
}
