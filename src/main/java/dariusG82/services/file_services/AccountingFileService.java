package dariusG82.services.file_services;

import dariusG82.accounting.finance.CashOperation;
import dariusG82.accounting.finance.CashRecord;
import dariusG82.accounting.orders.Order;
import dariusG82.accounting.orders.OrderLine;
import dariusG82.custom_exeptions.NegativeBalanceException;
import dariusG82.custom_exeptions.WrongDataPathExeption;
import dariusG82.data.interfaces.AccountingInterface;
import dariusG82.data.interfaces.FileReaderInterface;
import dariusG82.warehouse.Item;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static dariusG82.accounting.orders.OrderSeries.SALE;
import static dariusG82.services.file_services.DataFileIndex.*;
import static dariusG82.services.file_services.DataPath.*;

public class AccountingFileService implements AccountingInterface, FileReaderInterface {

    private final DataFromFileService dataService;

    public AccountingFileService(DataFromFileService dataService) {
        this.dataService = dataService;
    }

    @Override
    public void addNewCashRecord(CashRecord cashRecord) throws IOException {
        List<CashRecord> allCashRecords = dataService.getAllCashRecords();

        if (allCashRecords != null) {
            allCashRecords.add(cashRecord);
            List<CashRecord> uniqueRecords = sumCashRecordsByID(allCashRecords);
            dataService.rewriteDailyBalance(uniqueRecords);
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
        List<Order> orders = dataService.getAllOrders();

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
        DataPath dataPath = dataService.getDataPath(salesOrder.getOrderSeries());
        List<OrderLine> allOrderLines = dataService.getAllOrderLines(dataPath);

        if (allOrderLines == null) {
            throw new WrongDataPathExeption();
        }

        int orderNumber = salesOrder.getOrderNumber();

        allOrderLines.stream()
                .filter(orderLine -> orderLine.getOrderNumber() == orderNumber)
                .forEach(orderLine -> newOrderLines.stream()
                        .filter(newOrderLine -> orderLine.getItemID() == newOrderLine.getItemID())
                        .forEach(newOrderLine -> {
                            orderLine.setLineQuantity(orderLine.getLineQuantity() + newOrderLine.getLineQuantity());
                            orderLine.setLineAmount(orderLine.getLineAmount() + newOrderLine.getLineAmount());
                        })
                );

        dataService.rewriteOrderLines(allOrderLines);
    }

    @Override
    public Item getSoldItemByName(Order salesOrder, String itemName) throws WrongDataPathExeption {
        DataPath dataPath = dataService.getDataPath(salesOrder.getOrderSeries());
        List<OrderLine> salesOrderLines = dataService.getAllOrderLines(dataPath);

        for (OrderLine salesOrderLine : salesOrderLines) {
            Item soldItem = dataService.getWarehouseService().getItemById(salesOrderLine.getItemID());
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
    public int getNewSalesDocumentNumber() throws IOException, WrongDataPathExeption {
        return getNewDocumentNumber(SALES_ORDER_NR_INFO.getIndex());
    }

    @Override
    public int getNewReturnDocumentNumber() throws IOException, WrongDataPathExeption {
        return getNewDocumentNumber(RETURN_ORDER_NR_INFO.getIndex());
    }

    @Override
    public int getNewPurchaseOrderNumber() throws WrongDataPathExeption, IOException {
        return getNewDocumentNumber(PURCHASE_ORDER_NR_INFO.getIndex());
    }

    private int getNewDocumentNumber(String path) throws IOException, WrongDataPathExeption {
        int documentNr = dataService.getInfoFromDataString(path);

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

        double currentBalance = dataService.getBalanceFromDataString(dataId.getIndex());
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
        List<Order> salesOrders = dataService.getAllOrdersBySeries(SALE);

        salesOrders.stream()
                .filter(order1 -> order1.getOrderNumber() == order.getOrderNumber())
                .forEach(order1 -> order1.setPayment_received(true));

        dataService.updateOrders(salesOrders);
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
        List<CashRecord> allCashRecords = dataService.getAllCashRecords();
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
        List<CashRecord> allCashRecords = dataService.getAllCashRecords();
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
        List<CashRecord> allRecords = dataService.getAllCashRecords();
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
}
