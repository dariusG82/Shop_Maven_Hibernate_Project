package dariusG82.services.file_services;

import dariusG82.accounting.DailyReport;
import dariusG82.accounting.finance.CashJournalEntry;
import dariusG82.accounting.finance.CashOperation;
import dariusG82.accounting.finance.CashRecord;
import dariusG82.accounting.orders.Order;
import dariusG82.accounting.orders.OrderLine;
import dariusG82.accounting.orders.OrderSeries;
import dariusG82.custom_exeptions.NegativeBalanceException;
import dariusG82.custom_exeptions.WrongDataPathExeption;
import dariusG82.data.interfaces.AccountingInterface;
import dariusG82.data.interfaces.FileReaderInterface;
import dariusG82.warehouse.Item;

import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

import static dariusG82.accounting.orders.OrderSeries.SALE;
import static dariusG82.services.file_services.DataFileIndex.*;
import static dariusG82.services.file_services.DataPath.*;

public class AccountingFileService implements AccountingInterface, FileReaderInterface {

    WarehouseFileService warehouseFileService = new WarehouseFileService();
    AdminFileService adminFileService = new AdminFileService();

    @Override
    public void addNewCashRecord(CashRecord cashRecord) {
        List<CashRecord> allCashRecords = getAllCashRecords();

        if (allCashRecords != null) {
            allCashRecords.add(cashRecord);
            List<CashRecord> uniqueRecords = sumCashRecordsByID(allCashRecords);
            rewriteDailyBalance(uniqueRecords);
        }
    }

    @Override
    public Order getDocumentByID(String id) throws WrongDataPathExeption {
        String documentSeries = id.substring(0, id.indexOf(" ") + 1);
        String index = id.substring(id.indexOf(" ") + 1);
        int orderNr = Integer.parseInt(index);

        switch (documentSeries) {
            case "SF " -> {
                return getOrder(SALES_ORDERS_LINES_PATH, orderNr);
            }
            case "RE " -> {
                return getOrder(RETURN_ORDERS_LINES_PATH, orderNr);
            }
            case "PO" -> {
                return getOrder(PURCHASE_ORDERS_LINES_PATH, orderNr);
            }
            default -> {
                return null;
            }
        }
    }

    private Order getOrder(DataPath orderDataPath, int orderNr) throws WrongDataPathExeption {
        List<Order> orders = getAllOrders();

        if (orders == null) {
            return null;
        }

        String orderSeries = switch (orderDataPath) {
            case PURCHASE_ORDERS_LINES_PATH -> "PO";
            case RETURN_ORDERS_LINES_PATH -> "RE";
            case SALES_ORDERS_LINES_PATH -> "SF";
            default -> null;
        };

        if (orderSeries == null) {
            throw new WrongDataPathExeption();
        }

        return orders.stream()
                .filter(order -> order.getOrderSeries().equals(orderSeries) && order.getOrderNumber() == orderNr)
                .findFirst().orElse(null);
    }

    @Override
    public void updateSalesOrderLines(Order salesOrder, List<OrderLine> newOrderLines) throws WrongDataPathExeption, IOException {
        DataPath dataPath = getDataPath(salesOrder.getOrderSeries());
        List<OrderLine> allOrderLines = getAllOrderLines(dataPath);

        if (allOrderLines == null) {
            throw new WrongDataPathExeption();
        }

        long orderNumber = salesOrder.getOrderNumber();

        allOrderLines.stream()
                .filter(orderLine -> orderLine.getOrderNumber() == orderNumber)
                .forEach(orderLine -> newOrderLines.stream()
                        .filter(newOrderLine -> orderLine.getItemID() == newOrderLine.getItemID())
                        .forEach(newOrderLine -> {
                            orderLine.setLineQuantity(orderLine.getLineQuantity() + newOrderLine.getLineQuantity());
                            orderLine.setLineAmount(orderLine.getLineAmount() + newOrderLine.getLineAmount());
                        })
                );

        rewriteOrderLines(allOrderLines);
    }

    @Override
    public Item getSoldItemByName(Order salesOrder, String itemName) throws WrongDataPathExeption {
        DataPath dataPath = getDataPath(salesOrder.getOrderSeries());
        List<OrderLine> salesOrderLines = getAllOrderLines(dataPath);

        for (OrderLine salesOrderLine : salesOrderLines) {
            Item soldItem = warehouseFileService.getItemById(salesOrderLine.getItemID());
            if (soldItem.getItemName().equals(itemName)) {
                return soldItem;
            }
        }
        return null;
    }

