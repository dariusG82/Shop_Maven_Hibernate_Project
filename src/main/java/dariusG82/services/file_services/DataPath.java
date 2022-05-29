package dariusG82.services.file_services;

public enum DataPath {

    ALL_CASH_RECORDS_PATH("src/main/java/dariusG82/data/files/data/allCashRecords.txt"),
    CLIENT_PATH("src/main/java/dariusG82/data/files/data/clients.txt"),
    DAILY_CASH_JOURNALS_PATH("src/main/java/dariusG82/data/files/data/dailyCashJournals.txt"),
    ITEMS_DB("src/main/java/dariusG82/data/files/data/itemsDB.txt"),
    SYSTEM_DATA_PATH ("src/main/java/dariusG82/data/files/data/systemData.txt"),
    USERS_DATA_PATH("src/main/java/dariusG82/data/files/data/users.txt"),
    WAREHOUSE_DATA_PATH("src/main/java/dariusG82/data/files/data/warehouse.txt"),

    ALL_ORDERS_PATH("src/main/java/dariusG82/data/files/orders/allOrders.txt"),
    PURCHASE_ORDERS_LINES_PATH("src/main/java/dariusG82/data/files/orders/purchaseOrderList.txt"),
    RETURN_ORDERS_LINES_PATH("src/main/java/dariusG82/data/files/orders/returnOrderList"),
    SALES_ORDERS_LINES_PATH("src/main/java/dariusG82/data/files/orders/salesOrderList.txt");

    private final String path;

    DataPath(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}
