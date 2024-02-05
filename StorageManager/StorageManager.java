package StorageManager;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.lang.management.MemoryType;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

import StorageManager.Objects.AttributeSchema;
import StorageManager.Objects.Catalog;

import StorageManager.Objects.Page;
import StorageManager.Objects.Record;
import StorageManager.Objects.MessagePrinter.MessageType;

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
     * after a page splits, add the incoming record to the correct page
     * This function compares the last record of the first page
     * with the to be inserted record then determines
     * if the incoming record should be added to the first or second page
     *
     * NOTE: Page.addRecord(Record) will add the page into the
     *       record in the correct sorted position
     *
     *
     * @param page      The page that is to be split
     * @param record    the record to insert
     * @param index     the index of the value to compare
     * @param dataType  the dataType of the values to compare
     *
     *
     * @return          An array of size 2, consisting of 2 pages
     */
    private Page[] pageSplit(Page page, Record record, int index, String dataType) {
        List<Record> records = page.getRecords();
        int size = records.size();
        int startIndex = (int) (Math.ceil(size / 2) - 1);
        Page _new = new Page(startIndex + 1, page.getTableNumber());
        for (int i = startIndex; i < size; i++) {
            _new.addRecord(records.get(startIndex));
            records.remove(startIndex);
        }

        page.setRecords(records);
        page.setNumRecords(records.size());

        page.setChanged();
        _new.setChanged();

        // determnine which page to append the record to
        // compare the end of the first page
        List<Record> firstRecords = page.getRecords();
        Record lastRecord = firstRecords.get(firstRecords.size()-1);
        int comparison = lastRecord.comparison(
                index, record.getValues().get(index), dataType);

        if (comparison == 1) {
            // inputted was less than the last record of the first page
            // add to first page
            page.addRecord(record);
        } else if (comparison == 0) {
            // TODO primary key unique constraint not met error
            return null;
        } else {
            // inputted was greater than the last record
            // of the first page, so add to the second page
            _new.addRecord(record);
        }

        return new Page[] {page, _new};

    }

    /*
     * Construct the full table path according to where
     * the DB is located
     *
     * @param tableNumber   the id of the table
     *
     * @return              the full table path
     */
    private String getTablePath(int tableNumber) {
        String dbLoc = Catalog.getCatalog().getDbLocation();
        return dbLoc + "/tables/" + Integer.toString(tableNumber);

    }

    public Record getRecord(int tableNumber, Object primaryKey) {
        // TODO
        return null;
    }

    public List<Record> getRecords(int tableNumber) {
        //TODO
        return null;
    }

    public void insertRecord(int tableNumber, Record record) {
        String tablePath = this.getTablePath(tableNumber);
        File tableFile = new File(tablePath);
        try {
            // check to see if the file exists, if not create it
            if (!tableFile.exists()) {
                tableFile.createNewFile();
                // create a new page and insert the new record into it
                Page _new = new Page(0, tableNumber);
                _new.addRecord(record);
                // then add the page to the buffer
                this.addBuffer(_new);
            } else {
                // get tableSchema from the catalog
                TableSchema schema = Catalog.getCatalog().getSchema(tableNumber);

                // determine index of the primary key
                int primaryIndex = schema.getPrimaryIndex();
                List<AttributeSchema> attrs = schema.getAttributes();
                String primaryType = attrs.get(primaryIndex).getAttributeName();

                if (primaryIndex == -1) {
                    System.out.print("Error! This relation has no primary key!");
                    return;
                }

                // get primary key of incoming record
                Object primaryKey = record.getValues().get(primaryIndex);

                // read first 8 byte and determine number of pages in table
                byte[] buffer = new byte[8];
                InputStream reader = new FileInputStream(tableFile);
                reader.read(buffer);
                reader.close();
                int numPages = ByteBuffer.wrap(buffer).getInt();

                // if already exists, check constraints
                // check null constraints
                this.checkNullConstraint(schema, record);

                // check unique constraints
                List<Integer> uniqueIndex = new ArrayList<Integer>();
                for (int i = 0; i < attrs.size(); i++) {
                    if (attrs.get(i).isUnique()) {
                        uniqueIndex.add(i);
                    }
                }

                if (uniqueIndex.size() > 0) {
                    this.insertUnique(schema, record, uniqueIndex,
                                    primaryIndex, primaryKey, primaryType);
                } else {
                    
                    //check the buffer to see if
                    // the needed page is already there
                    if (this.insertBuffer(tableNumber, primaryIndex, primaryType, record)) {
                        return;
                    }
                    
                    // if not, start reading from file
                    boolean found = false;
                    int pageIndex = 0;

                    while (!found) {
                        // read a page in
                        Page page = this.readPageHardware(tableNumber, pageIndex);

                        // if this is the last page in the table, append
                        if (numPages == pageIndex+1) {
                            found = true;
                            if (page.addRecord(record)) {
                                // page was added successfully, write it to the buffer
                                this.addBuffer(page);
                            } else {
                                // failed to add, page full
                                Page[] pages = this.pageSplit(page, record, 
                                    pageIndex, primaryType);

                                // write both pages to the buffer
                                this.addBuffer(pages[0]);
                                this.addBuffer(pages[1]);
                            }
                            continue;
                        }

                        // attempt to add to page
                        boolean result = this.insertHelper(page, primaryIndex, primaryKey, 
                                                            primaryType, record);
                        if (!result) {
                            pageIndex++;
                        } else {
                            found = true;
                        }

                    }
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
     * Given a table schema and a record, check
     * to determine if the record conforms to any null
     * constraints
     * 
     * @param table     The table schema
     * @param record    The record to check
     * 
     * @return          true:  pass
     *                  false: fail
     */
    private boolean checkNullConstraint(TableSchema table, Record record) {
        List<AttributeSchema> attrs = table.getAttributes();
        for (int i = 0; i > attrs.size(); i++) {
            if (attrs.get(i).isNotNull()) {
                if (record.getValues().get(i).equals(null)) {
                    return false;
                }
            }
        }
        return true;
    }

    /*
     * Helper function for the insert
     * This function will take in a page and determine if
     * the incoming record should be inserted there
     * 
     * @param page          Page to insert a record to
     * @param record        to be inserted record
     * 
     * @return      boolean indicating of a successful insert
     */
    private boolean insertHelper(Page page, int primaryIndex, Object primaryKey, 
                                String primaryType, Record record) {
        // compare the last record in the page
        List<Record> records = page.getRecords();
        Record lastRecord = records.get(records.size() - 1);
        int comparison = lastRecord.comparison(
                            primaryIndex, primaryKey, primaryType);

        if (comparison == -1) {
            // if inputted is greater, continue
            return false;
        } else if (comparison == 0) {
            // an equal primary key was found, primary key unique conflict found
            // TODO unique conflict with primary key
            return false;
        } else {
            // if inputted is less, add record
            if (page.addRecord(record)) {
                // page was added successfully, write it to the buffer
                this.addBuffer(page);
            } else {
                // if page is full then split
                Page[] pages = this.pageSplit(page, record, 
                        primaryIndex, primaryType);

                // write both pages to the buffer
                this.addBuffer(pages[0]);
                this.addBuffer(pages[1]);
            }
            return true;
        }
    }
    
    /*
     * Checks to see if the buffer has a page where
     * the to be inserted record will be inserted in
     * if so, insert, otherwise return false
     * 
     * @param tableNumber   The table to insert in
     * @param primaryIndex  Location of thr primary key within the tableSchema
     * @param primaryType   The dataType of the primary key
     * @param record        The to be insertted record
     * 
     * @return              boolean indicating result of the isnert
     */
    private boolean insertBuffer(int tableNumber, int primaryIndex, 
                                String primaryType, Record record) {
        // check if buffer has the needed file
        // if so insert and return true
        Page bufferPage = this.checkBuffer(tableNumber, record, true);
        if (bufferPage != null) {
            // if record add was successful update priority
            if (bufferPage.addRecord(record)) {
                bufferPage.setPriority();
            } else {
                // page is full split it
                Page[] pages = this.pageSplit(bufferPage, record, 
                        primaryIndex, primaryType);

                // remove original bufferPage from the buffer
                this.buffer.remove(bufferPage);

                // write both pages to the buffer
                this.addBuffer(pages[0]);
                this.addBuffer(pages[1]);
            }
            return true;
        }
        return false;
    }

    /*
     * Helper function for the insert,
     * Gets called if the relation has a unique attribute
     * Will then call insertHelper 
     */
    private boolean insertUnique(TableSchema table, Record record, List<Integer> uniqueIndex,
                            int primaryIndex, Object primaryKey, String primaryType) {
        // the relation has one or more unique attributes
        // read in the entire relation locally
        List<Page> pages = this.readTableHardware(table.getTableNumber());
        int foundPage = -1;
        List<Object> incomingValues = record.getValues();

        // loop through every page to check if the constraint is met
        for (int i = 0; i < pages.size(); i++) {

            // as we loop through, determine where the record is supposed to be inserted as well
            if (foundPage == -1) {
                // compare the last record in the page
                List<Record> records = pages.get(i).getRecords();
                Record lastRecord = records.get(records.size() - 1);
                int comparison = lastRecord.comparison(
                                    primaryIndex, primaryKey, primaryType);
                if (comparison == 0) {
                    // TODO primaryKey not unique!
                    return false;
                } else if (comparison == 1) {
                    foundPage = i;
                }
            }
            List<Record> records = pages.get(i).getRecords();
            for (Record j : records) {
                for (int o : uniqueIndex) {
                    if (j.comparison(o, incomingValues.get(o), 
                        table.getAttributes().get(o).getDataType()) == 0) {
                        // TODO unique constraint fail
                        return false;
                    }
                }
            }
        }
        // constraints have been passed, attempt insert
        // attempt to insert into a page in the buffer
        if (!this.insertBuffer(table.getTableNumber(), primaryIndex, primaryType, record)) {
            // if not, use the page we found earlier
            Page page = pages.get(foundPage);
            if (page.addRecord(record)) {
                // page was added successfully, write it to the buffer
                this.addBuffer(page);
            } else {
                // if page is full then split
                Page[] splitPages = this.pageSplit(page, record, 
                        primaryIndex, primaryType);

                // write both pages to the buffer
                this.addBuffer(splitPages[0]);
                this.addBuffer(splitPages[1]);
            }
        }
        return true;

    }

    public void deleteRecord(int tableNumber, Object primaryKey) {
        // TODO
    }

    public void updateRecord(int tableNumber, Object primaryKey, Record record) {
        // TODO
    }

    //---------------------------- Page Buffer ------------------------------
    /*
     * Checks the buffer to determine if a needed page
     * is already in the buffer
     *
     * It determines what the needed page is by finding a record
     * with the same primary key on the page
     *
     * @param table     the table to find a page for
     * @param record    the record to search for
     *
     * @return          returns a page if found, otherwise null
     */
    private Page checkBuffer(int table, Record record) {
        // TODO
        return null;
    }

    /*
     * Checks the buffer to determine if a needed page
     * is already in the buffer
     *
     * It determines what the needed page is by finding a page
     * in which the record would fit in
     * (typically used for insertion)
     *
     * @param table         the table to find a page for
     * @param record        the record to search for
     * @param notSpecific   true: find a page where this record should belong in
     *                      false: call the other checkBuffer method
     *
     * @return              returns a page if found, otherwise null
     */
    private Page checkBuffer(int table, Record record, boolean notSpecific) {
        if (!notSpecific) {
            return this.checkBuffer(table, record);
        } else {
            // TODO
            return null;
        }
    }

    private void addBuffer(Page page) {
        if (this.buffer.size() == this.bufferSize) {
            // TODO, write LRU to disk
        }
        page.setPriority();
        this.buffer.add(page);
    }

    private Page readPageHardware(int tableNumber, int pageNumber) {
        // TODO
        // skip first 8 bytes since the first 8 bytes consist of the number of pages in the table
        // construct a new page class, first 8 bytes of a page is a number of records in the page
        return null;
    }

    /*
     * Reads all pages in a table and returns it
     * 
     * @param tableNum  The table to read
     * 
     * @return          The list of all pages
     */
    private List<Page> readTableHardware(int tableNum) {
        // TODO
        return null;
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

    public void dropTable(int tableNumber) {
        //TODO: The method.
    }

    public void alterTable(int tableNumber, String op, String attrName, String attrType, boolean notNull, boolean pKey, boolean unique) {
        //TODO: The method.
    }
}
