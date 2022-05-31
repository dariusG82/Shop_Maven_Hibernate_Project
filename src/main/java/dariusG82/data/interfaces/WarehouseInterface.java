package dariusG82.data.interfaces;

import dariusG82.accounting.orders.OrderLine;
import dariusG82.custom_exeptions.ItemIsAlreadyInDatabaseException;
import dariusG82.custom_exeptions.ItemIsNotInWarehouseExeption;
import dariusG82.custom_exeptions.OrderDoesNotExistException;
import dariusG82.custom_exeptions.WrongDataPathExeption;
import dariusG82.warehouse.Item;

import java.io.IOException;
import java.util.List;

public interface WarehouseInterface {

    void receiveGoods(long purchaseOrder) throws OrderDoesNotExistException, IOException, ItemIsNotInWarehouseExeption, WrongDataPathExeption;

    void updateWarehouseStock(OrderLine orderLine) throws IOException, ItemIsNotInWarehouseExeption;

    List<Item> getAllWarehouseItems();

    Item getItemByName(String itemName);

    Item getItemById(long id);

    long getNewItemID();

    void addNewItemCard(Item newItem) throws ItemIsAlreadyInDatabaseException, IOException;

    void removeItemCard(Item item) throws IOException;

    List<Item> getAllItems();
}
