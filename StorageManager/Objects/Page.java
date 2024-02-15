package StorageManager.Objects;

import java.lang.management.MemoryType;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import StorageManager.TableSchema;
import StorageManager.Objects.MessagePrinter.MessageType;

public class Page implements java.io.Serializable, Comparator<Page> {
    private int numRecords;
    private List<Record> records;
    private long priority;
    private boolean changed;
    private int tableNumber;
    private int pageNumber;

    public Page(int numRecords, int tableNumber, int pageNumber) {
        this.numRecords = numRecords;
        this.tableNumber = tableNumber;
        this.pageNumber = pageNumber;
    }

    /*
     * Gets the table number that this page is associated with
     *
     * @return  The associated table number
     */
    public int getTableNumber() {
        return this.tableNumber;
    }

    public int getPageNumber() {
        return this.pageNumber;
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
        Catalog catalog = Catalog.getCatalog();
        // check if record can fit in this page.
        if ((catalog.getPageSize() - this.computeSize()) < record.computeSize()) {
            return false;
        } else {
            Map<Integer, TableSchema> schemas = catalog.getSchemas();
            TableSchema schema = schemas.get(this.tableNumber);
            int primaryIndex = schema.getPrimaryIndex();
            String primaryType = schema.getAttributes().get(primaryIndex).getDataType();
            Comparator<Record> comparator = recordComparator(primaryIndex, primaryType);
            // Add record
            this.records.add(record);
            // sort
            this.records.sort(comparator);

            this.numRecords++;
            schema.incrementNumPages();
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
    public int computeSize() {
        // Page: numRecord, PageNumber, records..
        int size = Integer.BYTES * 2;
        for (Record record: this.records) {
            size += record.computeSize();
        }
        return size;
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

    /**
     * Creates a comparator to compare records based on the specified primary key index and data type.
     *
     * @param primaryKeyIndex The index of the primary key in each record.
     * @param dataType        The data type to determine the comparison method. Supported types are "integer", "double",
     *                        "boolean", "char(x)", and "varchar(x)" where x represents the length.
     * @return A comparator for comparing records based on the specified primary key index and data type.
     * @throws IllegalArgumentException If an unsupported data type is provided.
     */
    private Comparator<Record> recordComparator(int primaryKeyIndex, String dataType) {
        return (record1, record2) -> {
            Object obj1 = record1.getValues().get(primaryKeyIndex);
            Object obj2 = record2.getValues().get(primaryKeyIndex);

            if (dataType.equalsIgnoreCase("integer")) {
                Integer int1 = (Integer) obj1;
                Integer int2 = (Integer) obj2;
                return int1.compareTo(int2);
            } else if (dataType.equalsIgnoreCase("double")) {
                Double double1 = (Double) obj1;
                Double double2 = (Double) obj2;
                return double1.compareTo(double2);
            } else if (dataType.equalsIgnoreCase("boolean")) {
                Boolean bool1 = (Boolean) obj1;
                Boolean bool2 = (Boolean) obj2;
                return Boolean.compare(bool1, bool2);
            } else if (dataType.toLowerCase().contains("char") || dataType.toLowerCase().contains("varchar")) {
                String str1 = obj1.toString();
                String str2 = obj2.toString();
                return str1.compareTo(str2);
            } else {
                throw new IllegalArgumentException("Unsupported data type: " + dataType);
            }
        };
    }





}
