package dariusG82.services.file_services;

import dariusG82.accounting.DailyReport;
import dariusG82.accounting.finance.CashJournalEntry;
import dariusG82.accounting.finance.CashRecord;
import dariusG82.accounting.orders.Order;
import dariusG82.accounting.orders.OrderLine;
import dariusG82.accounting.orders.OrderSeries;
import dariusG82.custom_exeptions.NegativeBalanceException;
import dariusG82.custom_exeptions.WrongDataPathExeption;
import dariusG82.data.interfaces.AccountingInterface;

import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

import static dariusG82.accounting.orders.OrderSeries.RETURN;
import static dariusG82.accounting.orders.OrderSeries.SALE;
import static dariusG82.services.file_services.DataFileIndex.*;
import static dariusG82.services.file_services.DataPath.*;

public class AccountingFileService extends FileDataManager implements AccountingInterface {

    AdminFileService adminFileService = new AdminFileService();

    @Override
    public void addNewCashRecord(CashRecord cashRecord) throws IOException {
        List<CashRecord> allCashRecords = getAllCashRecords();

        if (allCashRecords != null) {
            allCashRecords.add(cashRecord);
            List<CashRecord> uniqueRecords = sumCashRecordsBySeriesAndNumber(allCashRecords);
            rewriteDailyBalance(uniqueRecords);
        }
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
    public double getDaysBalance(LocalDate date) {
        double income = getCashOperationsByTypeAndDay(SALE, date);
        double expense = getCashOperationsByTypeAndDay(RETURN, date);
        return income - expense;
    }

    @Override
    public double getMonthBalance(LocalDate date) {
        int year = date.getYear();
        int month = date.getMonthValue();
        double income = getCashOperationsByTypeAndMonth(SALE, year, month);
        double expense = getCashOperationsByTypeAndMonth(RETURN, year, month);
        return income - expense;
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

    @Override
    public List<CashRecord> getMonthlySalesReportBySeller(String username, LocalDate date) {
        List<CashRecord> recordsBySeller = getSalesRecordsForSeller(username);

        return recordsBySeller == null ? null : getCashRecords(date, recordsBySeller);
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
    public double getTotalOrderAmount(List<OrderLine> orderLines) {

        return orderLines.stream().mapToDouble(OrderLine::getLineAmount).sum();
    }

    @Override
    public boolean isOrderReceivedPayment(Order order) {
        return order.isPayment_received();
    }

    @Override
    public List<CashRecord> getDailySaleDocuments(LocalDate date, String series) {
        ArrayList<CashRecord> allCashRecords = getAllCashRecords();
        if (allCashRecords == null) {
            return null;
        }
        return allCashRecords.stream()
                .filter(cashRecord -> cashRecord.getOrderSeries().equals(series)
                        && cashRecord.getRecordDate().equals(date.toString()))
                .collect(Collectors.toCollection(ArrayList::new));

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

    protected void updateDailySalesJournal() {
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

    private long getNewDocumentNumber(String path) throws IOException, WrongDataPathExeption {
        long documentNr = getInfoFromDataString(path);

        if (documentNr > 0) {
            return documentNr;
        } else {
            throw new WrongDataPathExeption();
        }
    }

    public void countIncomeAndExpensesByDays() throws WrongDataPathExeption, IOException {
        ArrayList<CashRecord> cashRecords = getAllCashRecords();
        ArrayList<DailyReport> dailyReports = new ArrayList<>();

        if (cashRecords == null) {
            throw new WrongDataPathExeption();
        }

        if(cashRecords.size() < 1){
            return;
        }

        CashRecord record = cashRecords.get(0);
        LocalDate date = LocalDate.parse(record.getRecordDate());
        DailyReport report = new DailyReport(date);
        report.updateDailyReport(record.getOrderSeries(), record.getAmount());
        dailyReports.add(report);
        int dailyReportIndex = 0;

        for (int index = 1; index < cashRecords.size(); index++) {
            CashRecord currentRecord = cashRecords.get(index);
            DailyReport dailyReport = dailyReports.get(dailyReportIndex);
            LocalDate newDate = LocalDate.parse(record.getRecordDate());
            if (newDate.equals(dailyReport.getDate())) {
                dailyReport.updateDailyReport(currentRecord.getOrderSeries(), currentRecord.getAmount());
            } else {
                dailyReportIndex++;
                LocalDate nextDate = LocalDate.parse(record.getRecordDate());
                dailyReport = new DailyReport(nextDate);
                dailyReport.updateDailyReport(currentRecord.getOrderSeries(), currentRecord.getAmount());
                dailyReports.add(dailyReport);
            }
        }
        rewriteDailyReports(dailyReports);

    }

    private ArrayList<CashRecord> getAllCashRecords() {
        try {
            Scanner scanner = new Scanner(new File(ALL_CASH_RECORDS_PATH.getPath()));
            ArrayList<CashRecord> cashRecords = new ArrayList<>();

            while (scanner.hasNext()) {
                long id = Long.parseLong(scanner.nextLine());
                double amount = getAmount(scanner.nextLine());
                String operationDate = scanner.nextLine();
                long orderNumber = Long.parseLong(scanner.nextLine());
                String orderSeries = scanner.nextLine();
                String sellerUsername = scanner.nextLine();
                scanner.nextLine();

                if (operationDate != null && amount != 0.0) {
                    cashRecords.add(new CashRecord(id, amount, operationDate, orderNumber, orderSeries, sellerUsername));
                }
            }
            return cashRecords;
        } catch (FileNotFoundException e) {
            return null;
        }
    }

    private List<CashRecord> sumCashRecordsBySeriesAndNumber(List<CashRecord> oldRecords) {
        List<CashRecord> cashRecords = new ArrayList<>();

        for (CashRecord record : oldRecords) {
            boolean recordUpdated = false;

            long number = record.getOrderNumber();
            String series = record.getOrderSeries();

            for (CashRecord cashRecord : cashRecords) {
                if (cashRecord.getOrderNumber() == number
                        && cashRecord.getOrderSeries().equals(series)
                        && cashRecord.getSellerId().equals(record.getSellerId())) {
                    cashRecord.updateAmount(record.getAmount());
                    recordUpdated = true;
                }
            }
            if (!recordUpdated) {
                cashRecords.add(record);
            }
        }

        return cashRecords;
    }

    private double getCashOperationsByTypeAndMonth(OrderSeries series, int year, int month) {
        List<CashRecord> allCashRecords = getAllCashRecords();
        if (allCashRecords == null) {
            return 0.0;
        }

        String dateString = getYearAndMonthString(year, month);

        return allCashRecords.stream()
                .filter(cashRecord ->
                        cashRecord.getOrderSeries().equals(series.getSeries())
                                && cashRecord.getRecordDate().startsWith(dateString))
                .mapToDouble(CashRecord::getAmount).sum();

    }

    private double getCashOperationsByTypeAndDay(OrderSeries series, LocalDate date) {
        List<CashRecord> allCashRecords = getAllCashRecords();
        if (allCashRecords == null) {
            return 0.0;
        }

        return allCashRecords.stream()
                .filter(cashRecord -> cashRecord.getOrderSeries().equals(series.getSeries())
                        && cashRecord.getRecordDate().equals(date.toString()))
                .mapToDouble(CashRecord::getAmount)
                .sum();
    }

    private List<CashRecord> getSalesRecordsForSeller(String sellerUsername) {
        List<CashRecord> allRecords = getAllCashRecords();

        if (allRecords == null) {
            return new ArrayList<>();
        }

        return allRecords.stream()
                .filter(record -> record.getOrderSeries().equals("SF") && record.getSellerId().equals(sellerUsername))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private void rewriteDailyBalance(List<CashRecord> cashRecords) throws IOException {
        PrintWriter printWriter = new PrintWriter(new FileWriter(ALL_CASH_RECORDS_PATH.getPath()));

        cashRecords.forEach(cashRecord -> {
            printWriter.println(cashRecord.getRecordID());
            printWriter.printf("%.2f\n", cashRecord.getAmount());
            printWriter.println(cashRecord.getRecordDate());
            printWriter.println(cashRecord.getOrderNumber());
            printWriter.println(cashRecord.getOrderSeries());
            printWriter.println(cashRecord.getSellerId());
            printWriter.println();
        });

        printWriter.close();
    }

    private void rewriteDailyReports(ArrayList<DailyReport> dailyReports) throws IOException {
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

    private int getInfoFromDataString(String infoSection) throws IOException, WrongDataPathExeption {
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

    private double getBalanceFromDataString(String infoSection) throws WrongDataPathExeption {
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

    private double getAmount(String input) {
        try {
            return Double.parseDouble(input);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private double getBalance(String data, String infoString) {
        if (data.startsWith(infoString)) {
            String purchaseOrderNumberString = data.substring(data.indexOf("=") + 1);
            return Double.parseDouble(purchaseOrderNumberString);
        }
        return 0.0;
    }

    private LocalDate getLoginDate() throws WrongDataPathExeption {
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

    private String getYearAndMonthString(int year, int month) {
        String dataCase = "" + year + "-";

        if (month < 10) {
            dataCase += "0" + month;
        } else {
            dataCase += month;
        }

        return dataCase;
    }

    //    String getOperationDate(String input) {
//        try {
//            int year = Integer.parseInt(input.substring(0, 4));
//            int month = Integer.parseInt(input.substring(5, 7));
//            int day = Integer.parseInt(input.substring(8, 10));
//
//            return LocalDate.of(year, month, day).toString();
//        } catch (NumberFormatException e) {
//            return null;
//        }
//    }

    //    private ArrayList<CashRecord> getSalesRecordsForMonth(ArrayList<CashRecord> records, int year, int month) {
//
//        String finalDataCase = getYearAndMonthString(year, month);
//
//        return records.stream()
//                .filter(cashRecord -> cashRecord.getRecordDate().startsWith(finalDataCase))
//                .collect(Collectors.toCollection(ArrayList::new));
//    }
}
