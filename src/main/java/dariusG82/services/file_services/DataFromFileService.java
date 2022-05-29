package dariusG82.services.file_services;

import dariusG82.accounting.DailyReport;
import dariusG82.accounting.finance.CashOperation;
import dariusG82.accounting.finance.CashRecord;
import dariusG82.accounting.orders.Order;
import dariusG82.accounting.orders.OrderLine;
import dariusG82.accounting.orders.OrderSeries;
import dariusG82.custom_exeptions.ItemIsAlreadyInDatabaseException;
import dariusG82.custom_exeptions.WrongDataPathExeption;
import dariusG82.data.interfaces.*;
import dariusG82.partners.Client;
import dariusG82.users.User;
import dariusG82.users.UserType;
import dariusG82.warehouse.Item;

import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

import static dariusG82.services.file_services.DataFileIndex.CURRENT_DATE;
import static dariusG82.services.file_services.DataPath.*;

public class DataFromFileService implements DataManagement, FileReaderInterface {

    private final AdminFileService adminService = new AdminFileService(this);
    private final AccountingFileService accountingService = new AccountingFileService(this);
    private final BusinessFileService businessService = new BusinessFileService(this);
    private final WarehouseFileService warehouseService = new WarehouseFileService(this);

    public DataFromFileService() {
        updateDailySalesJournal();
    }

    public void rewriteDailyReports(ArrayList<DailyReport> dailyReports) throws IOException {
        PrintWriter printWriter = new PrintWriter(new FileWriter(String.valueOf(DAILY_CASH_JOURNALS_PATH)));

        dailyReports.forEach(report -> {
            printWriter.println(report.getDate());
            printWriter.println(report.getDailyIncome());
            printWriter.println(report.getDailyExpenses());
            printWriter.println(report.getDailyBalance());
            printWriter.println();
        });

        printWriter.close();
    }

    public List<OrderLine> getAllOrderLines(DataPath path) {
        List<OrderLine> orderLines = new ArrayList<>();

        try {
            Scanner scanner = new Scanner(new File(path.getPath()));
            while (scanner.hasNext()) {
                String orderSeries = scanner.nextLine();
                int id = Integer.parseInt(scanner.nextLine());
                int lineNumber = Integer.parseInt(scanner.nextLine());
                String itemName = scanner.nextLine();
                int quantity = Integer.parseInt(scanner.nextLine());

                scanner.nextLine();

                Item item = warehouseService.getItemFromWarehouse(itemName);
                double lineAmount = quantity * item.getSalePrice();
                OrderLine orderLine = new OrderLine(orderSeries, id, lineNumber, item.getItemId(), quantity, lineAmount);

                orderLines.add(orderLine);
            }
            return orderLines;
        } catch (FileNotFoundException | NumberFormatException e) {
            return null;
        }
    }

    @Override
    public List<OrderLine> getOrderLinesForOrder(Order order) throws WrongDataPathExeption {
        List<OrderLine> allOrderLines = getAllOrderLines(getDataPath(order.getOrderSeries()));

        return allOrderLines.stream()
                .filter(orderLine -> orderLine.getOrderNumber() == order.getOrderNumber())
                .collect(Collectors.toList());
    }

    public void rewriteOrderLines(List<OrderLine> orderLines) throws IOException, WrongDataPathExeption {
        DataPath path = getDataPath(orderLines.get(0).getOrderSeries());

        PrintWriter printWriter = new PrintWriter(new FileWriter(path.getPath()));

        orderLines.forEach(orderLine -> writeOrderLineToFile(printWriter, orderLine));

        printWriter.close();
    }

    private void writeOrderLineToFile(PrintWriter printWriter, OrderLine orderLine) {
        printWriter.println(orderLine.getOrderSeries());
        printWriter.println(orderLine.getOrderNumber());
        printWriter.println(orderLine.getOrderLineNumber());
        printWriter.println(orderLine.getItemID());
        printWriter.println(orderLine.getLineQuantity());
        printWriter.println(orderLine.getLineAmount());
        printWriter.println();
    }


