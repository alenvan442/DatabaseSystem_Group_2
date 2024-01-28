package Parser;

import javax.xml.catalog.Catalog;

import StorageManager.StorageManager;

public class DMLParser {
    private Catalog catalog;
    private StorageManager storageManager;

    public DMLParser(Catalog catalog, StorageManager storageManager) {
        this.catalog = catalog;
        this.storageManager = storageManager;
    }

    public void parseInsert(String dmlStatement) {

    }

    public void parseSelect(String dmlStatement) {

    }

    public void parseDisplay(String dmlStatement) {

    }

    public void parseDelete(String dmlStatement) {

    }

    public void parseUpdate(String dmlStatement) {

    }
}
