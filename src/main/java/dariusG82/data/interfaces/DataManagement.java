package dariusG82.data.interfaces;

public interface DataManagement {

    AdminInterface getAdminService();

    AccountingInterface getAccountingService();

    BusinessInterface getBusinessService();

    OrdersManagementInterface getOrderManagement();

    WarehouseInterface getWarehouseService();
}