    public double getDaysBalance(LocalDate date) {
        double income = getCashOperationsByTypeAndDay(CashOperation.DAILY_INCOME, date);
        double expense = getCashOperationsByTypeAndDay(CashOperation.DAILY_EXPENSE, date);
        return income - expense;
    }

    public double getMonthBalance(LocalDate date) {
        int year = date.getYear();
        int month = date.getMonthValue();
        double income = getCashOperationsByTypeAndMonth(CashOperation.DAILY_INCOME, year, month);
        double expense = getCashOperationsByTypeAndMonth(CashOperation.DAILY_EXPENSE, year, month);
        return income - expense;
    }

    @Override
    public List<CashRecord> getDailySaleDocuments(LocalDate date, String cashOperation) {
//        ArrayList<CashRecord> allCashRecords = dataService.getAllCashRecords();
//        ArrayList<CashRecord> cashRecords;
//        if (allCashRecords == null) {
//            return null;
//        }
//        cashRecords = allCashRecords.stream()
//                .filter(cashRecord -> cashRecord.getOperation().equals(cashOperation) && cashRecord.getDate().equals(date))
//                .collect(Collectors.toCollection(ArrayList::new));

        return null;
    }

    @Override
    public long getNewSalesDocumentNumber() throws IOException, WrongDataPathExeption {
        return getNewDocumentNumber(SALES_ORDER_NR_INFO.getIndex());
    }

    @Override
    public long getNewReturnDocumentNumber() throws IOException, WrongDataPathExeption {
        return getNewDocumentNumber(RETURN_ORDER_NR_INFO.getIndex());
    }

    @Override
    public long getNewPurchaseOrderNumber() throws WrongDataPathExeption, IOException {
        return getNewDocumentNumber(PURCHASE_ORDER_NR_INFO.getIndex());
    }

    private long getNewDocumentNumber(String path) throws IOException, WrongDataPathExeption {
        long documentNr = getInfoFromDataString(path);

        if (documentNr > 0) {
            return documentNr;
        } else {
            throw new WrongDataPathExeption();
        }
    }

    @Override
    public List<CashRecord> getMonthlySalesReportBySeller(String username, LocalDate date) {
        List<CashRecord> recordsBySeller = getSalesRecordsForSeller(username);

        return recordsBySeller == null ? null : getCashRecords(date, recordsBySeller);
    }

    @Override
    public double getTotalSalesByReport(List<CashRecord> records) {
        double totalSales = 0.0;
        for (CashRecord record : records) {
            totalSales += record.getAmount();
        }
        return totalSales;
    }

    @Override
    public void updateCashBalance(double amount, DataFileIndex dataId) throws WrongDataPathExeption, IOException, NegativeBalanceException {
        ArrayList<String> datalist = reader.getDataStrings();

        if (datalist == null) {
            throw new WrongDataPathExeption();
        }

        double currentBalance = getBalanceFromDataString(dataId.getIndex());
        double newBalance = currentBalance + amount;

        if (newBalance < 0) {
            throw new NegativeBalanceException();
        }

        for (String data : datalist) {
            if (data.startsWith(dataId.getIndex())) {
                updateBalanceStringData(datalist, newBalance, dataId.getIndex());
                reader.updateDataStrings(datalist);
            }
        }
    }

    @Override
    public boolean isOrderReceivedPayment(Order order) {
        return order.isPayment_received();
    }

    @Override
    public void updateSalesOrderStatus(Order order) throws IOException {
        List<Order> salesOrders = getAllOrdersBySeries(SALE);

        salesOrders.stream()
                .filter(order1 -> order1.getOrderNumber() == order.getOrderNumber())
                .forEach(order1 -> order1.setPayment_received(true));

        updateOrders(salesOrders);
    }

