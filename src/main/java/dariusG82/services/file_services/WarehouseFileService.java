package dariusG82.services.file_services;

import dariusG82.accounting.orders.OrderLine;
import dariusG82.custom_exeptions.*;
import dariusG82.data.interfaces.*;
import dariusG82.warehouse.Item;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static dariusG82.services.file_services.DataFileIndex.PURCHASE_ORDER_NR_INFO;
import static dariusG82.services.file_services.DataPath.PURCHASE_ORDERS_LINES_PATH;

public class WarehouseFileService implements WarehouseInterface, FileReaderInterface {

    private final DataFromFileService dataService;

    public WarehouseFileService(DataFromFileService dataService) {
        this.dataService = dataService;
    }



    @Override
    public void receiveGoods(int orderNr) throws OrderDoesNotExistException, IOException, ItemIsNotInWarehouseExeption {
        List<OrderLine> purchaseOrdersLines = dataService.getAllOrderLines(PURCHASE_ORDERS_LINES_PATH);
        List<Item> allItems = dataService.getAllItems();

        if (purchaseOrdersLines == null) {
            throw new OrderDoesNotExistException(orderNr);
        } else if (allItems == null) {
            throw new FileNotFoundException();
        }

        for (OrderLine orderLine : purchaseOrdersLines) {
            if (orderLine.getOrderNumber() == orderNr) {
                updateWarehouseStock(orderLine);
            }
        }
    }

    public void addNewItem(Item newItem) throws ItemIsAlreadyInDatabaseException, IOException {
        List<Item> allItems = dataService.getAllItems();

        for (Item item : allItems) {
            if (item.getItemName().equals(newItem.getItemName())) {
                throw new ItemIsAlreadyInDatabaseException();
            }
        }

        int itemId = allItems.size() + 1;
        newItem.setItemId(itemId);
        allItems.add(newItem);

        dataService.saveWarehouseStock(allItems);
    }

    @Override
    public Item getItemFromWarehouse(String itemName) {
        List<Item> allItems = dataService.getAllItems();

        if (allItems == null) {
            return null;
        }

        try {
            return allItems.stream()
                    .filter(item -> item.getItemName().equals(itemName))
                    .findFirst()
                    .orElseThrow();
        } catch (NoSuchElementException e) {
            return null;
        }
    }

    @Override
    public void updateWarehouseStock(OrderLine orderLine) throws IOException, ItemIsNotInWarehouseExeption {
        Item orderLineItem = getItemById(orderLine.getItemID());
        List<Item> allItems = dataService.getAllItems();

        if (allItems == null) {
            throw new ItemIsNotInWarehouseExeption();
        }
        boolean itemFound = allItems.stream().anyMatch(item -> item.equals(orderLineItem));

        if (!itemFound) {
            allItems.add(orderLineItem);
        } else {
            int itemIndex = allItems.indexOf(orderLineItem);
            Item item = allItems.get(itemIndex);
            item.setStockQuantity(orderLine.getLineQuantity() + item.getStockQuantity());
            allItems.remove(itemIndex);
            allItems.add(item);
        }

        dataService.saveWarehouseStock(allItems);
    }

    @Override
    public List<Item> getAllWarehouseItems() {
        List<Item> allItems = dataService.getAllItems();

        return allItems.stream()
                .filter(item -> item.getStockQuantity() > 0)
                .collect(Collectors.toList());
    }

    @Override
    public Item getItemByName(String itemName) {
        List<Item> allItems = dataService.getAllItems();

        return allItems.stream()
                .filter(item -> item.getItemName().equals(itemName))
                .findFirst()
                .orElse(null);
    }

    @Override
    public Item getItemById(int id) {
        List<Item> allItems = dataService.getAllItems();

        return allItems.stream()
                .filter(item -> item.getItemId() == id)
                .findFirst()
                .orElse(null);
    }

    @Override
    public int getNewItemID() {
        List<Item> items = dataService.getAllItems();

        if(items == null){
            return 1;
        }

        return items.size() + 1;
    }
}
