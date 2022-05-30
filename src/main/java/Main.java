import dariusG82.accounting.finance.CashJournalEntry;
import dariusG82.accounting.finance.CashRecord;
import dariusG82.accounting.orders.Order;
import dariusG82.accounting.orders.OrderLine;
import dariusG82.accounting.orders.OrderSeries;
import dariusG82.custom_exeptions.*;
import dariusG82.data.interfaces.DataManagement;
import dariusG82.partners.Client;
import dariusG82.services.sql_lite_services.DataFromSQLiteService;
import dariusG82.users.User;
import dariusG82.users.UserType;
import dariusG82.warehouse.Item;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static dariusG82.accounting.finance.CashOperation.*;
import static dariusG82.accounting.orders.OrderSeries.*;
import static dariusG82.services.file_services.DataFileIndex.*;
import static dariusG82.tools.Menu.*;

public class Main {

    private static final Scanner scanner = new Scanner(System.in);
    private static final DataManagement SERVICE = new DataFromSQLiteService();
//    private static final DataManagement SERVICE = new DataFromFileService();

    public static void main(String[] args) {
        System.out.println("Welcome to Office Goods Shop");

        while (true) {
            printMainMenu();
            int option = getChoiceFromScanner(3);

            switch (option) {
                case 1 -> {
                    try {
                        UserType type = UserType.ACCOUNTING;
                        User accountingUser = confirmLoginAndGetUser(type);
                        if (accountingUser != null) {
                            loginAsAccountant(accountingUser);
                        } else {
                            System.out.println("Wrong username/password/User type");
                        }
                    } catch (UserNotFoundException e) {
                        System.out.println(e.getMessage());
                    }
                }
                case 2 -> {
                    try {
                        UserType type = UserType.SALES;
                        User salesUser = confirmLoginAndGetUser(type);
                        if (salesUser != null) {
                            loginAsSalesman(salesUser);
                        } else {
                            System.out.println("Wrong username/password/User type");
                        }
                    } catch (UserNotFoundException e) {
                        System.out.println(e.getMessage());
                    }
                }
                case 3 -> {
                    try {
                        UserType type = UserType.ADMIN;
                        User supportUser = confirmLoginAndGetUser(type);
                        if (supportUser != null) {
                            loginAsITSupport(supportUser);
                        } else {
                            System.out.println("Wrong username/password/User type");
                        }
                    } catch (UserNotFoundException e) {
                        System.out.println(e.getMessage());
                    }
                }
                case 0 -> {
                    return;
                }
                default -> System.out.println("Unavailable option");
            }
        }
    }

    private static User confirmLoginAndGetUser(UserType type) throws UserNotFoundException {
        System.out.print("Enter your username: ");
        String username = scanner.nextLine();
        System.out.print("Enter your password: ");
        String password = scanner.nextLine();

        return SERVICE.getAdminService().getUser(username, password, type);
    }

    private static void loginAsAccountant(User currentUser) {
        System.out.printf("Welcome, %s %s!\n", currentUser.getName(), currentUser.getSurname());
        while (true) {
            printAccountantMenu();
            switch (getChoiceFromScanner(2)) {
                case 1 -> startFinancialOperations();
                case 2 -> startClientsOperations();
                case 3 -> startItemOperations();
                case 0 -> {
                    return;
                }
                default -> System.out.println("Unavailable option");
            }
        }
    }

    private static void startFinancialOperations() {
        while (true) {
            printFinancialMenu();
            switch (getChoiceFromScanner(6)) {
                case 1 -> getDailySalesReturnsBalance();
                case 2 -> getBalanceForADay();
                case 3 -> getBalanceForAMonth();
                case 4 -> getSalesDocumentsByDay();
                case 5 -> getReturnsDocumentsByDay();
                case 6 -> getSalesBySellerByMonth();
                case 0 -> {
                    return;
                }
                default -> System.out.println("Unavailable option");
            }
        }
    }

    private static void startClientsOperations() {
        while (true) {
            printClientsServiceMenu();
            switch (getChoiceFromScanner(3)) {
                case 1 -> addNewClient();
                case 2 -> getClientIDByClientName();
                case 3 -> deleteClient();
                case 4 -> getAllClientsFromDatabase();
                case 0 -> {
                    return;
                }
                default -> System.out.println("Unavailable option");
            }
        }
    }

