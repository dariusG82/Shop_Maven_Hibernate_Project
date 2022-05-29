package dariusG82.custom_exeptions;

public class DatabaseOperationFailureException extends Exception{
    public DatabaseOperationFailureException() {
        super("Database update fail, last operation cancelled");
    }
}
