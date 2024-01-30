package StorageManager;


public class StorageManager {
    private static StorageManager storageManager;
    private PageBuffer pageBuffer;


    private StorageManager() {
        this.pageBuffer = new PageBuffer();
    }

    public static void createStorageManager(int bufferSize) {
        storageManager = new StorageManager();
    }

    public static StorageManager getStorageManager() {
        return storageManager;
    }
}
