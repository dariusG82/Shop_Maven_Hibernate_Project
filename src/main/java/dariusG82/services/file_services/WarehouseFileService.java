package dariusG82.services.file_services;

import dariusG82.accounting.orders.OrderLine;
import dariusG82.custom_exeptions.ItemIsAlreadyInDatabaseException;
import dariusG82.custom_exeptions.ItemIsNotInWarehouseExeption;
import dariusG82.custom_exeptions.OrderDoesNotExistException;
import dariusG82.data.interfaces.DataManagement;
import dariusG82.data.interfaces.WarehouseInterface;
import dariusG82.warehouse.Item;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

import static dariusG82.services.file_services.DataPath.PURCHASE_ORDERS_LINES_PATH;
import static dariusG82.services.file_services.DataPath.WAREHOUSE_DATA_PATH;

public class WarehouseFileService extends FileDataManager implements WarehouseInterface {

    OrderManagementFileService orderManagementFileService;

    public WarehouseFileService(DataManagement dataManagement) {
        this.orderManagementFileService = (OrderManagementFileService) dataManagement.getOrderManagement();
    }

    @Override
    public long getNewItemID() {
        List<Item> items = getAllItems();

        if (items == null) {
            return 1;
        }

        return items.size() + 1;
    }
    @Override
    public Item getItemByName(String itemName) {
        List<Item> allItems = getAllItems();

        if(allItems == null){
            return null;
        }

        return allItems.stream()
                .filter(item -> item.getItemName().equals(itemName))
                .findFirst()
                .orElse(null);
    }

    @Override
    public Item getItemById(long id) {
        List<Item> allItems = getAllItems();

        if(allItems == null){
            return null;
        }

        return allItems.stream()
                .filter(item -> item.getItemId() == id)
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<Item> getAllWarehouseItems() {
        List<Item> allItems = getAllItems();

        if(allItems == null){
            return null;
        }

        return allItems.stream()
                .filter(item -> item.getStockQuantity() > 0)
                .collect(Collectors.toList());
    }

    @Override
    public void addNewItemCard(Item newItem) throws ItemIsAlreadyInDatabaseException, IOException {
        List<Item> allItems = getAllItems();

        if(allItems == null){
            allItems = new ArrayList<>();
        }

        for (Item item : allItems) {
            if (item.getItemName().equals(newItem.getItemName())) {
                throw new ItemIsAlreadyInDatabaseException();
            }
        }

        int itemId = allItems.size() + 1;
        newItem.setItemId(itemId);
        allItems.add(newItem);

        saveWarehouseStock(allItems);
    }

    @Override
    public void receiveGoods(long orderNr) throws OrderDoesNotExistException, IOException, ItemIsNotInWarehouseExeption {
        List<OrderLine> purchaseOrdersLines = orderManagementFileService.getAllOrderLines(PURCHASE_ORDERS_LINES_PATH);
        List<Item> allItems = getAllItems();

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

    @Override
    public void updateWarehouseStock(OrderLine orderLine) throws IOException, ItemIsNotInWarehouseExeption {
        Item orderLineItem = getItemById(orderLine.getItemID());
        List<Item> allItems = getAllItems();

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

        saveWarehouseStock(allItems);
    }

    @Override
    public List<Item> getAllItems() {
        try {
            Scanner scanner = new Scanner(new File(WAREHOUSE_DATA_PATH.getPath()));
            List<Item> items = new ArrayList<>();

            while (scanner.hasNext()) {
                int itemId = Integer.parseInt(scanner.nextLine());
                String itemName = scanner.nextLine();
                String description = scanner.nextLine();
                double purchasePrice = Double.parseDouble(scanner.nextLine());
                double salePrice = Double.parseDouble(scanner.nextLine());
                int quantity = Integer.parseInt(scanner.nextLine());
                scanner.nextLine();

                items.add(new Item(itemId, itemName, description, purchasePrice, salePrice, quantity));
            }

            return items;
        } catch (NumberFormatException | FileNotFoundException e) {
            return null;
        }
    }

    @Override
    public void removeItemCard(Item itemForRemoval) throws IOException {
        List<Item> allItems = getAllItems();
        List<Item> updatedItemList = allItems.stream()
                .filter(item -> !item.getItemName().equals(itemForRemoval.getItemName()))
                .toList();
        saveWarehouseStock(updatedItemList);
    }
}
