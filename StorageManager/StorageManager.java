package StorageManager;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Random;

import StorageManager.Objects.AttributeSchema;
import StorageManager.Objects.Catalog;
import StorageManager.Objects.MessagePrinter;
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
        this.buffer = new PriorityQueue<>(bufferSize, new Page());
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


    /**
     * Splits a page by moving half of its records to a new page.
     *
     * @param page         The page to split.
     * @param record       The record to insert after the split.
     * @param tableSchema  The schema of the table.
     * @throws Exception   If an error occurs during the split operation.
     */
    private void pageSplit(Page page, Record record, TableSchema tableSchema) throws Exception {
        // Create a new page
        Page newPage = new Page(0, tableSchema.getTableNumber(), tableSchema.getNumPages() + 1);

        // Calculate the split index
        int splitIndex = (int) Math.floor(page.getRecords().size() / 2);

        // Move half of the records to the new page
        for (Record copyRecord: page.getRecords().subList(splitIndex, page.getRecords().size())) {
            if(!newPage.addNewRecord(copyRecord)) {
                pageSplit(newPage, copyRecord, tableSchema);
            }
        }

        page.getRecords().subList(splitIndex, page.getRecords().size()).clear();

        // Add the new page to the buffer
        this.addPageToBuffer(newPage);

        // Update page order
        Integer currentPageIndex = tableSchema.getPageOrder().indexOf(page.getPageNumber());
        tableSchema.getPageOrder().add(currentPageIndex, newPage.getPageNumber());
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

    /**
     * Retrieves all records from a specified table.
     *
     * @param tableNumber The number of the table to retrieve records from.
     * @return A list of records from the specified table.
     * @throws Exception If an error occurs during the retrieval process.
    */
    public List<Record> getAllRecords(int tableNumber) throws Exception {
        List<Record> records = new ArrayList<>(); // List to store all records
        List<Page> allPagesForTable = new ArrayList<>();
        Catalog catalog = Catalog.getCatalog();
        TableSchema tableSchema = catalog.getSchema(tableNumber);
        for (Integer pageNumber : tableSchema.getPageOrder()) {
            allPagesForTable.add(this.getPage(tableNumber, pageNumber));
        }

        for (Page page: allPagesForTable) {
            records.addAll(page.getRecords());
        }

        return records;
    }

    private void checkUniqueContraints(Map<Integer, List<Object>> uniqueAttributes, int tableNumber, int primaryKeyIndex, Record newRecord) throws Exception {
        List<Record> records = this.getAllRecords(tableNumber);
        for (Record record : records){
            for (Integer attributeIndex : uniqueAttributes.keySet()) {
                uniqueAttributes.get(attributeIndex).add(record.getValues().get(attributeIndex));
                if (uniqueAttributes.get(attributeIndex).contains(newRecord.getValues().get(attributeIndex))) {
                    if (attributeIndex == primaryKeyIndex) {
                        MessagePrinter.printMessage(MessageType.ERROR, String.format("row (%d): Duplicate %s for row (%d)", records.indexOf(record), "primary key", records.indexOf(record)));
                    } else {
                        MessagePrinter.printMessage(MessageType.ERROR, String.format("row (%d): Duplicate %s for row (%d)", records.indexOf(record), "value", records.indexOf(record)));
                    }
                }
            }
        }


    }

    public void insertRecord(int tableNumber, Record record) throws Exception {
        String tablePath = this.getTablePath(tableNumber);
        File tableFile = new File(tablePath);
        Catalog catalog = Catalog.getCatalog();
        // get tableSchema from the catalog
        TableSchema tableSchema = catalog.getSchema(tableNumber);

            // check to see if the file exists, if not create it
        if (!tableFile.exists()) {
            tableFile.createNewFile();
            // create a new page and insert the new record into it
            Page _new = new Page(0, tableNumber, 1);
            tableSchema.addPageNumber(_new.getPageNumber());
            tableSchema.incrementNumPages();
            _new.addNewRecord(record);
            // then add the page to the buffer
            this.addPageToBuffer(_new);
        } else {

            // determine index of the primary key
            int primaryIndex = tableSchema.getPrimaryIndex();
            List<AttributeSchema> attrs = tableSchema.getAttributes();

            // check unique constraints
            Map<Integer, List<Object>> uniqueAttributes = new HashMap<>();
            for (int i = 0; i < attrs.size(); i++) {
                if (attrs.get(i).isUnique()) {
                    uniqueAttributes.put(i, new ArrayList<>());
                }
            }

             // confirm unique
             if (uniqueAttributes.size() > 0) {
                this.checkUniqueContraints(uniqueAttributes, tableNumber, primaryIndex, record);
            }

            int primaryKeyIndex = tableSchema.getPrimaryIndex();

            for (Integer pageNumber : tableSchema.getPageOrder()) {
                Page page = this.getPage(tableNumber, pageNumber);
                Record firstRecordInPage = page.getRecords().get(0);
                if (record.comapreTo(firstRecordInPage, primaryKeyIndex) < 0) {
                    // record is less than firstRecordInPage
                    if(!page.addNewRecord(record)) {
                        // page was full
                        this.pageSplit(page, record, tableSchema);
                    }
                    break;
                }

                // check if we are at last page
                if (pageNumber == tableSchema.getPageOrder().get(tableSchema.getPageOrder().size() -1)) {
                    if(!page.addNewRecord(record)) {
                        // page was full
                        this.pageSplit(page, record, tableSchema);
                    }
                }
            }
        }
    }

    // /*
    //  * Helper function for the insert
    //  * This function will take in a page and determine if
    //  * the incoming record should be inserted there
    //  *
    //  * @param page          Page to insert a record to
    //  * @param record        to be inserted record
    //  *
    //  * @return      boolean indicating of a successful insert
    //  */
    // private boolean insertHelper(Page page, int primaryIndex, Object primaryKey,
    //                             String primaryType, Record record) throws Exception {
    //     // compare the last record in the page
    //     List<Record> records = page.getRecords();
    //     Record lastRecord = records.get(records.size() - 1);
    //     int comparison = lastRecord.comparison(
    //                         primaryIndex, primaryKey, primaryType);

    //     if (comparison == -1) {
    //         // if inputted is greater, continue
    //         return false;
    //     } else if (comparison == 0) {
    //         // an equal primary key was found, primary key unique conflict found
    //         // TODO unique conflict with primary key
    //         return false;
    //     } else {
    //         // if inputted is less, add record
    //         if (page.addNewRecord(record)) {
    //             // page was added successfully, write it to the buffer
    //             this.addPageToBuffer(page);
    //         } else {
    //             // if page is full then split
    //             Page[] pages = this.pageSplit(page, record,
    //                     primaryIndex, primaryType);

    //             // write both pages to the buffer
    //             this.addPageToBuffer(pages[0]);
    //             this.addPageToBuffer(pages[1]);
    //         }
    //         return true;
    //     }
    // }

    // /*
    //  * Checks to see if the buffer has a page where
    //  * the to be inserted record will be inserted in
    //  * if so, insert, otherwise return false
    //  *
    //  * @param tableNumber   The table to insert in
    //  * @param primaryIndex  Location of thr primary key within the tableSchema
    //  * @param primaryType   The dataType of the primary key
    //  * @param record        The to be insertted record
    //  *
    //  * @return              boolean indicating result of the isnert
    //  */
    // private boolean insertBuffer(int tableNumber, int primaryIndex,
    //                             String primaryType, Record record) throws Exception {
    //     // check if buffer has the needed file
    //     // if so insert and return true
    //     List<Page> foundPages = this.checkBuffer(tableNumber, record, primaryIndex, true);
    //     Page bufferPage = foundPages.get(0);
    //     if (bufferPage != null) {
    //         // if record add was successful update priority
    //         if (!bufferPage.addNewRecord(record)) {
    //             // page is full split it
    //             Page[] pages = this.pageSplit(bufferPage, record,
    //                     primaryIndex, primaryType);

    //             // remove original bufferPage from the buffer
    //             this.buffer.remove(bufferPage);

    //             // write both pages to the buffer
    //             this.addPageToBuffer(pages[0]);
    //             this.addPageToBuffer(pages[1]);
    //         }
    //         return true;
    //     }
    //     return false;
    // }

    // /*
    //  * Helper function for the insert,
    //  * Gets called if the relation has a unique attribute
    //  * Will then call insertHelper
    //  */
    // private boolean insertUnique(TableSchema table, Record record, List<Integer> uniqueIndex,
    //                         int primaryIndex, Object primaryKey, String primaryType) {
    //     // the relation has one or more unique attributes
    //     // read in the entire relation locally
    //     List<Page> pages = this.getAllRecords(table.getTableNumber());
    //     int foundPage = -1;
    //     List<Object> incomingValues = record.getValues();

    //     // loop through every page to check if the constraint is met
    //     for (int i = 0; i < pages.size(); i++) {

    //         // as we loop through, determine where the record is supposed to be inserted as well
    //         if (foundPage == -1) {
    //             // compare the last record in the page
    //             List<Record> records = pages.get(i).getRecords();
    //             Record lastRecord = records.get(records.size() - 1);
    //             int comparison = lastRecord.comparison(
    //                                 primaryIndex, primaryKey, primaryType);
    //             if (comparison == 0) {
    //                 // TODO primaryKey not unique!
    //                 return false;
    //             } else if (comparison == 1) {
    //                 foundPage = i;
    //             }
    //         }
    //         List<Record> records = pages.get(i).getRecords();
    //         for (Record j : records) {
    //             for (int o : uniqueIndex) {
    //                 if (j.comparison(o, incomingValues.get(o),
    //                     table.getAttributes().get(o).getDataType()) == 0) {
    //                     // TODO unique constraint fail
    //                     return false;
    //                 }
    //             }
    //         }
    //     }
    //     // constraints have been passed, attempt insert
    //     // attempt to insert into a page in the buffer
    //     if (!this.insertBuffer(table.getTableNumber(), primaryIndex, primaryType, record)) {
    //         // if not, use the page we found earlier
    //         Page page = pages.get(foundPage);
    //         if (page.addNewRecord(record)) {
    //             // page was added successfully, write it to the buffer
    //             this.addPageToBuffer(page);
    //         } else {
    //             // if page is full then split
    //             Page[] splitPages = this.pageSplit(page, record,
    //                     primaryIndex, primaryType);

    //             // write both pages to the buffer
    //             this.addPageToBuffer(splitPages[0]);
    //             this.addPageToBuffer(splitPages[1]);
    //         }
    //     }
    //     return true;

    // }

    /*
     *
     * @return      A triple consisting of:
     *                  (1): Page the record was found in
     *                  (2): The index the record was found in
     *                  (3): boolean indicating if found in buffer
     */
    // private Triple<Page, Integer, Boolean> findRecord(int tableNumber, Record record) {
    //     // TODO
    //     // copy and apste deleteRecord here
    //     // this will find a specific record based on the primary key
    //     // then return the page it was founded on, as well as the index
    //     // the record is at
    //     TableSchema schema = this.getTableSchema(tableNumber);
    //     int primaryIndex = schema.getPrimaryIndex();
    //     Object primaryKey = record.getValues().get(primaryIndex);
    //     String primaryType = schema.getAttributes().get(primaryIndex).getDataType();
    //     List<Page> bufferPages = this.checkBuffer(tableNumber, record, primaryIndex, false);
    //     Page foundPage = null;
    //     int foundIndex = -1;


    //     if (bufferPages.size() > 0) {
    //         // find the page that the record is in
    //         for (Page page : bufferPages) {
    //             List<Record> pageRecords = page.getRecords();
    //             Record lastRecord = pageRecords.get(pageRecords.size()-1);
    //             int comparison = lastRecord.comparison(primaryIndex, primaryKey, primaryType);

    //             // if th elast record of current page is the record
    //             // we are looking for, delete it
    //             if (comparison == 0) {
    //                 foundPage = page;
    //                 foundIndex = pageRecords.size()-1;
    //                 break;
    //             } else if (comparison == 1) {
    //                 foundPage = page;
    //                 break;
    //             }
    //         }

    //         if (foundPage.equals(null)) {
    //             // TODO error, this should not have happened, check buffer failed
    //         }

    //         // loop through every record of the found page
    //         // delete the record once we find it
    //         if (foundIndex != -1) {
    //             foundIndex = this.findRecordHelper(foundPage, primaryIndex, primaryKey, primaryType);
    //         }

    //         if (foundIndex == -1) {
    //             // TODO error, this should not have happened, check buffer failed
    //         }

    //         return new Triple<Page,Integer, Boolean>(foundPage, foundIndex, true);

    //     } else {
    //         String tablePath = this.getTablePath(tableNumber);
    //         File tableFile = new File(tablePath);
    //         try {
    //             // read first 8 byte and determine number of pages in table
    //             byte[] buffer = new byte[8];
    //             InputStream reader = new FileInputStream(tableFile);
    //             reader.read(buffer);
    //             reader.close();
    //             int numPages = ByteBuffer.wrap(buffer).getInt();

    //             // start reading pages
    //             // get page order
    //             List<Integer> pageOrder = schema.getPageOrder();

    //             // find the correct page
    //             for (Integer pageIndex : pageOrder) {
    //                 Page page = this.readPageHardware(tableNumber, pageIndex);

    //                 // compare last record un page
    //                 List<Record> foundRecords = page.getRecords();
    //                 Record lastRecord = foundRecords.getLast();
    //                 int comparison = lastRecord.comparison(primaryIndex, primaryKey, primaryType);
    //                 if (comparison == 0) {
    //                     // found the record
    //                     foundPage = page;
    //                     foundIndex = foundRecords.size()-1;
    //                     break;
    //                 } else if (comparison == 1) {
    //                     // found the correct page
    //                     foundPage = page;
    //                     break;
    //                 } else {
    //                     // page was not found, continue
    //                     continue;
    //                 }
    //             }

    //             if (foundPage.equals(null)) {
    //                 // no page was found, meaning no record exists
    //                 return new Triple<Page,Integer,Boolean>(null, null, null);
    //             }

    //             if (foundIndex == -1) {
    //                 // no index was found yet
    //                 foundIndex = this.findRecordHelper(foundPage, primaryIndex, primaryKey, primaryType);
    //             }

    //             // if no record was found then foundIndex == -1
    //             return new Triple<Page,Integer,Boolean>(foundPage, foundIndex, false);
    //         } catch (Exception e) {
    //             System.out.println(e.getStackTrace());
    //             return new Triple<Page,Integer,Boolean>(null, -1, null);
    //         }
    //     }
    // }

    /*
     * Finds the index of a record within a page
     */
    // public Integer findRecordHelper(Page page, int primaryIndex,
    //                                             Object primaryKey, String primaryType) {
    //     int foundIndex = -1;
    //     List<Record> foundRecords = page.getRecords();
    //     for (int i = 0; i < foundRecords.size(); i++) {
    //         Record bufferRecord = foundRecords.get(i);
    //         int comparison = bufferRecord.comparison(primaryIndex, primaryKey, primaryType);
    //         if (comparison == 0) {
    //             foundIndex = i;
    //             break;
    //         }
    //     }
    //     return foundIndex;
    // }

    public void deleteRecord(int tableNumber, Record record) throws Exception {
        // Triple<Page, Integer, Boolean> found = this.findRecord(tableNumber, record);
        // Page foundPage = found.first;
        // int foundIndex = found.second;
        // boolean buffer = found.third;

        // if (foundIndex == -1) {
        //     // TODO
        //     // no record found
        //     return;
        // }

        // foundPage.deleteRecord(foundIndex);
        // if (!buffer) {
        //     this.addPageToBuffer(foundPage);
        // }
    }
    public void updateRecord(int tableNumber, Record record) throws Exception {
        // Triple<Page, Integer, Boolean> found = this.findRecord(tableNumber, record);
        // Page foundPage = found.first;
        // int foundIndex = found.second;
        // boolean buffer = found.third;

        // if (foundIndex == -1) {
        //     // TODO
        //     // no record found
        //     return;
        // }

        // foundPage.updateRecord(foundIndex, record);
        // if (!buffer) {
        //     this.addPageToBuffer(foundPage);
        // }
    }

    //---------------------------- Page Buffer ------------------------------

    private Page getLastPageInBuffer(PriorityQueue<Page> buffer) {
        Page[] bufferArray = (Page[]) buffer.toArray();
        return bufferArray[bufferArray.length - 1];
    }

    @Override
    public Page getPage(int tableNumber, int pageNumber) throws Exception {
        // check if page is in buffer
        for (Page page: this.buffer) {
            if (page.getTableNumber() == tableNumber && page.getPageNumber() == pageNumber) {
                page.setPriority();
                return page;
            }
        }

        // read page from hardware into buffer
        readPageHardware(tableNumber, pageNumber);
        return getLastPageInBuffer(this.buffer);

    }
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
    // private List<Page> checkBuffer(int table, Record record, int attributeIndex) {
    //     // TODO
    //     List<Page> pagesFound = new ArrayList<>();
    //     String dataType = this.getTableSchema(table).getAttributes()
    //                                 .get(attributeIndex).getAttributeName();
    //     Object incomingVal = record.getValues().get(attributeIndex);
    //     // loop through each page in the buffer
    //     for (Page page : this.buffer) {
    //         // if the page is from the same table
    //         if (page.getTableNumber() == table && !pagesFound.contains(page)) {
    //             for (Record i : page.getRecords()) {
    //                 if (i.comparison(attributeIndex, incomingVal, dataType) == 0) {
    //                     pagesFound.add(page);
    //                     break;
    //                 }
    //             }
    //         }
    //     }
    //     // loop through each record in the page to determine if the record is int
    //     return null;
    // }

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
    // private List<Page> checkBuffer(int table, Record record, int attributeIndex, boolean notSpecific) {
    //     if (!notSpecific) {
    //         return this.checkBuffer(table, record, attributeIndex);
    //     } else {
    //         List<Page> pagesFound = new ArrayList<>();
    //         // loop through each page in the buffer
    //         for (Page page : this.buffer) {
    //             // if the page is from th esame table
    //             if (page.getTableNumber() == table) {
    //                 // compare last record in the page
    //                 Record last = page.getRecords().getLast();
    //                 TableSchema schema = this.getTableSchema(table);
    //                 // if the last record is greater than incoming record, check the first record
    //                 if (last.comparison(attributeIndex,
    //                                     record.getValues().get(attributeIndex),
    //                                     schema.getAttributes().get(attributeIndex)
    //                                                             .getDataType()) == 1) {

    //                     Record first = page.getRecords().getFirst();
    //                     // if incoming record is greater than the first record
    //                     // return page
    //                     if (first.comparison(attributeIndex,
    //                                     record.getValues().get(attributeIndex),
    //                                     schema.getAttributes().get(attributeIndex)
    //                                                             .getDataType()) == -1) {
    //                         pagesFound.add(page);
    //                         return pagesFound;
    //                     }
    //                 }
    //             }
    //         }
    //         return null;
    //     }
    // }
    private void readPageHardware(int tableNumber, int pageNumber) throws Exception {
        Catalog catalog = Catalog.getCatalog();
        TableSchema tableSchema = catalog.getSchema(tableNumber);
        String filePath = this.getTablePath(tableNumber);
        File tableFile = new File(filePath);
        RandomAccessFile tableAccessFile = new RandomAccessFile(tableFile, "r");
        int pageIndex = pageNumber - 1;

        tableAccessFile.seek(Integer.BYTES + (catalog.getPageSize() * pageIndex)); // start after numPages
        int numRecords = tableAccessFile.readInt();
        int pageNum = tableAccessFile.readInt();
        Page page = new Page(numRecords, tableNumber, pageNum);
        page.readFromHardware(tableAccessFile, tableSchema);
        this.addPageToBuffer(page);
        tableAccessFile.close();
    }

    private void writePageHardware(Page page) throws Exception {
        Catalog catalog = Catalog.getCatalog();
        TableSchema tableSchema = catalog.getSchema(page.getTableNumber());
        String filePath = this.getTablePath(page.getTableNumber());
        File tableFile = new File(filePath);
        RandomAccessFile tableAccessFile = new RandomAccessFile(tableFile, "rw");
        tableAccessFile.writeInt(tableSchema.getNumPages());
        int pageIndex = page.getPageNumber() - 1;

        // Go to the point where the page is in the file
        tableAccessFile.seek(tableAccessFile.getFilePointer() + (catalog.getPageSize() * pageIndex));

        // Allocate space for a Page in the table file
        Random random = new Random();
        byte[] buffer = new byte[catalog.getPageSize()];
        random.nextBytes(buffer);
        tableAccessFile.write(buffer, 0, catalog.getPageSize());

        page.writeToHardware(tableAccessFile);
        tableAccessFile.close();
    }

    private void addPageToBuffer(Page page) throws Exception {
        if (this.buffer.size() > this.bufferSize) {
            this.writePageHardware(this.buffer.poll()); // assuming the first Page in the buffer is LSU
        } else {
            this.buffer.add(page);
        }
    }

    public void writeAll() throws Exception {
        for (Page page : buffer) {
            if (page.isChanged()) {
                writePageHardware(page);
            }
        }
        this.buffer.removeAll(buffer);
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

    public Exception alterTable(int tableNumber, String op, String attrName, String attrType,Object val, boolean notNull, boolean pKey, boolean unique) {
        try {

            Catalog catalog = Catalog.getCatalog();
            TableSchema currentSchemea = catalog.getSchema(tableNumber);

            for(int i = 1; i<=currentSchemea.getNumPages(); i++) {
                    Page currentPage = getPage(tableNumber, i);
                    Page newPage = new Page(currentPage.getNumRecords(), tableNumber, i);
                    List<Record> currentPageRecords = currentPage.getRecords();
                    List<Record> newPageRecords = new ArrayList<>();
                    List<Object> newVals;
                    for (int j = 0; j < currentPageRecords.size(); j++) {

                        List<Object> oldVals = currentPageRecords.get(j).getValues();
                        newVals = oldVals;
                        if(op.equals("add")) {
                            newVals.add(new Object());
                            if(val!=null){
                                newVals.set(-1,val);
                            }
                        }else if(op.equals("drop")){
                            for(int k=0; k<currentSchemea.getAttributes().size(); k++ ){
                                if(currentSchemea.getAttributes().get(k).getAttributeName().equals(attrName)){
                                    newVals.remove(k);
                                }
                            }

                        }else{
                            throw new Exception("unknown op");
                        }
                        newPageRecords.add(new Record(newVals));
                    }
                    buffer.remove(currentPage);
                    buffer.add(newPage);

            }


        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return null;
    }
}