    public ArrayList<CashRecord> getAllCashRecords() {
        try {
            Scanner scanner = new Scanner(new File(ALL_CASH_RECORDS_PATH.getPath()));
            ArrayList<CashRecord> cashRecords = new ArrayList<>();

            while (scanner.hasNext()) {
                String id = scanner.nextLine();
                LocalDate operationDate = getOperationDate(scanner.nextLine());
                CashOperation cashOperation = getCashOperation(scanner.nextLine());
                double amount = getAmount(scanner.nextLine());
                String sellerUsername = scanner.nextLine();
                scanner.nextLine();

//                if (operationDate != null && cashOperation != null && amount != 0.0) {
//                    cashRecords.add(new CashRecord(id, operationDate, amount, sellerUsername));
//                }
            }
            return cashRecords;
        } catch (FileNotFoundException e) {
            return null;
        }
    }

    public ArrayList<DailyReport> getDailyReports() {
        try {
            Scanner scanner = new Scanner(new File(DAILY_CASH_JOURNALS_PATH.getPath()));
            ArrayList<DailyReport> dailyReports = new ArrayList<>();

            while (scanner.hasNext()) {
                LocalDate localDate = LocalDate.parse(scanner.nextLine());
                double incomes = Double.parseDouble(scanner.nextLine());
                double expenses = Double.parseDouble(scanner.nextLine());
                double balance = Double.parseDouble(scanner.nextLine());
                scanner.nextLine();

                DailyReport report = new DailyReport(localDate, incomes, expenses, balance);
                dailyReports.add(report);

            }
            return dailyReports;
        } catch (FileNotFoundException e) {
            return null;
        }
    }

