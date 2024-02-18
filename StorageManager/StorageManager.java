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
import StorageManager.Objects.Utility.Triple;

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

    private TableSchema getTableSchema(int tableNum) {
        Catalog catalog = Catalog.getCatalog();
        return catalog.getSchema(tableNum);
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
        Page _new = new Page(0, page.getTableNumber());
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
        // start, read in how many pages there are
        // loop through each page
        // compare last record of the page with primary key
        // if primaryKey is greater, continue, otherwise,
        // otherwise loop through current page
        return null;
    }

    public List<Record> getAllRecords(int tableNumber) {
        //TODO
        // start, read in ho wmany pages there are
        // loop through each page
        // append every record found
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
                TableSchema schema = this.getTableSchema(tableNumber);

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
        List<Page> foundPages = this.checkBuffer(tableNumber, record, primaryIndex, true);
        Page bufferPage = foundPages.get(0);
        if (bufferPage != null) {
            // if record add was successful update priority
            if (!bufferPage.addRecord(record)) {
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

    /*
     * 
     * @return      A triple consisting of:
     *                  (1): Page the record was found in
     *                  (2): The index the record was found in
     *                  (3): boolean indicating if found in buffer
     */
    private Triple<Page, Integer, Boolean> findRecord(int tableNumber, Record record) {
        // TODO
        // copy and apste deleteRecord here
        // this will find a specific record based on the primary key
        // then return the page it was founded on, as well as the index
        // the record is at
        TableSchema schema = this.getTableSchema(tableNumber);
        int primaryIndex = schema.getPrimaryIndex();
        Object primaryKey = record.getValues().get(primaryIndex);
        String primaryType = schema.getAttributes().get(primaryIndex).getDataType();
        List<Page> bufferPages = this.checkBuffer(tableNumber, record, primaryIndex, false);
        Page foundPage = null;
        int foundIndex = -1;
        

        if (bufferPages.size() > 0) {
            // find the page that the record is in
            for (Page page : bufferPages) {
                List<Record> pageRecords = page.getRecords();
                Record lastRecord = pageRecords.get(pageRecords.size()-1);
                int comparison = lastRecord.comparison(primaryIndex, primaryKey, primaryType);

                // if th elast record of current page is the record
                // we are looking for, delete it
                if (comparison == 0) {
                    foundPage = page;
                    foundIndex = pageRecords.size()-1;
                    break;
                } else if (comparison == 1) {
                    foundPage = page;
                    break;
                }
            }

            if (foundPage.equals(null)) {
                // TODO error, this should not have happened, check buffer failed
            }
            
            // loop through every record of the found page
            // delete the record once we find it
            if (foundIndex != -1) {
                foundIndex = this.findRecordHelper(foundPage, primaryIndex, primaryKey, primaryType);
            }

            if (foundIndex == -1) {
                // TODO error, this should not have happened, check buffer failed
            }
            
            return new Triple<Page,Integer, Boolean>(foundPage, foundIndex, true);

        } else {
            String tablePath = this.getTablePath(tableNumber);
            File tableFile = new File(tablePath);
            try {
                // read first 8 byte and determine number of pages in table
                byte[] buffer = new byte[8];
                InputStream reader = new FileInputStream(tableFile);
                reader.read(buffer);
                reader.close();
                int numPages = ByteBuffer.wrap(buffer).getInt();

                // start reading pages
                // get page order
                List<Integer> pageOrder = schema.getPageOrder();

                // find the correct page
                for (Integer pageIndex : pageOrder) {
                    Page page = this.readPageHardware(tableNumber, pageIndex);

                    // compare last record un page
                    List<Record> foundRecords = page.getRecords();
                    Record lastRecord = foundRecords.getLast();
                    int comparison = lastRecord.comparison(primaryIndex, primaryKey, primaryType);
                    if (comparison == 0) {
                        // found the record
                        foundPage = page;
                        foundIndex = foundRecords.size()-1;
                        break;
                    } else if (comparison == 1) {
                        // found the correct page
                        foundPage = page;
                        break;
                    } else {
                        // page was not found, continue
                        continue;
                    }
                }

                if (foundPage.equals(null)) {
                    // no page was found, meaning no record exists
                    return new Triple<Page,Integer,Boolean>(null, null, null);
                }

                if (foundIndex == -1) {
                    // no index was found yet
                    foundIndex = this.findRecordHelper(foundPage, primaryIndex, primaryKey, primaryType);
                }

                // if no record was found then foundIndex == -1
                return new Triple<Page,Integer,Boolean>(foundPage, foundIndex, false);
            } catch (Exception e) {
                System.out.println(e.getStackTrace());
                return new Triple<Page,Integer,Boolean>(null, -1, null);
            }
        }
    } 

    /*
     * Finds the index of a record within a page
     */
    public Integer findRecordHelper(Page page, int primaryIndex, 
                                                Object primaryKey, String primaryType) {
        int foundIndex = -1;
        List<Record> foundRecords = page.getRecords();
        for (int i = 0; i < foundRecords.size(); i++) {
            Record bufferRecord = foundRecords.get(i);
            int comparison = bufferRecord.comparison(primaryIndex, primaryKey, primaryType);
            if (comparison == 0) {
                foundIndex = i;
                break;
            }
        }
        return foundIndex;
    }

    public void deleteRecord(int tableNumber, Record record) {
        Triple<Page, Integer, Boolean> found = this.findRecord(tableNumber, record);
        Page foundPage = found.first;
        int foundIndex = found.second;
        boolean buffer = found.third;

        if (foundIndex == -1) {
            // TODO
            // no record found
            return;
        }

        foundPage.deleteRecord(foundIndex);
        if (!buffer) {
            this.addBuffer(foundPage);
        }
    }
    public void updateRecord(int tableNumber, Record record) {
        Triple<Page, Integer, Boolean> found = this.findRecord(tableNumber, record);
        Page foundPage = found.first;
        int foundIndex = found.second;
        boolean buffer = found.third;

        if (foundIndex == -1) {
            // TODO
            // no record found
            return;
        }

        foundPage.updateRecord(foundIndex, record);
        if (!buffer) {
            this.addBuffer(foundPage);
        }
    }

    //---------------------------- Page Buffer ------------------------------
    /*
     * Checks the buffer to determine if a needed page
     * is already in the buffer
     *
     * It determines what the needed page is by finding a record
     * with the same value on the page
     *
     * @param table             the table to find a page for
     * @param record            the record to search for
     * @param attributeIndex    the index of the attribute to consider
     *
     * @return                  returns a list of pages if found, otherwise null
     */
    private List<Page> checkBuffer(int table, Record record, int attributeIndex) {
        // TODO
        List<Page> pagesFound = new ArrayList<>();
        String dataType = this.getTableSchema(table).getAttributes()
                                    .get(attributeIndex).getAttributeName();
        Object incomingVal = record.getValues().get(attributeIndex);
        // loop through each page in the buffer
        for (Page page : this.buffer) {
            // if the page is from the same table
            if (page.getTableNumber() == table && !pagesFound.contains(page)) {
                for (Record i : page.getRecords()) {
                    if (i.comparison(attributeIndex, incomingVal, dataType) == 0) {
                        pagesFound.add(page);
                        break;
                    }
                }
            }
        }
        // loop through each record in the page to determine if the record is int
        return null;
    }

    /*
     * Checks the buffer to determine if a needed page
     * is already in the buffer
     *
     * It determines what the needed page is by finding a page
     * in which the record would fit in
     * (typically used for insertion, or finding some record based on an order)
     *
     * @param table             the table to find a page for
     * @param record            the record to search for
     * @param attributeIndex    the index of the attribute to consider
     * @param notSpecific       true: find a page where this record should belong in
     *                          false: call the other checkBuffer method
     *
     * @return                  returns a list of pages if found, otherwise null
     *                          if notsSpecific is true, the list of size 1
     */
    private List<Page> checkBuffer(int table, Record record, int attributeIndex, boolean notSpecific) {
        if (!notSpecific) {
            return this.checkBuffer(table, record, attributeIndex);
        } else {
            List<Page> pagesFound = new ArrayList<>();
            // loop through each page in the buffer
            for (Page page : this.buffer) {
                // if the page is from th esame table
                if (page.getTableNumber() == table) {
                    // compare last record in the page
                    Record last = page.getRecords().getLast();
                    TableSchema schema = this.getTableSchema(table);
                    // if the last record is greater than incoming record, check the first record
                    if (last.comparison(attributeIndex, 
                                        record.getValues().get(attributeIndex), 
                                        schema.getAttributes().get(attributeIndex)
                                                                .getDataType()) == 1) {
                        
                        Record first = page.getRecords().getFirst();
                        // if incoming record is greater than the first record
                        // return page
                        if (first.comparison(attributeIndex, 
                                        record.getValues().get(attributeIndex), 
                                        schema.getAttributes().get(attributeIndex)
                                                                .getDataType()) == -1) {
                            pagesFound.add(page);
                            return pagesFound;
                        }
                    }
                }
            }
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
        //TODO: Current Implementation assumes that if the table exists in the buffer, it DOES not exist as a file and
        //TODO: Vice Versa.

        //LOOP THROUGH THE BUFFER
        //DELETE ALL PAGES IN THE BUFFER THAT REFERENCE A TABLE WE ARE DROPPING.

        //Checks the hardware for a tablefile. If it finds it remove it.
        String tablePath = this.getTablePath(tableNumber);
        File tableFile = new File(tablePath);
        try {
            if (tableFile.exists()) {
                tableFile.delete();

                for (Page page : this.buffer) {
                    if(tableNumber == page.getTableNumber()){
                        buffer.remove(page);
                    }
                }
                /*
                Page bufferPage = this.checkBuffer(tableNumber, null, true);
                while(bufferPage!=null){
                    buffer.remove(bufferPage);
                    bufferPage = this.checkBuffer(tableNumber, null, true);
                }

                 */
            }else {

            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public void alterTable(int tableNumber, String op, String attrName, String attrType, boolean notNull, boolean pKey, boolean unique) {
        //TODO: The method.

        //NOTES FROM PROFESSOR:
        //WHEN MAKING A NEW COLUMN, MAKE A NEW TABLE AND LOOP THROUGH ADDING ALL THE ATTRS FROM THE OLD TABLE THEN
        //DELETE THE OLD TABLE.
        String tablePath = this.getTablePath(tableNumber);
        File tableFile = new File(tablePath);
        try {
            if (tableFile.exists()) {
                /*
                //TODO: Import File as a table
                //tableFile.delete();
                if(op.equals("add")){
                    //TODO: Add the specified attr to the table
                }else if(op.equals("drop")){
                    //TODO: Find the specified attr and drop it
                }else{
                    throw new Exception("Invalid: Alter Table Operation.");
                }

                Page bufferPage = this.checkBuffer(tableNumber, null, true);
                while(bufferPage!=null){
                    //TODO: Get the table from the buffer.
                    if(op.equals("add")){
                        //TODO: Add the specified attr to the table.
                    }else if(op.equals("drop")){
                        //TODO: Find the specified attr and drop it.
                    }else{
                        throw new Exception("Invalid: Alter Table Operation");
                    }

                    //buffer.remove(bufferPage);
                    bufferPage = this.checkBuffer(tableNumber, null, true);
                
                }
                 */
            }else {

            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
