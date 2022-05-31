package dariusG82.services.file_services;

import dariusG82.accounting.orders.Order;
import dariusG82.accounting.orders.OrderLine;
import dariusG82.accounting.orders.OrderSeries;
import dariusG82.custom_exeptions.WrongDataPathExeption;
import dariusG82.data.interfaces.OrdersManagementInterface;
import dariusG82.warehouse.Item;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

import static dariusG82.services.file_services.DataPath.*;

public class OrderManagementFileService implements OrdersManagementInterface {

    WarehouseFileService warehouseFileService = new WarehouseFileService();


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

    @Override
    public List<OrderLine> getOrderLinesForOrder(Order order) throws WrongDataPathExeption {
        List<OrderLine> allOrderLines = getAllOrderLines(getDataPath(order.getOrderSeries()));

        return allOrderLines.stream()
                .filter(orderLine -> orderLine.getOrderNumber() == order.getOrderNumber())
                .collect(Collectors.toList());
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
    public void updateSalesOrderStatus(Order order) throws IOException {
        List<Order> salesOrders = getAllSalesOrders();

        if(salesOrders == null){
            return;
        }

        salesOrders.stream()
                .filter(order1 -> order1.getOrderNumber() == order.getOrderNumber())
                .forEach(order1 -> order1.setPayment_received(true));

        updateOrders(salesOrders);
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

    private List<Order> getAllSalesOrders() {
        List<Order> allOrders = getAllOrders();
        List<Order> filteredOrders = new ArrayList<>();

        if(allOrders == null){
            return null;
        }

        for (Order order : allOrders) {
            if (order.getOrderSeries().equals(OrderSeries.SALE.getSeries())) {
                filteredOrders.add(order);
            }
        }

        return filteredOrders;
    }

    private List<Order> getAllOrders() {
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

    private void saveNewOrder(Order newOrder) throws IOException {
        List<Order> orders = getAllOrders();

        if(orders == null){
            orders = new ArrayList<>();
        }

        orders.add(newOrder);

        updateOrders(orders);
    }

    private void updateOrders(List<Order> orders) throws IOException {
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

    private void rewriteOrderLines(List<OrderLine> orderLines) throws IOException, WrongDataPathExeption {
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

    private void saveOrderLine(DataPath dataPath, OrderLine orderLine) throws IOException {
        PrintWriter printWriter = new PrintWriter(new FileWriter(dataPath.getPath(), true));

        writeOrderLineToFile(printWriter, orderLine);

        printWriter.close();
    }

    private DataPath getDataPath(String orderSeries) throws WrongDataPathExeption {
        return switch (orderSeries) {
            case "SF" -> SALES_ORDERS_LINES_PATH;
            case "RE" -> RETURN_ORDERS_LINES_PATH;
            case "PO" -> PURCHASE_ORDERS_LINES_PATH;
            default -> throw new WrongDataPathExeption();
        };
    }
}
