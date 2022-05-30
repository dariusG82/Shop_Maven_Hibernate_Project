package dariusG82.custom_exeptions;

public class OrderDoesNotExistException extends Exception {

    public OrderDoesNotExistException(long number) {
        super("Order " + number + " doesn't exist");
    }
}
