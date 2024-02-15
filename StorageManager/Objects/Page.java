package StorageManager.Objects;

import java.util.Comparator;
import java.util.Date;
import java.util.Dictionary;
import java.util.List;

import StorageManager.TableSchema;

public class Page implements java.io.Serializable, Comparator<Page> {
    private int numRecords;
    private List<Record> records;
    private long priority;
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
    public void setPriority() {
        this.priority = System.currentTimeMillis();
    }

    /*
     * Adds a record to the page in the correct order
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
            Dictionary<Integer, TableSchema> schemas = Catalog.getCatalog().getSchemas();
            TableSchema schema = schemas.get(this.tableNumber);
            int primaryIndex = schema.getPrimaryIndex();
            String primaryType = schema.getAttributes().get(primaryIndex).getAttributeName();
            boolean added = false;
            for (int i = 0; i < this.records.size(); i++) {
                int compare = this.records.get(i).comparison(primaryIndex, 
                                    record.getValues().get(primaryIndex), primaryType);
                if (compare == -1) {
                    // in coming record is greater, so continue
                    continue;
                } else {
                    // in coming record is either equal to, or less than
                    // so append before
                    List<Record> updatedList = this.records.subList(0, i);
                    updatedList.add(record);
                    for (int j = i; j < this.records.size(); j++) {
                        updatedList.add(this.records.get(j));
                    }
                    this.records = updatedList;
                    added = true;
                    break;
                }
            }

            // unable to find a spot, append to end of page
            if (added == false) {
                this.records.add(record);
            }
            
            this.numRecords++;
            this.changed = true;
            this.setPriority();
            return true;
        }
    } 

    /*
     * Deletes a record at a specific index
     * 
     * @param index     The index to delete the record at
     * 
     * @return          boolean, indicating success status
     */
    public boolean deleteRecord(int index) {
        Record removed = this.records.remove(index);
        if (!removed.equals(null)) {
            this.changed = true;
            this.setPriority();
            return true;
        }
        return false;
    }

    /*
     * Replaces a record at a given index
     * 
     * @param index     The index to replace the record at
     * @param record    The record to replace with
     * 
     * @return          boolean, indicating success status
     */
    public boolean updateRecord(int index, Record record) {
        if (this.getRecords().remove(index).equals(null)) {
            return false;
        } else {
            this.getRecords().add(index, record);
            this.changed = true;
            this.setPriority();
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
        if (o1.priority > o2.priority) {
            return 1;
        } else if (o1.priority == o2.priority) {
            return 0;
        } else {
            return -1;
        }
    }




}
