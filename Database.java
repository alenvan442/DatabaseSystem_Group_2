import javax.xml.catalog.Catalog;

import StorageManager.PageBuffer;
import StorageManager.StorageManager;

public class Database {
    private Catalog catalog;
    private StorageManager storageManager;
    private PageBuffer pagebuffer;
    private UserInterface userInterface;

    public Database(String dbLocation, int pageSize, int bufferSize) {
        // instantiate catalog etc. with the passed in parameters
    }

    public void start() {

    }

    private void shutdown() {
        
    }

}
