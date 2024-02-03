package StorageManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.PriorityQueue;

import StorageManager.Objects.Attribute;
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
     * after a page splits, add the incoming record to the correct page
     * This function compares the last record of the first page
     * with the to be inserted record then determines
     * if the incoming record should be added to the first or second page
     * 
     * NOTE: Page.addRecord(Record) will add the page into the 
     *       record in the correct sorted position
     * 
     * @param page  The page that is to be split
     * @param record    the record to insert
     * @param index     the index of the value to compare
     * @param dataType  the dataType of the values to compare
     *  
     * @return      An array of size 2, consisting of 2 pages
     */
    private Page[] pageSplit(Page page, Record record, int index, String dataType) {
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
        } else {
            // inputted was either equal to, or greater than the last record
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
                int primaryIndex = -1;
                List<Attribute> attrs = schema.getAttributes();
                for (int i = 0; i < attrs.size(); i++) {
                    if (attrs.get(i).isPrimaryKey()) {
                        primaryIndex = i;
                    }
                }

                if (primaryIndex == -1) {
                    System.out.print("Error! This relation has no primary key!");
                    return;
                }

                // if already exists, check the buffer to see if
                // the needed page is already there
                
                Page bufferPage = this.checkBuffer(tableNumber, record, true);
                if (bufferPage != null) {
                    // if record add was successful update priority
                    if (bufferPage.addRecord(record)) {
                        bufferPage.setPriority();
                    } else {
                        // page is full split it
                        Page[] pages = this.pageSplit(bufferPage, record, primaryIndex,
                                attrs.get(primaryIndex).getAttributeName());

                        // remove original bufferPage from the buffer
                        this.buffer.remove(bufferPage);

                        // write both pages to the buffer
                        this.addBuffer(pages[0]);
                        this.addBuffer(pages[1]);
                    }
                    return;
                }

                // if not, start reading from file
                boolean found = false;
                int pageIndex = 0;

                // get primary key of incoming record
                Object primaryKey = record.getValues().get(primaryIndex);
                
                // read first 8 byte and determine number of pages in table
                byte[] buffer = new byte[8];
                InputStream reader = new FileInputStream(tableFile);
                reader.read(buffer);
                reader.close();
                int numPages = ByteBuffer.wrap(buffer).getInt();

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
                            Page[] pages = this.pageSplit(page, record, pageIndex,
                                attrs.get(primaryIndex).getAttributeName());
                            
                            // write both pages to the buffer
                            this.addBuffer(pages[0]);
                            this.addBuffer(pages[1]);
                        }
                        continue;
                    }

                    // compare the last record in the page
                    List<Record> records = page.getRecords();
                    Record lastRecord = records.get(records.size() - 1);
                    int comparison = lastRecord.comparison(
                                        primaryIndex, primaryKey, 
                                        attrs.get(primaryIndex).getAttributeName());
                    
                    if (comparison == -1 || comparison == 0) {
                        // if inputted is greater or equal to, continue
                        pageIndex++;
                        continue;
                    } else {
                        found = true;
                        // if inputted is less, add record
                        if (page.addRecord(record)) {
                            // page was added successfully, write it to the buffer
                            this.addBuffer(page);
                        } else {
                            // if page is full then split
                            Page[] pages = this.pageSplit(page, record, primaryIndex,
                                    attrs.get(primaryIndex).getAttributeName());
  
                            // write both pages to the buffer
                            this.addBuffer(pages[0]);
                            this.addBuffer(pages[1]);
                        }
                    }

                }
                
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
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
     * @return  returns a page if found, otherwise null
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
     * @return  returns a page if found, otherwise null
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
            // TODO make sure what is being removed is the LRU
            this.writePageHardware(this.buffer.remove());
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