    private static void startItemOperations() {
        while (true) {
            printItemsServiceMenu();
            switch (getChoiceFromScanner(3)) {
                case 1 -> addNewItemCard();
                case 2 -> removeItemCard();
                case 0 -> {
                    return;
                }
                default -> System.out.println("Unavailable option");
            }
        }
    }

    private static void getDailySalesReturnsBalance() {
        List<CashJournalEntry> reports = SERVICE.getAccountingService().getDailyReports();

        if (reports == null) {
            System.out.println("There is no daily reports");
            return;
        }
        double totalBalance = 0.0;
        System.out.println("**********************");
        for (CashJournalEntry report : reports) {
            System.out.printf("Date: %s || Daily income = %.2f, Daily expenses = %.2f, || Daily balance = %.2f\n",
                    report.getReportDate(), report.getDailyIncome(), report.getDailyExpenses(), report.getDailyBalance());
            totalBalance += report.getDailyBalance();
        }
        System.out.println("**********************");
        System.out.printf("Total sales/returns balance = %.2f\n", totalBalance);
    }

    private static void getBalanceForADay() {
        System.out.print("Enter the day for balance - format yyyy-mm-dd: ");
        while (true) {
            try {
                LocalDate date = getLocalDate();
                double cashBalance = SERVICE.getAccountingService().getDaysBalance(date);
                System.out.println("*******************");
                System.out.printf("Balance for %s is: %.2f\n", date, cashBalance);
                System.out.println("*******************");
                return;
            } catch (NumberFormatException e) {
                System.out.println("Wrong format, try again");
            } catch (DateTimeException e) {
                System.out.println("Wrong day/month/year entered, try again");
            } catch (SQLException e) {
                System.out.println("Database error");
            }
        }

    }

    private static void getBalanceForAMonth() {
        System.out.print("Enter the month for balance - format yyyy-mm: ");
        while (true) {
            try {
                LocalDate date = getLocalDate();
                double cashBalance = SERVICE.getAccountingService().getMonthBalance(date);
                System.out.println("*******************");
                System.out.printf("Balance for %d %s is: %.2f\n", date.getYear(), date.getMonth(), cashBalance);
                System.out.println("*******************");
                return;
            } catch (NumberFormatException e) {
                System.out.println("Wrong format, try again");
            } catch (DateTimeException e) {
                System.out.println("Wrong day/month/year entered, try again");
            }
        }
    }

    private static void getSalesDocumentsByDay() {
        System.out.print("Enter the day for sales documents balance - format yyyy-mm-dd: ");
        while (true) {
            try {
                LocalDate date = getLocalDate();
                List<CashRecord> salesForDay = SERVICE.getAccountingService().getDailySaleDocuments(date, DAILY_INCOME.toString());
                if (salesForDay.size() == 0) {
                    System.out.printf("There is no sales document for %s day\n", date);
                    return;
                }
                System.out.printf("Sales documents for %s day is:\n", date);
                System.out.println("*******************");
                for (CashRecord cashRecord : salesForDay) {
                    System.out.printf("Sales document id: %s, amount = %.2f\n",
                            cashRecord.getRecordID(), cashRecord.getAmount());
                    System.out.println("*******************");
                }
                return;
            } catch (NumberFormatException e) {
                System.out.println("Wrong format, try again");
            } catch (DateTimeException e) {
                System.out.println("Wrong day/month/year entered, try again");
            }
        }
    }

    private static void getReturnsDocumentsByDay() {
        System.out.print("Enter the day for returns documents balance - format yyyy-mm-dd: ");
        while (true) {
            try {
                LocalDate date = getLocalDate();
                List<CashRecord> salesForDay = SERVICE.getAccountingService().getDailySaleDocuments(date, DAILY_EXPENSE.toString());
                System.out.printf("Return documents for %s is:\n", date);
                System.out.println("*******************");
                for (CashRecord cashRecord : salesForDay) {
                    System.out.printf("Return document id: %s, amount = %.2f\n",
                            cashRecord.getRecordID(), cashRecord.getAmount());
                    System.out.println("*******************");
                }
                return;
            } catch (NumberFormatException e) {
                System.out.println("Wrong format, try again");
            } catch (DateTimeException e) {
                System.out.println("Wrong day/month/year entered, try again");
            }
        }
    }

