import java.io.File;
import java.util.Scanner;

import StorageManager.PageBuffer;
import StorageManager.StorageManager;
import StorageManager.Objects.Catalog;

public class Database {
    private Catalog catalog;
    private StorageManager storageManager;
    private PageBuffer pagebuffer;
    private UserInterface userInterface;
    private String dbLocation;
    private int pageSize;
    private int bufferSize;

    public Database(String dbLocation, int pageSize, int bufferSize) {
        // instantiate catalog etc. with the passed in parameters
        this.catalog = new Catalog(dbLocation + "/catalog", dbLocation, bufferSize);
        this.pagebuffer = new PageBuffer();
        this.storageManager = new StorageManager(catalog, pagebuffer);
        this.dbLocation = dbLocation;
        this.pageSize = pageSize;
        this.bufferSize = bufferSize;
    }

    public void start() {
        System.out.println("Welcolm to CASE-C QL");
        System.out.println("Looking at " + dbLocation);
        File directory = new File(dbLocation);
        if (directory.exists() && directory.isDirectory()) {
            System.out.println("Database found...");
            pagebuffer.setBufferSize(bufferSize);
        } else {
            System.out.println("Creating new db at " + dbLocation);
            boolean success = directory.mkdirs();
            if (success){
                catalog.setPageSize(pageSize);
                pagebuffer.setBufferSize(bufferSize);
                System.out.println("New db created sucessfully");
                System.out.println("Page Size: " + pageSize);
                System.out.println("Buffer Size: " + bufferSize);
            }
        }
        userInterface = new UserInterface(storageManager, catalog, new Scanner(System.in));
        userInterface.start();
    }

    private void shutdown() {

    }

}