    private double getAmount(String input) {
        try {
            return Double.parseDouble(input);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private CashOperation getCashOperation(String input) {
        try {
            return CashOperation.valueOf(input);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private LocalDate getOperationDate(String input) {
        try {
            int year = Integer.parseInt(input.substring(0, 4));
            int month = Integer.parseInt(input.substring(5, 7));
            int day = Integer.parseInt(input.substring(8, 10));

            return LocalDate.of(year, month, day);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public void rewriteDailyBalance(List<CashRecord> cashRecords) {
//        PrintWriter printWriter = new PrintWriter(new FileWriter(ALL_CASH_RECORDS_PATH.getPath()));
//
//        cashRecords.forEach(cashRecord -> {
//            printWriter.println(cashRecord.getRecordID());
//            printWriter.println(cashRecord.getDate());
//            printWriter.println(cashRecord.getOperation());
//            printWriter.printf("%.2f\n", cashRecord.getAmount());
//            printWriter.println(cashRecord.getSellerId());
//            printWriter.println();
//        });
//
//        printWriter.close();
    }

    public List<User> getAllUsers() {
        try {
            Scanner scanner = new Scanner(new File(USERS_DATA_PATH.getPath()));
            ArrayList<User> users = new ArrayList<>();

            while (scanner.hasNext()) {
                String name = scanner.nextLine();
                String surname = scanner.nextLine();
                String username = scanner.nextLine();
                String password = scanner.nextLine();
                UserType type = getUserType(scanner);
                if (type != null) {
                    User user = new User(name, surname, username, password, type);
                    users.add(user);
                }
                scanner.nextLine();
            }

            return users;
        } catch (FileNotFoundException e) {
            return null;
        }
    }

    private UserType getUserType(Scanner scanner) {
        String type = scanner.nextLine();
        try {
            return UserType.valueOf(type);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }


    @Override
    public List<Client> getAllClients() {
        try {
            Scanner scanner = new Scanner(new File(CLIENT_PATH.getPath()));
            ArrayList<Client> allClients = new ArrayList<>();

            while (scanner.hasNext()) {
                String partnerName = scanner.nextLine();
                String businessID = scanner.nextLine();
                String streetAddress = scanner.nextLine();
                String city = scanner.nextLine();
                String country = scanner.nextLine();
                scanner.nextLine();

                allClients.add(new Client(partnerName, businessID, streetAddress, city, country));
            }

            return allClients;
        } catch (FileNotFoundException e) {
            return null;
        }
    }

    private void saveNewOrder(Order newOrder) throws IOException {
        List<Order> orders = getAllOrders();
        orders.add(newOrder);

        updateOrders(orders);
    }

    public List<Order> getAllOrders() {
        try {
            Scanner scanner = new Scanner(new File(ALL_ORDERS_PATH.getPath()));
            List<Order> allOrders = new ArrayList<>();

            while (scanner.hasNext()) {
                String orderSeries = scanner.nextLine();
                int orderNumber = Integer.parseInt(scanner.nextLine());
                String orderDate = scanner.nextLine();
                String clientName = scanner.nextLine();
                double amount = Double.parseDouble(scanner.nextLine());
                String salesPerson = scanner.nextLine();
                boolean payment_finished = Boolean.getBoolean(scanner.nextLine());

                allOrders.add(new Order(orderSeries, orderNumber, orderDate, clientName, amount, salesPerson, payment_finished));
            }

            return allOrders;
        } catch (FileNotFoundException e) {
            return null;
        }
    }

    public List<Order> getAllOrdersBySeries(OrderSeries series){
        List<Order> allOrders = getAllOrders();
        List<Order> filteredOrders = new ArrayList<>();

        for(Order order : allOrders){
            if(order.getOrderSeries().equals(series.getSeries())){
                filteredOrders.add(order);
            }
        }

        return filteredOrders;
    }

    @Override
    public void addNewItemCard(Item item) throws ItemIsAlreadyInDatabaseException, IOException {
        List<Item> allItems = getAllItems();

        if(!allItems.contains(item)){
            allItems.add(item);
            saveWarehouseStock(allItems);
        } else {
            throw new ItemIsAlreadyInDatabaseException();
        }
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

                items.add(new Item(itemId, itemName, description, purchasePrice, salePrice, quantity));
            }

            return items;
        } catch (NumberFormatException | FileNotFoundException e) {
            return null;
        }
    }

    @Override
    public void saveOrder(Order order, List<OrderLine> orderLines) throws WrongDataPathExeption, IOException {
        saveNewOrder(order);
        String series = order.getOrderSeries();
        DataPath dataPath = getDataPath(series);
        List<OrderLine> allOrderLines = getAllOrderLines(dataPath);

        List<Integer> orderItemsID = allOrderLines.stream()
                .filter(orderLine -> orderLine.getOrderNumber() == order.getOrderNumber())
                .map(OrderLine::getItemID).toList();

        List<Item> orderItems = getItemsList(orderItemsID);

        for (OrderLine orderLine : orderLines) {
            saveOrderLine(dataPath, orderLine);
        }

        List<Item> itemsWithUpdatedQuantity = getItemsWithUpdatedQuantityList(orderItems);

        saveWarehouseStock(itemsWithUpdatedQuantity);
    }

    private List<Item> getItemsList(List<Integer> itemsIDs) {
        List<Item> allItems = new ArrayList<>();

        for(int id : itemsIDs){
            Item item = warehouseService.getItemById(id);
            allItems.add(item);
        }

        return allItems;
    }

    public void saveOrderLine(DataPath dataPath, OrderLine orderLine) throws IOException {
        PrintWriter printWriter = new PrintWriter(new FileWriter(dataPath.getPath(), true));

        writeOrderLineToFile(printWriter, orderLine);

        printWriter.close();
    }

    public void updateOrders(List<Order> orders) throws IOException {
        PrintWriter printWriter = new PrintWriter(new FileWriter(ALL_ORDERS_PATH.getPath()));

        orders.forEach(order -> {
            printWriter.println(order.getOrderSeries());
            printWriter.println(order.getOrderNumber());
            printWriter.println(order.getOrderDate());
            printWriter.println(order.getClientName());
            printWriter.println(order.getOrderAmount());
            printWriter.println(order.getSalesperson());
            printWriter.println(order.isPayment_received());
        });
    }

    private List<Item> getItemsWithUpdatedQuantityList(List<Item> itemsToUpdate) {
        List<Item> updatedList = new ArrayList<>();
        List<Item> warehouseStock = getAllItems();

        warehouseStock.forEach(stockItem -> {
            itemsToUpdate.stream()
                    .filter(stockItem::equals)
                    .forEachOrdered(newItem -> {
                        stockItem.updateQuantity(newItem.getStockQuantity());
                        updatedList.add(stockItem);
                    });
            updatedList.add(stockItem);
        });

        return updatedList;
    }

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

    public int getInfoFromDataString(String infoSection) throws IOException, WrongDataPathExeption {
        ArrayList<String> dataList = reader.getDataStrings();

        if (dataList == null) {
            throw new WrongDataPathExeption();
        }

        for (String data : dataList) {
            int orderNr = getOrderNr(dataList, infoSection, data);
            if (orderNr > 0) {
                return orderNr;
            }
        }
        return 0;
    }

    public double getBalanceFromDataString(String infoSection) throws WrongDataPathExeption {
        ArrayList<String> dataList = reader.getDataStrings();

        if (dataList == null) {
            throw new WrongDataPathExeption();
        }

        double balance = 0.0;

        for (String data : dataList) {
            balance = getBalance(data, infoSection);
            if (balance > 0.0) {
                break;
            }
        }
        return balance;
    }

    public LocalDate getLoginDate() throws WrongDataPathExeption {
        ArrayList<String> datalist = reader.getDataStrings();

        if (datalist == null) {
            throw new WrongDataPathExeption();
        }

        return datalist.stream()
                .filter(data -> data.startsWith(CURRENT_DATE.getIndex()))
                .map(data -> data.substring(data.indexOf("-") + 1))
                .findFirst()
                .map(LocalDate::parse)
                .orElse(null);
    }

    private int getOrderNr(ArrayList<String> dataList, String infoSection, String data) throws IOException {
        if (data.startsWith(infoSection)) {
            String purchaseOrderNumberString = data.substring(data.indexOf("-") + 1);
            int newOrderNumber = Integer.parseInt(purchaseOrderNumberString);
            int index = dataList.indexOf(data);
            String newString = data.substring(0, data.indexOf("-") + 1) + ++newOrderNumber;
            dataList.set(index, newString);
            reader.updateDataStrings(dataList);
            return newOrderNumber;
        }
        return 0;
    }

    private double getBalance(String data, String infoString) {
        if (data.startsWith(infoString)) {
            String purchaseOrderNumberString = data.substring(data.indexOf("=") + 1);
            return Double.parseDouble(purchaseOrderNumberString);
        }
        return 0.0;
    }


    private void updateDailySalesJournal() {
        try {
            LocalDate localDate = LocalDate.now();
            LocalDate lastLoginDate = getLoginDate();
            if (!lastLoginDate.equals(localDate)) {
                accountingService.countIncomeAndExpensesByDays();
                adminService.updateCurrentDateInDataString(localDate);
            }
        } catch (WrongDataPathExeption | IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public DataPath getDataPath(String orderSeries) throws WrongDataPathExeption {
        return switch (orderSeries) {
            case "SF" -> SALES_ORDERS_LINES_PATH;
            case "RE" -> RETURN_ORDERS_LINES_PATH;
            case "PO" -> PURCHASE_ORDERS_LINES_PATH;
            default -> throw new WrongDataPathExeption();
        };
    }

    @Override
    public AdminFileService getAdminService() {
        return adminService;
    }

    @Override
    public AccountingFileService getAccountingService() {
        return accountingService;
    }

    @Override
    public BusinessInterface getBusinessService() {
        return businessService;
    }

    @Override
    public WarehouseInterface getWarehouseService() {
        return warehouseService;
    }
}
