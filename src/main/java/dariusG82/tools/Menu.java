package dariusG82.tools;

public class Menu {

    public static void printMainMenu(){
        System.out.println("Choose your function:");
        System.out.println("[1] - Accounting login");
        System.out.println("[2] - Sales login");
        System.out.println("[3] - IT login");
        System.out.println("[0] - Exit");
    }

    public static void printAccountantMenu() {
        System.out.println("Accountant Operations:");
        System.out.println("**********************");
        System.out.println("[1] - Financial Operations");
        System.out.println("[2] - Clients Operations");
        System.out.println("[3] - Items Operations");
        System.out.println("[0] - Return to previous menu");
    }

    public static void printFinancialMenu() {
        System.out.println("Balance Operations:");
        System.out.println("**********************");
        System.out.println("[1] - Get daily sales/returns balance reports");
        System.out.println("[2] - Balance for a day");
        System.out.println("[3] - Balance for a month");
        System.out.println("[4] - Sales documents by day");
        System.out.println("[5] - Returns documents by day");
        System.out.println("[6] - Month sales by seller");
        System.out.println("[0] - Return to previous menu");
    }

    public static void printClientsServiceMenu() {
        System.out.println("Clients Master Operations:");
        System.out.println("**********************");
        System.out.println("[1] - Add new client");
        System.out.println("[2] - Get clientID by client name");
        System.out.println("[3] - Delete client from database");
        System.out.println("[4] - Get all clients from database");
        System.out.println("[0] - Return to previous menu");
    }

    public static void printItemsServiceMenu(){
        System.out.println("Items Service Operations:");
        System.out.println("**********************");
        System.out.println("[1] - Add new Item Card");
        System.out.println("[2] - Remove Item Card");
        System.out.println("[0] - Return to previous menu");
    }

    public static void printSalesmanMenu() {
        System.out.println("Sales Operations:");
        System.out.println("**********************");
        System.out.println("[1] - Create sales order");
        System.out.println("[2] - Make payment for sales order");
        System.out.println("[3] - Find sales document by document Nr.");
        System.out.println("[4] - Return operation");
        System.out.println("[5] - Find return document by document Nr. ");
        System.out.println("[6] - Create goods order to shop");
        System.out.println("[7] - Receive goods to warehouse by order Nr.");
        System.out.println("[8] - Print warehouse stock");
        System.out.println("[0] - Return to previous menu");
    }

    public static void printITSupportMenu() {
        System.out.println("IT Support Operations:");
        System.out.println("**********************");
        System.out.println("[1] - Print list of users");
        System.out.println("[2] - Register new user");
        System.out.println("[3] - Remove user");
        System.out.println("[0] - Return to previous menu");
    }
}
