package StorageManager;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.PriorityQueue;

import StorageManager.Objects.Catalog;
import StorageManager.Objects.Page;
import StorageManager.Objects.Record;

public class StorageManager implements StorageManagerInterface {
    private static StorageManager storageManager;
    private PriorityQueue<Page> buffer;
    private int bufferSize;

    /*
     * Constructor for the storage manager
     * initializes the class by initializing the buffer
     * 
     * @param buffersize    The size of the buffer
     */
    private StorageManager(int bufferSize) {
        this.bufferSize = bufferSize;
        this.buffer = new PriorityQueue<>(bufferSize);
    }

    /*
     * Static function that initializes the storageManager
     * 
     * @param bufferSize    The size of the buffer
     */
    public static void createStorageManager(int bufferSize) {
        storageManager = new StorageManager(bufferSize);
    }

    /*
     * Getter for the global storageManager
     * 
     * @return  The storageManager
     */
    public static StorageManager getStorageManager() {
        return storageManager;
    }

    /*
     * Splits a page that is full into 2 separate pages
     * 
     * @param page  The page that is to be split
     * 
     * @return      An array of size 2, consisting of 2 pages
     */
    private Page[] pageSplit(Page page) {
        List<Record> records = page.getRecords();
        int size = records.size();
        int startIndex = Math.ceilDiv(size, 2) - 1;
        Page _new = new Page(startIndex + 1, page.getTableNumber());
        for (int i = startIndex; i < size; i++) {
            _new.addRecord(records.get(startIndex));
            records.remove(startIndex);
        }

        page.setRecords(records);
        page.setNumRecords(records.size());

        page.isChanged();
        _new.isChanged();

        this.addBuffer(_new);

        return new Page[] {page, _new};

    }

    /*
     * Checks the buffer to determine if a needed page
     * is already needed
     * 
     * @param   tentative
     * 
     * @return  returns a page if found, otherwise null
     */
    private Page checkBuffer() {
        // TODO
        return null;
    }

    private String getTablePath(int tableNumber) {
        String dbLoc = Catalog.getCatalog().getDbLocation();
        return dbLoc + "/tables/" + Integer.toString(tableNumber);

    }

    public void insertRecord(int tableNumber, Record record) {
        // TODO
    }

    public void deleteRecord(int tableNumber, Object primaryKey) {
        // TODO
    }

    public void updateRecord(int tableNumber, Object primaryKey, Record record) {
        // TODO
    }

    //---------------------------- Page Buffer ------------------------------
    //public void setBuffer(List<Page> buffer) {
    //    this.buffer = buffer;
    //}

    private void addBuffer(Page page) {
        if (this.buffer.size() == this.bufferSize) {
            // TODO, write LRU to disk
        } 

        this.buffer.add(page);
    }

    private void readPageHardware() {
        // TODO
    }

    private void writePageHardware(Page page) {
        // if the buffer size exceeds the limit then write the least recently used page to HW
        // may need to change the buffer to either a queue or a stack
        // TODO

    }

    public void writeAll() {
        for (Page page : buffer) {
            if (page.isChanged()) {
                writePageHardware(page);
            }
        }
        this.buffer.removeAll(buffer);
    }

    private void pageEncode(Page page) {
        // encode page to bytes
        // TODO

    }

}
