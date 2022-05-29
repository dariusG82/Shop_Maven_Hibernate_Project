package dariusG82.custom_exeptions;

public class ItemIsNotInWarehouseExeption extends Exception {
    public ItemIsNotInWarehouseExeption() {
        super("Item is not in warehouse");
    }
}
