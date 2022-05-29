package dariusG82.custom_exeptions;

public class OrderDoesNotExistException extends Exception {

    public OrderDoesNotExistException(int number) {
        super("Order " + number + " doesn't exist");
    }
}
