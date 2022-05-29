package dariusG82.custom_exeptions;

public class ItemIsAlreadyInDatabaseException extends Exception{

    public ItemIsAlreadyInDatabaseException(){
        super("Item cannot be added, it is already in database");
    }
}
