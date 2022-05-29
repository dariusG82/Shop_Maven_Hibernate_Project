package dariusG82.custom_exeptions;

public class ItemIsNotInOrderException extends Exception{

    public ItemIsNotInOrderException(){
        super("Item is not in order or item quantity for this order is 0");
    }
}
