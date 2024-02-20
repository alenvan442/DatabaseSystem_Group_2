package StorageManager;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
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
    private void pageSplit(Page page, Record record, TableSchema tableSchema, int primaryKeyIndex) throws Exception {
        // Create a new page
        Page newPage = new Page(0, tableSchema.getTableNumber(), tableSchema.getNumPages() + 1);

        // Calculate the split index
        int splitIndex = (int) Math.floor(page.getRecords().size() / 2);

        // Move half of the records to the new page
        for (Record copyRecord: page.getRecords().subList(splitIndex, page.getRecords().size())) {
            if(!newPage.addNewRecord(copyRecord)) {
                pageSplit(newPage, copyRecord, tableSchema, primaryKeyIndex);
            }
        }

        page.getRecords().subList(splitIndex, page.getRecords().size()).clear();

        // decide what page to add record to
        Record lastRecordInCurrPage = page.getRecords().get(page.getRecords().size() - 1);
        if (record.compareTo(lastRecordInCurrPage, primaryKeyIndex) < 0) {
            // record is less than lastRecord in page
            if (!page.addNewRecord(record)) {
                pageSplit(page, record, tableSchema, primaryKeyIndex);
            }
        } else {
            if (!newPage.addNewRecord(record)) {
                pageSplit(newPage, record, tableSchema, primaryKeyIndex);
            }
        }

        page.setChanged();

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

    public Record getRecord(int tableNumber, Object primaryKey) throws Exception {
        // used for selecting based on primary key
        Catalog catalog = Catalog.getCatalog();
        TableSchema schema = catalog.getSchema(tableNumber);
        int primaryKeyIndex = schema.getPrimaryIndex();
        List<Integer> pageOrder = schema.getPageOrder();
        Page foundPage = null;

        for (int i : pageOrder) {
            Page page = this.getPage(tableNumber, i);
            Record lastRecord = page.getRecords().get(page.getRecords().size() - 1);
            int comparison = lastRecord.compareTo(primaryKey, primaryKeyIndex);

            if (comparison == 0) {
                // found the record, return it
                return lastRecord;
            } else if (comparison < 0) {
                // found the correct page
                foundPage = page;
                break;
            } else {
                // record was not found, continue
                continue;
            }
        }

        if (foundPage.equals(null)) {
            // a page with the record was not found
            return null;
        } else {
            List<Record> records = foundPage.getRecords();
            for (Record i : records) {
                if (i.compareTo(primaryKey, primaryKeyIndex) == 0) {
                    return i;
                }
            }
            // record was not found
            return null;
        }

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

    private void checkUniqueContraints(List<Integer> uniqueAttributeIndexes, int tableNumber, int primaryKeyIndex, Record newRecord) throws Exception {
        List<Record> records = this.getAllRecords(tableNumber);
        for (Record record : records){
            for (Integer attributeIndex : uniqueAttributeIndexes) {
                if (newRecord.compareTo(record, attributeIndex) == 0) {
                    if (attributeIndex == primaryKeyIndex) {
                        MessagePrinter.printMessage(MessageType.ERROR, String.format("row (%d): Duplicate %s for row (%d)", printRow(record), "primary key", printRow(record)));
                    } else {
                        MessagePrinter.printMessage(MessageType.ERROR, String.format("row (%d): Duplicate %s for row (%d)", printRow(record), "value", printRow(record)));
                    }
                }
            }
        }
    }

    private String printRow(Record record) {
        StringBuilder row = new StringBuilder();
        Boolean addSpace = false;
        for (Object value: record.getValues()) {
          if (addSpace) row.append(" ");
          row.append(value.toString());
          addSpace = true;
        }
        return row.toString();
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
            int primaryKeyIndex = tableSchema.getPrimaryIndex();
            List<AttributeSchema> attrs = tableSchema.getAttributes();

            // check unique constraints
            List<Integer> uniqueAttributes = new ArrayList<>();
            for (int i = 0; i < attrs.size(); i++) {
                if (attrs.get(i).isUnique()) {
                    uniqueAttributes.add(i);
                }
            }

            // confirm unique
            if (uniqueAttributes.size() > 0) {
                this.checkUniqueContraints(uniqueAttributes, tableNumber, primaryKeyIndex, record);
            }

            for (Integer pageNumber : tableSchema.getPageOrder()) {
                Page page = this.getPage(tableNumber, pageNumber);
                Record lastRecordInPage = page.getRecords().get(page.getRecords().size() - 1);
                if (record.compareTo(lastRecordInPage, primaryKeyIndex) < 0) {
                    // record is less than lastRecordPage
                    if(!page.addNewRecord(record)) {
                        // page was full
                        this.pageSplit(page, record, tableSchema, primaryKeyIndex);
                    }
                    break;
                }

                // check if we are at last page
                if (pageNumber == tableSchema.getPageOrder().get(tableSchema.getPageOrder().size() -1)) {
                    if(!page.addNewRecord(record)) {
                        // page was full
                        this.pageSplit(page, record, tableSchema, primaryKeyIndex);
                    }
                }
            }
        }
    }

    public Page deleteRecord(int tableNumber, Record record) {

        TableSchema schema = Catalog.getCatalog().getSchema(tableNumber);
        int primaryIndex = schema.getPrimaryIndex();
        Page foundPage = null;

        try {
            // start reading pages
            // get page order
            List<Integer> pageOrder = schema.getPageOrder();

            // find the correct page
            for (Integer pageIndex : pageOrder) {
                Page page = this.getPage(tableNumber, pageIndex);

                // compare last record in page
                List<Record> foundRecords = page.getRecords();
                Record lastRecord = foundRecords.get(page.getRecords().size() - 1);
                int comparison = lastRecord.compareTo(record, primaryIndex);
                if (comparison == 0) {
                    // found the record, delete it
                    page.deleteRecord(page.getNumRecords()-1);
                    return page;
                } else if (comparison > 0) {
                    // found the correct page
                    foundPage = page;
                    break;
                } else {
                    // page was not found, continue
                    continue;
                }
            }

            if (foundPage.equals(null)) {
                MessagePrinter.printMessage(MessageType.ERROR,
                    String.format("No record of primary key: (%d), was found.", record.getValues().get(primaryIndex)));
                return null;
            } else {
                // a page was found but deletion has yet to happen
                List<Record> recordsInFound = foundPage.getRecords();
                for (int i = 0; i < recordsInFound.size(); i++) {
                    if (record.compareTo(recordsInFound.get(i), primaryIndex) == 0) {
                        foundPage.deleteRecord(i);
                        return foundPage;
                    }
                }
                MessagePrinter.printMessage(MessageType.ERROR,
                    String.format("No record of primary key: (%d), was found.", record.getValues().get(primaryIndex)));
                return null;
            }

        } catch (Exception e) {
            System.out.println(e.getStackTrace());
            return null;
        }

    }

    public void updateRecord(int tableNumber, Record record) throws Exception {

        Page deletePage = this.deleteRecord(tableNumber, record); // if the delete was successful then deletePage != null

        if (deletePage.equals(null)) {
            // no record found
            // error message was already thrown in deletePage
            return;
        }

        try {
            this.insertRecord(tableNumber, record);
        } catch (Exception e) {
            // insert failed, restore the deleted record
            deletePage.addNewRecord(record);

        }

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
        tableAccessFile.seek(tableAccessFile.getFilePointer() -  catalog.getPageSize()); // move pointer back

        page.writeToHardware(tableAccessFile);
        tableAccessFile.close();
    }

    private void addPageToBuffer(Page page) throws Exception {
        if (this.buffer.size() > this.bufferSize) {
            Page lruPage = this.buffer.poll(); // assuming the first Page in the buffer is LRU
            if (page.isChanged()) {
                this.writePageHardware(lruPage);
            }
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

    /**
     * Method to drop whole tables from the DB
     * @param tableNumber - the tablenumber for the table we are removing
     */
    public void dropTable(int tableNumber) {

        //Checks the hardware for a tablefile. If it finds it remove it.
        String tablePath = this.getTablePath(tableNumber);
        File tableFile = new File(tablePath);
        try {
            //if its on the file system remove it.
            if (tableFile.exists()) {
                tableFile.delete();
            }

            //for every page in the buffer that has this table number, remove it.
            for (Page page : this.buffer) {
                if(tableNumber == page.getTableNumber()){
                    buffer.remove(page);
                }
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * Method meant to alter table attributes universally
     * @param tableNumber - the number of the table we are altering
     * @param op - the operation we are performing on the table, add or drop
     * @param attrName - attrName name of the attr we are altering
     * @param val - the default value if appliacable, otherwise null.
     * @return - null
     */
    public Exception alterTable(int tableNumber, String op, String attrName, Object val) {
        try {

            Catalog catalog = Catalog.getCatalog();
            TableSchema currentSchemea = catalog.getSchema(tableNumber);

            //loops through all of the pages the table has.
            for(int i = 1; i<=currentSchemea.getNumPages(); i++) {
                    Page currentPage = getPage(tableNumber, i);
                    Page newPage = new Page(currentPage.getNumRecords(), tableNumber, i);
                    List<Record> currentPageRecords = currentPage.getRecords();
                    List<Record> newPageRecords = new ArrayList<>();
                    List<Object> newVals;

                    //For each record in those tables
                    for (int j = 0; j < currentPageRecords.size(); j++) {
                        List<Object> oldVals = currentPageRecords.get(j).getValues();
                        newVals = oldVals;

                        //if we are adding, create a copy of the attr there and copy the values over to it.
                        // add the new attr to the end of the List<Object> (represnting data tuples)

                        if(op.equals("add")) {
                            newVals.add(new Object());

                            //if theres a default val, set it for each instances
                            if(val!=null){
                                newVals.set(-1,val);
                            }

                            //if we are dropping just remove the attr col
                        }else if(op.equals("drop")){
                            for(int k=0; k<currentSchemea.getAttributes().size(); k++ ){
                                if(currentSchemea.getAttributes().get(k).getAttributeName().equals(attrName)){
                                    newVals.remove(k);
                                }
                            }

                        }else{
                            throw new Exception("unknown op");
                        }
                        //put the records into  a page.
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
