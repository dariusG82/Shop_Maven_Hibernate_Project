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

    void receiveGoods(int purchaseOrder) throws OrderDoesNotExistException, IOException, ItemIsNotInWarehouseExeption, WrongDataPathExeption;

    Item getItemFromWarehouse(String itemName) throws ItemIsNotInWarehouseExeption;

    void updateWarehouseStock(OrderLine orderLine) throws IOException, ItemIsNotInWarehouseExeption;

    List<Item> getAllWarehouseItems();

    Item getItemByName(String itemName);

    Item getItemById(int id);

    void addNewItem(Item item) throws ItemIsAlreadyInDatabaseException, IOException;

    int getNewItemID();
}