    private static void getSalesBySellerByMonth() {
        System.out.print("Enter salesman username to get report: ");
        String sellerUsername = scanner.nextLine();
        User user;
        user = SERVICE.getAdminService().getUserByUsername(sellerUsername);
        if (user == null) {
            System.out.printf("User with username %s does not exist\n", sellerUsername);
            return;
        }
        while (true) {
            try {
                System.out.print("Enter month for sales report - format yyyy-mm: ");
                LocalDate date = getLocalDate();
                List<CashRecord> userSales = SERVICE.getAccountingService().getMonthlySalesReportBySeller(sellerUsername, date);
                if (userSales.size() == 0) {
                    System.out.printf("No sales data found for %s user for %s %s\n",
                            sellerUsername, date.getYear(), date.getMonth());
                    return;
                }
                for (CashRecord record : userSales) {
                    System.out.println(record.toString());
                }
                System.out.println("*******************");
                System.out.printf("Total %s %s sales for %s %s is: %.2f\n",
                        user.getName(), user.getSurname(),
                        date.getYear(), date.getMonth(),
                        SERVICE.getAccountingService().getTotalSalesByReport(userSales));
                System.out.println("*******************");
                return;
            } catch (NumberFormatException e) {
                System.out.println("Wrong format, try again");
            } catch (DateTimeException e) {
                System.out.println("Wrong day/month/year entered, try again");
            }
        }
    }

    private static void addNewClient() {
        System.out.print("Enter client name: ");
        String name;
        try {
            name = validateClientName();
        } catch (ClientAlreadyInDatabaseException e) {
            System.out.println(e.getMessage());
            return;
        }
        System.out.print("Enter client businessID: ");
        String businessId = scanner.nextLine();
        System.out.print("Enter client street address: ");
        String streetAddress = scanner.nextLine();
        System.out.print("Enter client city: ");
        String city = scanner.nextLine();
        System.out.print("Enter client country: ");
        String country = scanner.nextLine();

        Client client = new Client(name, businessId, streetAddress, city, country);

        try {
            SERVICE.getBusinessService().addNewClientToDatabase(client);
            System.out.printf("New client %s added successfully!\n", client.getClientName());
        } catch (WrongDataPathExeption e) {
            System.out.println(e.getMessage());
        } catch (IOException | SQLException e) {
            System.out.printf("Client %s was not added\n", client.getClientName());
        }
    }

