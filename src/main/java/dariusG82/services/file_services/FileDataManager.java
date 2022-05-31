package dariusG82.services.file_services;

import dariusG82.data.files.FileDataReader;
import dariusG82.warehouse.Item;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import static dariusG82.services.file_services.DataPath.WAREHOUSE_DATA_PATH;

public class FileDataManager {
    FileDataReader reader = new FileDataReader();

    public void saveWarehouseStock(List<Item> items) throws IOException {
        PrintWriter printWriter = new PrintWriter(new FileWriter(WAREHOUSE_DATA_PATH.getPath()));

        items.forEach(item -> {
            printWriter.println(item.getItemId());
            printWriter.println(item.getItemName());
            printWriter.println(item.getItemDescription());
            printWriter.println(item.getPurchasePrice());
            printWriter.println(item.getSalePrice());
            printWriter.println(item.getStockQuantity());
            printWriter.println();
        });

        printWriter.close();
    }
}