    public void countIncomeAndExpensesByDays() throws WrongDataPathExeption, IOException {
//        ArrayList<CashRecord> cashRecords = dataService.getAllCashRecords();
//        ArrayList<DailyReport> dailyReports = new ArrayList<>();
//
//        if (cashRecords == null) {
//            throw new WrongDataPathExeption();
//        }
//
//        CashRecord record = cashRecords.get(0);
//        DailyReport report = new DailyReport(record.getDate());
//        report.updateDailyReport(record.getOperation(), record.getAmount());
//        dailyReports.add(report);
//        int dailyReportIndex = 0;
//
//        for (int index = 1; index < cashRecords.size(); index++) {
//            CashRecord currentRecord = cashRecords.get(index);
//            DailyReport dailyReport = dailyReports.get(dailyReportIndex);
//            if (currentRecord.getDate().equals(dailyReport.getDate())) {
//                dailyReport.updateDailyReport(currentRecord.getOperation(), currentRecord.getAmount());
//            } else {
//                dailyReportIndex++;
//                dailyReport = new DailyReport(currentRecord.getDate());
//                dailyReport.updateDailyReport(currentRecord.getOperation(), currentRecord.getAmount());
//                dailyReports.add(dailyReport);
//            }
//        }
//        dataService.rewriteDailyReports(dailyReports);


    }


    private List<CashRecord> sumCashRecordsByID(List<CashRecord> oldRecords) {
        List<CashRecord> cashRecords = new ArrayList<>();

//        for (CashRecord record : oldRecords) {
//            boolean recordUpdated = false;
//
//            String id = record.getRecordID();
//
//            for (CashRecord cashRecord : cashRecords) {
//                if (cashRecord.getRecordID().equals(id) && cashRecord.getSellerId().equals(record.getSellerId())) {
//                    cashRecord.updateAmount(record.getAmount());
//                    recordUpdated = true;
//                }
//            }
//            if (!recordUpdated) {
//                cashRecords.add(record);
//            }
//        }

        return cashRecords;
    }

    private double getCashOperationsByTypeAndMonth(CashOperation operation, int year, int month) {
        List<CashRecord> allCashRecords = getAllCashRecords();
        double cashSum = 0.0;
//        if (allCashRecords == null) {
//            return cashSum;
//        }
//        for (CashRecord cashRecord : allCashRecords) {
//            if (cashRecord.getOperation().equals(operation) &&
//                    cashRecord.getDate().getYear() == year &&
//                    cashRecord.getDate().getMonthValue() == month) {
//                cashSum += cashRecord.getAmount();
//            }
//        }
        return cashSum;
    }

    private double getCashOperationsByTypeAndDay(CashOperation operation, LocalDate date) {
        List<CashRecord> allCashRecords = getAllCashRecords();
        double cashSum = 0.0;
//        if (allCashRecords == null) {
//            return cashSum;
//        }
//        cashSum = allCashRecords.stream()
//                .filter(cashRecord -> cashRecord.getOperation().equals(operation) && cashRecord.getDate().equals(date))
//                .mapToDouble(CashRecord::getAmount)
//                .sum();

        return cashSum;
    }

    private List<CashRecord> getSalesRecordsForSeller(String sellerUsername) {
        List<CashRecord> allRecords = getAllCashRecords();
        List<CashRecord> salesCashRecords;

//        if (allRecords == null) {
//            return new ArrayList<>();
//        }
//
//        salesCashRecords = allRecords.stream()
//                .filter(record -> record.getRecordID().startsWith("SF ") && record.getSellerId().equals(sellerUsername))
//                .collect(Collectors.toCollection(ArrayList::new));

        return null;
    }

    private ArrayList<CashRecord> getSalesRecordsForMonth(ArrayList<CashRecord> records, int year, int month) {

//        return records.stream()
//                .filter(cashRecord -> cashRecord.getDate().getYear() == year && cashRecord.getDate().getMonthValue() == month)
//                .collect(Collectors.toCollection(ArrayList::new));
        return null;
    }

    private void updateBalanceStringData(ArrayList<String> dataList, double newBalance, String dataId) {
        for (String data : dataList) {
            if (data.startsWith(dataId)) {
                int index = dataList.indexOf(data);
                String balance = newBalance + "0";
                String balanceString = data.substring(0, data.indexOf("=") + 1) + balance.substring(0, balance.indexOf(".") + 2);
                dataList.set(index, balanceString);
            }
        }
    }

    @Override
    public double getTotalOrderAmount(List<OrderLine> orderLines) {

        return orderLines.stream().mapToDouble(OrderLine::getLineAmount).sum();
    }

