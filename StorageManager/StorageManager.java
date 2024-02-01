package StorageManager;

import java.io.IOException;
import java.util.Dictionary;

import java.util.List;
import java.util.PriorityQueue;

import StorageManager.Objects.Page;
import StorageManager.Objects.Record;

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

    public void insertRecord(int tableNumber, Record record) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'insertRecord'");
    }

    public Table getTable(int hash) {
        
    }

    public Table getTable(String name) {
        
    }

    public int hashName(String name) {
        char[] chars = name.toLowerCase().toCharArray();
        int hash = 0;
        int index = 0;
        for (char c : chars) {
            hash += Character.hashCode(c) + index;
            index++;
        }
        return hash;
    }

    //---------------------------- Page Buffer ------------------------------
    //public void setBuffer(List<Page> buffer) {
    //    this.buffer = buffer;
    //}

    private void addBuffer(Page page) {
        if (this.buffer.size() == this.bufferSize) {
            
        } else {

        }
    }

    public void readPageHardware() {

    }

    public void writePageHardware(Page page) {
        // if the buffer size exceeds the limit then write the least recently used page to HW
        // may need to change the buffer to either a queue or a stack
        try {

        } catch (IOException io) {
            io.printStackTrace();
        }

    }

    public void writeAll() {
        for (Page page : buffer) {
            if (page.isChanged()) {
                writePageHardware(page);
            }
        }
        this.buffer.removeAll(buffer);
    }

    private void pageSerailization(Page page) {
        // serialize the page

        // encode page to bytes
    }

}
