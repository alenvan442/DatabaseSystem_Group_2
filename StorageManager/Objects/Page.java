package StorageManager.Objects;

import java.util.Comparator;
import java.util.List;

public class Page implements java.io.Serializable, Comparator<Page> {
    private int numRecords;
    private List<Record> records;
    private int priority;
    private boolean changed;
    private int tableNumber;

    public Page(int numRecords, int tableNumber) {
        this.numRecords = numRecords;
        this.tableNumber = tableNumber;
    }

    /*
     * Gets the table number that this page is associated with
     * 
     * @return  The associated table number
     */
    public int getTableNumber() {
        return this.tableNumber;
    }

    public int getNumRecords() {
        return numRecords;
    }

    public void setNumRecords(int numRecords) {
        this.numRecords = numRecords;
    }

    public List<Record> getRecords() {
        return records;
    }

    public void setRecords(List<Record> records) {
        this.records = records;
    }

    /*
     * Returns whether or not this page has been changed
     * 
     * @return  bool
     */
    public boolean isChanged() {
        return this.changed;
    }

    /*
     * Sets the value that indicates that this page has been changed
     */
    public void setChanged() {
        this.changed = true;
    }

    /*
     * Sets the priority of this page
     * This is used in ordering the buffer for LRU
     */
    public void setPriority(int pritority) {
        this.priority = pritority;
    }

    /*
     * Adds a record to the page
     * 
     * @param record    The record to be inserted
     * 
     * @return          true: insert success
     *                  false: page is full
     */
    public boolean addRecord(Record record) {
        if (this.spaceLeft() < record.byteSize()) {
            return false;
        } else {
            // TODO, insert the record in order
            return true;
        }
    } 

    /*
     * returns the number of bytes of space is left in this page
     * 
     * @return  int - number of bytes of space left
     */
    public int spaceLeft() {
        // TODO
        return 0;
    }

    /*
     * Compare function used for the priority queue
     * of the buffer to determine which page is LRU
     */
    @Override
    public int compare(Page o1, Page o2) {
        return o1.priority - o2.priority;
    }




}