    private static String validateClientName() throws ClientAlreadyInDatabaseException {
        String clientName = scanner.nextLine();
        try {
            if (SERVICE.getBusinessService().isClientNameUnique(clientName)) {
                return clientName;
            } else {
                throw new ClientAlreadyInDatabaseException(clientName);
            }

        } catch (WrongDataPathExeption e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    private static void getClientIDByClientName() {
        System.out.print("Enter client name: ");
        String name = scanner.nextLine();
        try {
            Client client = SERVICE.getBusinessService().getClientByName(name);
            if (client == null) {
                System.out.printf("Client %s does not exist in database\n", name);
                return;
            }
            System.out.printf("Client %s id is: %s\n", name, client.getClientID());
        } catch (WrongDataPathExeption | ClientDoesNotExistExeption | SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void deleteClient() {
        System.out.print("Enter name of client you want to delete: ");
        String name = scanner.nextLine();
        try {
            Client client = SERVICE.getBusinessService().getClientByName(name);
            SERVICE.getBusinessService().deleteClientFromDatabase(client);
            System.out.printf("Client %s successfully deleted\n", client.getClientName());
        } catch (WrongDataPathExeption | ClientDoesNotExistExeption | SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void getAllClientsFromDatabase() {
        System.out.println("All clients List: ");
        List<Client> clients = SERVICE.getBusinessService().getAllClients();

        for (Client client : clients) {
            System.out.println("Client name: " + client.getClientName() + ", business ID = " + client.getClientID());
        }
    }

    private static void addNewItemCard() {
        System.out.print("Enter new item name: ");
        String itemName = scanner.nextLine();
        System.out.print("Enter item description: ");
        String itemDescription = scanner.nextLine();
        System.out.print("Enter item price: ");
        double purchasePrice = Double.parseDouble(scanner.nextLine());

        long itemId = SERVICE.getWarehouseService().getNewItemID();

        Item item = new Item(itemId, itemName, itemDescription, purchasePrice);

        try {
            SERVICE.getWarehouseService().addNewItem(item);
            System.out.printf("%s ItemCard successfully added to database\n", itemName);
        } catch (ItemIsAlreadyInDatabaseException e) {
            System.out.println(e.getMessage());
        } catch (IOException e) {
            System.out.println("File not found, item cannot be added");
        }
    }

    private static void removeItemCard() {
        // TODO implement function
    }

    private static void loginAsSalesman(User currentUser) {
        System.out.printf("Welcome, %s %s!\n", currentUser.getName(), currentUser.getSurname());
        while (true) {
            printSalesmanMenu();
            switch (getChoiceFromScanner(8)) {
                case 1 -> createNewSalesOperation(currentUser);
                case 2 -> makePaymentForSalesOrder();
                case 3 -> findDocumentByID(SALE);
                case 4 -> createNewReturnOperation(currentUser);
                case 5 -> findDocumentByID(RETURN);
                case 6 -> createPurchaseOrderToWarehouse();
                case 7 -> receiveGoodsToWarehouse();
                case 8 -> showWarehouseStock();
                case 0 -> {
                    return;
                }
                default -> System.out.println("Unavailable option");
            }
        }
    }

    private static void createNewSalesOperation(User currentUser) {
        System.out.println("Enter client name: ");
        String clientName = scanner.nextLine();
        Client client;
        try {
            client = SERVICE.getBusinessService().getClientByName(clientName);
        } catch (ClientDoesNotExistExeption | WrongDataPathExeption | SQLException e) {
            System.out.println(e.getMessage());
            return;
        }
        try {
            long salesOrderID = SERVICE.getAccountingService().getNewSalesDocumentNumber();
            Order newSalesOrder = createNewSalesOrder(currentUser, client, salesOrderID);
            List<OrderLine> orderLines = new ArrayList<>();

            int orderLineNumber = 1;
            while (true) {
                System.out.println("[1] - Add item to sales order / [2] - Finish sales order");
                switch (getChoiceFromScanner(2)) {
                    case 1 -> {
                        Item item = getItemByName();
                        if (item.getStockQuantity() == 0) {
                            System.out.println("Item is out of stock");
                            return;
                        }
                        int quantity = getNewItemQuantity(item);
                        double lineAmount = quantity * item.getSalePrice();
                        OrderLine orderLine = new OrderLine(
                                SALE.getSeries(), salesOrderID, orderLineNumber, item.getItemId(), quantity, lineAmount
                        );
                        orderLineNumber++;
                        orderLines.add(orderLine);
                        SERVICE.getWarehouseService().updateWarehouseStock(orderLine);
                    }
                    case 2 -> {
                        newSalesOrder.setOrderAmount(SERVICE.getAccountingService().getTotalOrderAmount(orderLines));
                        SERVICE.getAccountingService().saveOrder(newSalesOrder, orderLines);
                        System.out.printf("Total sales order cash amount = %.2f\n", newSalesOrder.getOrderAmount());
                        return;
                    }
                    default -> System.out.println("Wrong choice, choose again");
                }
            }
        } catch (WrongDataPathExeption | IOException e) {
            System.out.println("Cannot create new order / data file doesn't exist");
        } catch (ItemIsNotInWarehouseExeption e) {
            System.out.println("Item is not in warehouse");
        }
    }

    private static void makePaymentForSalesOrder() {
        Order order = getOrderForPayment();
        if (order == null) {
            System.out.println("Wrong order");
            return;
        }
        System.out.println("Choose your payment method: [1] - Cash, [2] - Credit card");
        switch (getChoiceFromScanner(2)) {
            case 1 -> makeCashPayment(order);
            case 2 -> makeCreditCardPayment(order);
            default -> {
                System.out.println("No such payment method");
                return;
            }
        }
        CashRecord record = new CashRecord(
                order.getOrderAmount(), LocalDate.now().toString(), order.getOrderNumber(), order.getOrderSeries(), order.getSalesperson()
        );
        try {
            SERVICE.getAccountingService().addNewCashRecord(record);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private static Order getOrderForPayment() {
        System.out.println("Enter document Nr. for which you want to make payment");
        Order order = getDocumentFromAccounting(SALE.getSeries());

        try {
            if (SERVICE.getAccountingService().isOrderReceivedPayment(order)) {
                System.out.println("Order is already paid");
                return null;
            }
        } catch (SQLException e) {
            System.out.println("Database error");
            return null;
        }
        return order;
    }

    private static void makeCreditCardPayment(Order order) {
        while (true) {
            try {
                double orderAmount = order.getOrderAmount();
                {
                    System.out.printf("Credit card charged for %.2f Eur\n", orderAmount);
                    SERVICE.getAccountingService().updateCashBalance(orderAmount, BANK_ACCOUNT);
                    SERVICE.getAccountingService().updateSalesOrderStatus(order);
                    System.out.println("Credit Card payment is accepted");
                    return;
                }
            } catch (NumberFormatException e) {
                System.out.println("Wrong amount, try again");
            } catch (WrongDataPathExeption | NegativeBalanceException | IOException e) {
                System.out.println("Payment failed");
                System.out.println(e.getMessage());
                return;
            }
        }
    }

    private static void makeCashPayment(Order order) {
        while (true) {
            try {
                System.out.print("Enter amount of money you want to pay: ");
                double amount = Double.parseDouble(scanner.nextLine());
                double orderAmount = order.getOrderAmount();
                if (amount == orderAmount || amount > orderAmount) {
                    SERVICE.getAccountingService().updateCashBalance(orderAmount, CASH_REGISTER);
                    SERVICE.getAccountingService().updateSalesOrderStatus(order);
                    System.out.println("Cash payment is accepted");
                    if (amount > orderAmount) {
                        System.out.printf("Your return is: %.2f\n", amount - orderAmount);
                    }
                    return;
                } else {
                    System.out.println("Not enough money to pay for order");
                }
            } catch (NumberFormatException e) {
                System.out.println("Wrong amount, try again");
            } catch (WrongDataPathExeption | NegativeBalanceException | IOException e) {
                System.out.println("Payment failed");
                System.out.println(e.getMessage());
            }
        }
    }

    private static void createNewReturnOperation(User currentUser) {
        Order order = getDocumentFromAccounting(SALE.getSeries());

        if (order == null) {
            System.out.println("Sales order cannot be found");
            return;
        }
        try {
            long returnOrderID = SERVICE.getAccountingService().getNewReturnDocumentNumber();

            Order newReturnOrder = createNewReturnOrder(currentUser, order, returnOrderID);

            List<OrderLine> returnOrderLines = new ArrayList<>();
            int orderLineNumber = 1;

            while (true) {
                System.out.println("[1] - Add item to return order / [2] - Finish return order");
                switch (getChoiceFromScanner(2)) {
                    case 1 -> returnOrderLines.add(createReturnOderLine(order, newReturnOrder, orderLineNumber));
                    case 2 -> {
                        try {
                            for (OrderLine orderLine : returnOrderLines) {
                                SERVICE.getWarehouseService().updateWarehouseStock(orderLine);
                            }
                            SERVICE.getAccountingService().updateSalesOrderLines(order, returnOrderLines);
                            newReturnOrder.setOrderAmount(SERVICE.getAccountingService().getTotalOrderAmount(returnOrderLines));
                            SERVICE.getAccountingService().saveOrder(newReturnOrder, returnOrderLines);
                            makeCashReturnPayment(newReturnOrder);
                            System.out.printf("Total return order cash amount = %.2f\n", newReturnOrder.getOrderAmount());
                            return;
                        } catch (IOException | ItemIsNotInWarehouseExeption e) {
                            System.out.println(e.getMessage());
                        }
                    }
                    default -> System.out.println("Wrong choice, choose again");
                }
                orderLineNumber++;
            }
        } catch (WrongDataPathExeption | IOException e) {
            System.out.println("Cannot create new return order / data file doesn't exist");
        }
    }

    private static OrderLine createReturnOderLine(Order salesOrder, Order returnOrder, int lineNumber) {
        System.out.print("Enter sold item name: ");
        String itemName = scanner.nextLine();
        Item returnedItem = null;
        try {
            returnedItem = SERVICE.getAccountingService().getSoldItemByName(salesOrder, itemName);
        } catch (SQLException e) {
            System.out.println("Database ERROR");
        } catch (WrongDataPathExeption | FileNotFoundException | ItemIsNotInOrderException e) {
            System.out.println(e.getMessage());
        }

        if (returnedItem == null) {
            System.out.printf("Cannot find sold item by name: %s\n", itemName);
            return null;
        }
        int quantity = getNewItemQuantity(returnedItem);
        double lineAmount = quantity * returnedItem.getSalePrice();
        returnedItem.setStockQuantity(quantity);

        return new OrderLine(returnOrder.getOrderSeries(), returnOrder.getOrderNumber(), lineNumber,
                returnedItem.getItemId(), -quantity, -lineAmount);
    }

    private static void findDocumentByID(OrderSeries orderSeries) {
        switch (orderSeries) {
            case SALE -> getDocumentByID(SALE);
            case RETURN -> getDocumentByID(RETURN);
            case PURCHASE -> getDocumentByID(PURCHASE);
        }
    }

    private static void getDocumentByID(OrderSeries orderSeries) {
        Order order = getDocumentFromAccounting(orderSeries.getSeries());
        if (order == null) {
            System.out.println("Order cannot be found");
            return;
        }
        System.out.println("*****************************");
        System.out.printf("Order Nr: %s\n", order.getOrderSeries() + " " + order.getOrderNumber());
        List<OrderLine> orderLines;
        try {
            orderLines = SERVICE.getAccountingService().getOrderLinesForOrder(order);
            for (OrderLine orderLine : orderLines) {
                Item item = SERVICE.getWarehouseService().getItemById(orderLine.getItemID());
                System.out.printf("Item: %s, quantity: %s, unitPrice: %.2f, total line amount %.2f\n",
                        item.getItemName(),
                        orderLine.getLineQuantity(),
                        item.getSalePrice(),
                        orderLine.getLineQuantity() * item.getSalePrice());
            }
            System.out.println("**************");
            System.out.printf("Total order amount: %.2f\n", order.getOrderAmount());
            System.out.println("*****************************");
        } catch (WrongDataPathExeption e) {
            System.out.println(e.getMessage());
        }

    }

    private static void createPurchaseOrderToWarehouse() {
        try {
            long purchaseNr = SERVICE.getAccountingService().getNewPurchaseOrderNumber();
            Order purchaseOrder = createNewPurchaseOrder(purchaseNr);
            List<OrderLine> purchaseOrderLines = new ArrayList<>();

            int orderLineNumber = 1;
            while (true) {
                System.out.println("[1] - Add item to purchase order / [2] - Finish order");

                switch (getChoiceFromScanner(2)) {
                    case 1 -> {
                        OrderLine orderLine = createNewPurchaseOrderLine(purchaseOrder, orderLineNumber);
                        if (orderLine == null) {
                            orderLineNumber--;
                            break;
                        }
                        purchaseOrder.updateOrderAmount(orderLine.getLineAmount());
                        purchaseOrderLines.add(orderLine);
                    }
                    case 2 -> {
                        System.out.printf("Total purchase order cash amount = %.2f\n", purchaseOrder.getOrderAmount());
                        SERVICE.getAccountingService().updateCashBalance(-purchaseOrder.getOrderAmount(), BANK_ACCOUNT);
                        SERVICE.getAccountingService().saveOrder(purchaseOrder, purchaseOrderLines);
                        return;
                    }
                    default -> System.out.println("Wrong choice, choose again");
                }
                orderLineNumber++;
            }
        } catch (IOException |
                 WrongDataPathExeption |
                 NegativeBalanceException e) {
            System.out.println(e.getMessage());
        }

    }

    private static Order createNewPurchaseOrder(long purchaseNr) {
        System.out.printf("Creating purchase order Nr.: %d\n", purchaseNr);
        System.out.println("***********************");

        Order purchaseOrder = new Order(purchaseNr);
        purchaseOrder.setOrderSeries(PURCHASE.getSeries());
        purchaseOrder.setOrderDate(LocalDate.now().toString());
        purchaseOrder.setClientName("none");
        purchaseOrder.setOrderAmount(0.0);
        purchaseOrder.setSalesperson("none");
        purchaseOrder.setPayment_received(false);

        return purchaseOrder;
    }

    private static Order createNewSalesOrder(User currentUser, Client client, long salesOrderID) {
        System.out.printf("Creating sales order Nr.: %d\n", salesOrderID);
        System.out.println("***********************");

        Order newOrder = new Order(salesOrderID, currentUser.getUsername());

        newOrder.setOrderSeries(SALE.getSeries());
        newOrder.setOrderDate(LocalDate.now().toString());
        newOrder.setClientName(client.getClientName());
        newOrder.setPayment_received(false);

        return newOrder;
    }

    private static Order createNewReturnOrder(User currentUser, Order order, long returnOrderID) {
        System.out.printf("Creating return order Nr.: %d\n", returnOrderID);
        System.out.println("***********************");

        Order returnOrder = new Order(returnOrderID, currentUser.getUsername());

        returnOrder.setOrderSeries(RETURN.getSeries());
        returnOrder.setOrderDate(LocalDate.now().toString());
        returnOrder.setClientName(order.getClientName());
        returnOrder.setPayment_received(true);

        return returnOrder;
    }

    private static OrderLine createNewPurchaseOrderLine(Order purchaseOrder, int orderLineNumber) throws FileNotFoundException {
        System.out.print("Enter item name: ");
        String itemName = scanner.nextLine();
        Item item = SERVICE.getWarehouseService().getItemByName(itemName);
        if (item == null) {
            System.out.println("Item is not in database, ask accountant to create new Item Card");
            return null;
        }
        int quantity = getQuantityFromScanner();
        double lineAmount = item.getSalePrice() * quantity;

        return new OrderLine(
                purchaseOrder.getOrderSeries(),
                purchaseOrder.getOrderNumber(),
                orderLineNumber,
                item.getItemId(),
                quantity,
                lineAmount);

    }

    private static void receiveGoodsToWarehouse() {
        int purchaseNr = 0;
        do {
            System.out.print("Enter goods purchase order nr: ");
            String input = scanner.nextLine();
            try {
                purchaseNr = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Wrong input, try again");
            }
            if (purchaseNr <= 0) {
                System.out.println("Order number cannot be 0");
            } else {
                break;
            }
        } while (true);
        try {
            SERVICE.getWarehouseService().receiveGoods(purchaseNr);
            System.out.println("Goods successfully added to warehouse stock");
            System.out.println("**********************");
        } catch (OrderDoesNotExistException | IOException | ItemIsNotInWarehouseExeption | WrongDataPathExeption e) {
            System.out.println(e.getMessage());
        }
    }

    private static void showWarehouseStock() {
        List<Item> stock = SERVICE.getWarehouseService().getAllWarehouseItems();

        if (stock == null) {
            System.out.println("Warehouse is empty");
            return;
        }

        for (Item item : stock) {
            System.out.println("**********");
            System.out.printf("Item: %s, total stock: %d\n", item.getItemName(), item.getStockQuantity());
        }
        System.out.println("**********");
    }

    private static Order getDocumentFromAccounting(String orderSeries) {
        System.out.println("Enter sales/return document number: ");
        String requestedID = scanner.nextLine();

        if (requestedID.substring(0, 2).equals(orderSeries)) {
            try {
                return SERVICE.getAccountingService().getDocumentByID(requestedID);
            } catch (WrongDataPathExeption | ClientDoesNotExistExeption | SQLException |
                     OrderDoesNotExistException e) {
                System.out.println(e.getMessage());
                return null;
            }
        } else {
            return null;
        }
    }

    private static void makeCashReturnPayment(Order order) {
        double returnAmount = order.getOrderAmount();

        try {
            CashRecord record = new CashRecord(
                    returnAmount,
                    LocalDate.now().toString(),
                    order.getOrderNumber(),
                    order.getOrderSeries(),
                    order.getSalesperson());
            SERVICE.getAccountingService().updateCashBalance(-returnAmount, BANK_ACCOUNT);
            SERVICE.getAccountingService().addNewCashRecord(record);

        } catch (WrongDataPathExeption | NegativeBalanceException e) {
            System.out.println(e.getMessage());
        } catch (IOException e) {
            System.out.println("File System ERROR");
        }
    }

    private static int getQuantityFromScanner() {
        do {
            try {
                System.out.print("Enter item purchase quantity: ");
                String purchaseQuantityString = scanner.nextLine();
                int quantity = Integer.parseInt(purchaseQuantityString);
                if (quantity > 0) {
                    return quantity;
                }
            } catch (NumberFormatException e) {
                System.out.println("Wrong input, try again");
            }
        } while (true);
    }

    private static Item getItemByName() {
        while (true) {
            System.out.print("Enter sold item name: ");
            String itemName = scanner.nextLine();
            Item item = SERVICE.getWarehouseService().getItemByName(itemName);
            if (item != null) {
                return item;
            }
        }
    }

    private static void loginAsITSupport(User currentUser) {
        System.out.printf("Welcome, %s %s!\n", currentUser.getName(), currentUser.getSurname());
        while (true) {
            printITSupportMenu();
            switch (getChoiceFromScanner(3)) {
                case 1 -> printListOfUsers();
                case 2 -> registerNewUser();
                case 3 -> removeUserByUsername(currentUser);
                case 0 -> {
                    return;
                }
                default -> System.out.println("Unavailable option");
            }
        }
    }

    private static void printListOfUsers() {
        List<User> users = SERVICE.getAdminService().getAllUsers();
        System.out.println("********************");
        for (User user : users) {
            System.out.printf("Username: %s, User: %s %s, User Role: %s\n",
                    user.getUsername(), user.getName(), user.getSurname(), user.getUserType());
            System.out.println("********************");
        }
    }

    private static void registerNewUser() {
        System.out.println("Enter new user data:");
        String username = validateUsername();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();
        System.out.print("Enter name of user: ");
        String name = scanner.nextLine();
        System.out.print("Enter surname of user: ");
        String surname = scanner.nextLine();
        UserType type = getUserType();
        if (username == null) {
            System.out.println("User cannot be added -> Database Error");
        }
        try {
            SERVICE.getAdminService().addNewUser(new User(name, surname, username, password, type));
            System.out.printf("User %s successfully added\n", username);
        } catch (IOException e) {
            System.out.printf("User %s cannot be added\n", username);
        }
    }

    private static void removeUserByUsername(User currentUser) {
        System.out.println("Enter username of user to delete: ");
        String username = scanner.nextLine();
        if (currentUser.getUsername().equals(username)) {
            System.out.println("ATTENTION!!! You cannot delete yourself!!!");
            return;
        }

        try {
            SERVICE.getAdminService().removeUser(username);
            System.out.printf("User %s has been deleted\n", username);
        } catch (UserNotFoundException e) {
            System.out.println(e.getMessage());
        } catch (IOException e) {
            System.out.println("Database file was not found");
        }
    }

    private static String validateUsername() {
        while (true) {
            System.out.print("Enter username: ");
            String username = scanner.nextLine();
            if (SERVICE.getAdminService().isUsernameUnique(username)) {
                return username;
            } else {
                System.out.println("Username is already in database, try again");
            }
        }
    }

    private static UserType getUserType() {
        String input;
        int option = 0;
        while (true) {
            System.out.println("Choose new user role: ");
            System.out.println("[1] - ACCOUNTING");
            System.out.println("[2] - SALESPERSON");
            System.out.println("[3] - IT SUPPORT");
            try {
                input = scanner.nextLine();
                option = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Wrong input format, try again");
            }
            switch (option) {
                case 1 -> {
                    return UserType.ACCOUNTING;
                }
                case 2 -> {
                    return UserType.SALES;
                }
                case 3 -> {
                    return UserType.ADMIN;
                }
                default -> System.out.println("Unavailable option");
            }
        }
    }

    private static LocalDate getLocalDate() throws DateTimeException, NumberFormatException {
        String input;
        input = scanner.nextLine();

        int year = Integer.parseInt(input.substring(0, 4));
        int month = Integer.parseInt(input.substring(5, 7));
        int day = 1;
        if (input.length() == 10) {
            day = Integer.parseInt(input.substring(8));
        }

        return LocalDate.of(year, month, day);
    }

    private static int getNewItemQuantity(Item item) {
        while (true) {
            System.out.print("Enter quantity: ");
            int quantity = getChoiceFromScanner(Integer.MAX_VALUE);
            if (quantity == 0) {
                System.out.println("Quantity cannot be 0");
            } else if (quantity > item.getStockQuantity()) {
                System.out.println("Not enough items");
            } else {
                return quantity;
            }
        }
    }

    private static int getChoiceFromScanner(int upperChoiceLimit) {
        String input;
        int option;
        while (true) {
            try {
                input = scanner.nextLine();
                option = Integer.parseInt(input);
                if (option > upperChoiceLimit || option < 0) {
                    System.out.println("Wrong choice, try again");
                } else {
                    return option;
                }
            } catch (NumberFormatException e) {
                System.out.println("Wrong input format, try again");
            }
        }
    }
}
