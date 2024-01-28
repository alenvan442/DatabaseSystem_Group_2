package Parser;

import javax.xml.catalog.Catalog;

import StorageManager.StorageManager;

public class DDLParser {
    private Catalog catalog;
    private StorageManager storageManager;

    public DDLParser(Catalog catalog, StorageManager storageManager) {
        this.catalog = catalog;
        this.storageManager = storageManager;
    }

    public void parseCreateTable(String ddlStatement) {

    }

    public void parseDropTable(String ddlStatement) {

    }

    public void parseAlterTable(String ddlStatement) {

    }
}
