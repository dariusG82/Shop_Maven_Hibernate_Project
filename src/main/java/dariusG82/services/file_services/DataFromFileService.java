package dariusG82.services.file_services;

import dariusG82.data.interfaces.BusinessInterface;
import dariusG82.data.interfaces.DataManagement;
import dariusG82.data.interfaces.FileReaderInterface;
import dariusG82.data.interfaces.WarehouseInterface;

public class DataFromFileService implements DataManagement, FileReaderInterface {

    private final AdminFileService adminService = new AdminFileService();
    private final AccountingFileService accountingService = new AccountingFileService();
    private final BusinessFileService businessService = new BusinessFileService();
    private final WarehouseFileService warehouseService = new WarehouseFileService();

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
    public WarehouseInterface getWarehouseService() {
        return warehouseService;
    }
}