    private ArrayList<CashRecord> getAllCashRecords() {
        try {
            Scanner scanner = new Scanner(new File(ALL_CASH_RECORDS_PATH.getPath()));
            ArrayList<CashRecord> cashRecords = new ArrayList<>();

            while (scanner.hasNext()) {
                String id = scanner.nextLine();
                String operationDate = scanner.nextLine();
                String cashOperation = scanner.nextLine();
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

    @Override
    public List<CashJournalEntry> getDailyReports() {
        try {
            Scanner scanner = new Scanner(new File(DAILY_CASH_JOURNALS_PATH.getPath()));
            List<CashJournalEntry> dailyReports = new ArrayList<>();

            while (scanner.hasNext()) {
                long orderId = Long.parseLong(scanner.nextLine());
                String localDate = scanner.nextLine();
                double incomes = Double.parseDouble(scanner.nextLine());
                double expenses = Double.parseDouble(scanner.nextLine());
                double balance = Double.parseDouble(scanner.nextLine());
                scanner.nextLine();

                CashJournalEntry report = new CashJournalEntry(orderId, localDate, incomes, expenses, balance);
                dailyReports.add(report);

            }
            return dailyReports;
        } catch (FileNotFoundException e) {
            return null;
        }
    }

    double getAmount(String input) {
        try {
            return Double.parseDouble(input);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    CashOperation getCashOperation(String input) {
        try {
            return CashOperation.valueOf(input);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    String getOperationDate(String input) {
        try {
            int year = Integer.parseInt(input.substring(0, 4));
            int month = Integer.parseInt(input.substring(5, 7));
            int day = Integer.parseInt(input.substring(8, 10));

            return LocalDate.of(year, month, day).toString();
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

                Item item = warehouseFileService.getItemFromWarehouse(itemName);
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

    public List<Order> getAllOrdersBySeries(OrderSeries series) {
        List<Order> allOrders = getAllOrders();
        List<Order> filteredOrders = new ArrayList<>();

        for (Order order : allOrders) {
            if (order.getOrderSeries().equals(series.getSeries())) {
                filteredOrders.add(order);
            }
        }

        return filteredOrders;
    }

    @Override
    public void saveOrder(Order order, List<OrderLine> orderLines) throws WrongDataPathExeption, IOException {
        saveNewOrder(order);
        String series = order.getOrderSeries();
        DataPath dataPath = getDataPath(series);
        List<OrderLine> allOrderLines = getAllOrderLines(dataPath);

        List<Long> orderItemsID = allOrderLines.stream()
                .filter(orderLine -> orderLine.getOrderNumber() == order.getOrderNumber())
                .map(OrderLine::getItemID).toList();

        List<Item> orderItems = warehouseFileService.getItemsList(orderItemsID);

        for (OrderLine orderLine : orderLines) {
            saveOrderLine(dataPath, orderLine);
        }

        List<Item> itemsWithUpdatedQuantity = warehouseFileService.getItemsWithUpdatedQuantityList(orderItems);

        warehouseFileService.saveWarehouseStock(itemsWithUpdatedQuantity);
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


    void updateDailySalesJournal() {
        try {
            LocalDate localDate = LocalDate.now();
            LocalDate lastLoginDate = getLoginDate();
            if (!lastLoginDate.equals(localDate)) {
                countIncomeAndExpensesByDays();
                adminFileService.updateCurrentDateInDataString(localDate);
            }
        } catch (WrongDataPathExeption | IOException e) {
            System.out.println(e.getMessage());
        }
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

    public DataPath getDataPath(String orderSeries) throws WrongDataPathExeption {
        return switch (orderSeries) {
            case "SF" -> SALES_ORDERS_LINES_PATH;
            case "RE" -> RETURN_ORDERS_LINES_PATH;
            case "PO" -> PURCHASE_ORDERS_LINES_PATH;
            default -> throw new WrongDataPathExeption();
        };
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

    public void saveOrderLine(DataPath dataPath, OrderLine orderLine) throws IOException {
        PrintWriter printWriter = new PrintWriter(new FileWriter(dataPath.getPath(), true));

        writeOrderLineToFile(printWriter, orderLine);

        printWriter.close();
    }

    private void saveNewOrder(Order newOrder) throws IOException {
        List<Order> orders = getAllOrders();
        orders.add(newOrder);

        updateOrders(orders);
    }
}
