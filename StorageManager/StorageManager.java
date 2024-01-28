package StorageManager;

import StorageManager.Objects.Catalog;

public class StorageManager {
    private Catalog catalog;
    private PageBuffer buffer;
    private String dbLocation;

    public StorageManager(Catalog catalog, PageBuffer buffer) {
        this.catalog = catalog;
        this.buffer =  buffer;
    }

    //will call page buffer's write to hardware
    public void insertRecord(int tableNumber) {

    }
}
