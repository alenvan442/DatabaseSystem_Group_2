package StorageManager;

import java.util.List;
import java.util.PriorityQueue;

import StorageManager.Objects.Page;

public class StorageManager implements StorageManagerInterface {
    private static StorageManager storageManager;
    private PriorityQueue<Page> buffer;
    private int bufferSize;



    private StorageManager(int bufferSize) {
        this.bufferSize = bufferSize;
        this.buffer = new PriorityQueue<>(bufferSize);
    }

    public static void createStorageManager(int bufferSize) {
        storageManager = new StorageManager(bufferSize);
    }

    public static StorageManager getStorageManager() {
        return storageManager;
    }
}
