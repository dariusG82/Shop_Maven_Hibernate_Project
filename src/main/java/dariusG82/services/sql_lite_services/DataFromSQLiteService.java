package dariusG82.services.sql_lite_services;

import dariusG82.data.interfaces.*;

public class DataFromSQLiteService extends SQLService implements DataManagement {

    private final AdminDatabaseService adminDatabaseService = new AdminDatabaseService();
    private final BusinessDatabaseService businessDatabaseService = new BusinessDatabaseService();
    private final WarehouseDatabaseService warehouseDatabaseService = new WarehouseDatabaseService();
    private final AccountingDatabaseService accountingDatabaseService = new AccountingDatabaseService();

    @Override
    public AdminInterface getAdminService() {
        return adminDatabaseService;
    }

    @Override
    public AccountingInterface getAccountingService() {
        return accountingDatabaseService;
    }

    @Override
    public BusinessInterface getBusinessService() {
        return businessDatabaseService;
    }

    @Override
    public WarehouseInterface getWarehouseService() {
        return warehouseDatabaseService;
    }


}
