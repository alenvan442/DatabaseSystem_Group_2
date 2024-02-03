import java.io.File;

import StorageManager.StorageManager;
import StorageManager.Objects.Catalog;

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

    public void start() {
        System.out.println("Welcolm to CASE-C QL");
        System.out.println("Looking at " + dbLocation);
        File dbDirectory = new File(dbLocation);
        if (dbDirectory.exists() && dbDirectory.isDirectory()) {
            System.out.println("Database found...");
            StorageManager.createStorageManager(bufferSize);
            Catalog.createCatalog(dbLocation, dbDirectory.getAbsolutePath().concat("/catalog"), -1, bufferSize);
        } else {
            System.out.println("Creating new db at " + dbLocation);
            File tableDirectory = new File(dbDirectory.getAbsolutePath() + "/tables");
            File catalogDirectory = new File(dbDirectory.getAbsolutePath() + "/catalog");
            boolean success = dbDirectory.mkdir() &&  tableDirectory.mkdir() && catalogDirectory.mkdir();
            if (success){
                StorageManager.createStorageManager(bufferSize);
                Catalog.createCatalog(dbLocation, dbDirectory.getAbsolutePath().concat("/catalog"), pageSize, bufferSize);
                System.out.println("New db created sucessfully");
                System.out.println("Page Size: " + pageSize);
                System.out.println("Buffer Size: " + bufferSize);
            }
        }
        userInterface = new UserInterface();
        userInterface.start();
        shutdown();
    }

    private void shutdown() {

    }

}
