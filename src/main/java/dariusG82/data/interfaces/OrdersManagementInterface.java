package dariusG82.data.interfaces;

import dariusG82.accounting.orders.Order;
import dariusG82.accounting.orders.OrderLine;
import dariusG82.custom_exeptions.*;
import dariusG82.services.file_services.DataPath;
import dariusG82.warehouse.Item;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public interface OrdersManagementInterface {

    Order getDocumentByID(String id) throws WrongDataPathExeption, ClientDoesNotExistExeption, SQLException, OrderDoesNotExistException;

    List<OrderLine> getOrderLinesForOrder(Order order) throws WrongDataPathExeption;

    List<OrderLine> getAllOrderLines(DataPath orderDataType) throws WrongDataPathExeption, ItemIsNotInWarehouseExeption;

    Item getSoldItemByName(Order salesOrder, String itemName) throws SQLException, WrongDataPathExeption, FileNotFoundException, ItemIsNotInOrderException, ItemIsNotInWarehouseExeption;

    void saveOrder(Order order, List<OrderLine> orderLines) throws WrongDataPathExeption, IOException, ItemIsNotInWarehouseExeption;

    void updateSalesOrderLines(Order salesOrder, List<OrderLine> orderLines) throws WrongDataPathExeption, IOException;

    void updateSalesOrderStatus(Order order) throws IOException, WrongDataPathExeption;
}
