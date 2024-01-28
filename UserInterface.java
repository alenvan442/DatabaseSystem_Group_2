import java.util.Scanner;

import javax.xml.catalog.Catalog;

import StorageManager.StorageManager;

public class UserInterface {
    private StorageManager storageManager;
    private Scanner scanner;
    private Catalog catalog;

    public UserInterface(StorageManager storageManager, Catalog catalog, Scanner scanner) {
        this.storageManager = storageManager;
        this.catalog = catalog;
        this.scanner = scanner;
    }
}
