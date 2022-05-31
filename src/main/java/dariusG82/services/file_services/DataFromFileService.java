package dariusG82.services.file_services;

import dariusG82.data.interfaces.*;

public class DataFromFileService implements DataManagement {

    private final AdminFileService adminService = new AdminFileService();
    private final AccountingFileService accountingService = new AccountingFileService();
    private final BusinessFileService businessService = new BusinessFileService();
    private final OrderManagementFileService orderManagementFileService = new OrderManagementFileService(this);
    private final WarehouseFileService warehouseService = new WarehouseFileService(this);

    public DataFromFileService() {
        getAccountingService().updateDailySalesJournal();
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
    public OrdersManagementInterface getOrderManagement() {
        return orderManagementFileService;
    }

    @Override
    public WarehouseInterface getWarehouseService() {
        return warehouseService;
    }
}
