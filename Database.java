import java.io.File;
import java.security.MessageDigest;

import StorageManager.StorageManager;
import StorageManager.Objects.Catalog;
import StorageManager.Objects.MessagePrinter;
import StorageManager.Objects.MessagePrinter.MessageType;

public class Database {
    private UserInterface userInterface;
    private String dbLocation;
    private int pageSize;
    private int bufferSize;

    public Database(String dbLocation, int pageSize, int bufferSize) {
        this.bufferSize = bufferSize;
        this.pageSize = pageSize;
        this.dbLocation = dbLocation;
    }

    public void start() throws Exception {
        System.out.println("Welcolm to CASE-C QL");
        System.out.println("Looking at " + dbLocation);
        File dbDirectory = new File(dbLocation);
        File schemaFile = new File(dbDirectory.getAbsolutePath().concat("/schema"));
        if (!dbDirectory.exists()) {
            MessagePrinter.printMessage(MessageType.ERROR, "Failed to find database at " + dbDirectory.getAbsolutePath());
        }
        if (schemaFile.exists()) {
            System.out.println("Database found..." + "\n" +
            "Restarting the database...\n" +
            "\tIgnoring provided pages size, using stored page size");
            StorageManager.createStorageManager(bufferSize);
            Catalog.createCatalog(dbDirectory.getAbsolutePath(), schemaFile.getAbsolutePath(), -1, bufferSize);
            System.out.println("Page Size: " + Catalog.getCatalog().getPageSize());
            System.out.println("Buffer Size: " + bufferSize);
        } else {
            System.out.println("Creating new db at " + dbDirectory.getAbsolutePath());
            File tableDirectory = new File(dbDirectory.getAbsolutePath().concat("/tables"));
            boolean success = tableDirectory.mkdir() && schemaFile.createNewFile();
            if (success){
                StorageManager.createStorageManager(bufferSize);
                Catalog.createCatalog(dbDirectory.getAbsolutePath(), schemaFile.getAbsolutePath(), pageSize, bufferSize);
                System.out.println("New db created successfully");
                System.out.println("Page Size: " + pageSize);
                System.out.println("Buffer Size: " + bufferSize);
            } else {
                MessagePrinter.printMessage(MessageType.ERROR, "Unable to successfully create the database.");
            }
        }
        userInterface = new UserInterface();
        userInterface.start();
        shutdown();
    }

    private void shutdown() {
        Catalog catalog = Catalog.getCatalog();
        StorageManager storageManager = StorageManager.getStorageManager();
        try {
            System.out.println("Safely shutting down the database...\r\n" +
                                "Purging page buffer...");
            storageManager.writeAll();
            System.out.println("Saving catalog...\n\n");
            catalog.saveCatalog();
        } catch (Exception e) {
            System.out.println("Database Failed to shut down successfully");
        }
        System.out.println("Exiting the database...");
    }

}
