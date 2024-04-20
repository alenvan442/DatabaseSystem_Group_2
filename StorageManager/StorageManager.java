package StorageManager;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;

import Parser.Insert;
import Parser.Type;
import QueryExecutor.InsertQueryExcutor;
import StorageManager.BPlusTree.BPlusNode;
import StorageManager.BPlusTree.Bucket;
import StorageManager.BPlusTree.InternalNode;
import StorageManager.BPlusTree.LeafNode;
import StorageManager.Objects.AttributeSchema;
import StorageManager.Objects.BufferPage;
import StorageManager.Objects.Catalog;
import StorageManager.Objects.MessagePrinter;
import StorageManager.Objects.Page;
import StorageManager.Objects.Record;
import StorageManager.Objects.MessagePrinter.MessageType;
import StorageManager.Objects.Utility.Pair;

public class StorageManager implements StorageManagerInterface {
    private static StorageManager storageManager;
    private PriorityQueue<BufferPage> buffer;
    private int bufferSize;

    /**
     * Constructor for the storage manager
     * initializes the class by initializing the buffer
     *
     * @param buffersize The size of the buffer
     */
    private StorageManager(int bufferSize) {
        this.bufferSize = bufferSize;
        this.buffer = new PriorityQueue<>(bufferSize, new Page());
    }

    /**
     * Static function that initializes the storageManager
     *
     * @param bufferSize The size of the buffer
     */
    public static void createStorageManager(int bufferSize) {
        storageManager = new StorageManager(bufferSize);
    }

    /**
     * Getter for the global storageManager
     *
     * @return The storageManager
     */
    public static StorageManager getStorageManager() {
        return storageManager;
    }

    /**
     * Splits a page by moving half of its records to a new page.
     *
     * @param page        The page to split.
     * @param record      The record to insert after the split.
     * @param tableSchema The schema of the table.
     * @throws Exception If an error occurs during the split operation.
     */
    private void pageSplit(Page page, Record record, TableSchema tableSchema, int primaryKeyIndex) throws Exception {
        // Create a new page
        Page newPage = new Page(0, tableSchema.getTableNumber(), tableSchema.getNumPages() + 1);
        tableSchema.addPageNumber(page.getPageNumber(), newPage.getPageNumber());

        // Calculate the split index
        int splitIndex = 0;
        if (page.getRecords().size() == 1) {
            Record lastRecordInCurrPage = page.getRecords().get(page.getRecords().size() - 1);
            if (record.compareTo(lastRecordInCurrPage, primaryKeyIndex) < 0) {
                page.getRecords().clear();
                page.addNewRecord(record);
                newPage.addNewRecord(lastRecordInCurrPage);
            } else {
                newPage.addNewRecord(record);
            }
        } else {
            splitIndex = (int) Math.floor(page.getRecords().size() / 2);

            // Move half of the records to the new page
            for (Record copyRecord : page.getRecords().subList(splitIndex, page.getRecords().size())) {
                if (!newPage.addNewRecord(copyRecord)) {
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
        }

        page.setNumRecords();
        newPage.setNumRecords();
        page.setChanged();

        // Add the new page to the buffer
        this.addPageToBuffer(newPage);
    }

    /**
     * Construct the full table path according to where
     * the DB is located
     *
     * @param tableNumber the id of the table
     *
     * @return the full table path
     */
    private String getTablePath(int tableNumber) {
        String dbLoc = Catalog.getCatalog().getDbLocation();
        return dbLoc + "/tables/" + Integer.toString(tableNumber);
    }

    /**
     * Construct the full indexing file path according to where
     * the DB is located
     *
     * @param tableNumber the id of the table
     *
     * @return the full indexing path
     */
    private String getIndexingPath(int tableNumber) {
        String dbLoc = Catalog.getCatalog().getDbLocation();
        return dbLoc + "/indexing/" + Integer.toString(tableNumber);

    }

    public Record getRecord(int tableNumber, Object primaryKey) throws Exception {
        // used for selecting based on primary key
        Catalog catalog = Catalog.getCatalog();
        TableSchema schema = catalog.getSchema(tableNumber);
        int primaryKeyIndex = schema.getPrimaryIndex();
        List<Integer> pageOrder = schema.getPageOrder();
        Page foundPage = null;

        for (int pageNumber : pageOrder) {
            Page page = this.getPage(tableNumber, pageNumber);
            if (page.getNumRecords() == 0) {
                return null;
            }

            Record lastRecord = page.getRecords().get(page.getRecords().size() - 1);
            int comparison = lastRecord.compareTo(primaryKey, primaryKeyIndex);

            if (comparison == 0) {
                // found the record, return it
                return lastRecord;
            } else if (comparison > 0) {
                // found the correct page
                foundPage = page;
                break;
            } else {
                // record was not found, continue
                continue;
            }
        }

        if (foundPage == null) {
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

    public Record getRecord(String tableName, Object primaryKey) throws Exception {
        int tableNumber = TableSchema.hashName(tableName);
        return this.getRecord(tableNumber, primaryKey);
    }

    public List<Record> getAllRecords(int tableNumber) throws Exception {
        List<Record> records = new ArrayList<>(); // List to store all records
        List<Page> allPagesForTable = new ArrayList<>();
        Catalog catalog = Catalog.getCatalog();
        TableSchema tableSchema = catalog.getSchema(tableNumber);
        for (Integer pageNumber : tableSchema.getPageOrder()) {
            allPagesForTable.add(this.getPage(tableNumber, pageNumber));
        }

        for (Page page : allPagesForTable) {
            records.addAll(page.getRecords());
        }

        return records;
    }

    public List<Record> getAllRecords(String tableName) throws Exception {
        int tableNum = TableSchema.hashName(tableName);
        return this.getAllRecords(tableNum);
    }

    public void insertRecord(int tableNumber, Record record) throws Exception {
        Catalog catalog = Catalog.getCatalog();
        // get tableSchema from the catalog
        TableSchema tableSchema = catalog.getSchema(tableNumber);
        if (record.computeSize() > (catalog.getPageSize() - (Integer.BYTES * 2))) {
            MessagePrinter.printMessage(MessageType.ERROR,
                    "Unable to insert record. The record size is larger than the page size.");
        }

        String tablePath = this.getTablePath(tableNumber);
        File tableFile = new File(tablePath);
        String indexPath = this.getIndexingPath(tableNumber);
        File indexFile = new File(indexPath);

        // determine index of the primary key
        int primaryKeyIndex = tableSchema.getPrimaryIndex();
        int n = 0;

        if (catalog.isIndexingOn()) {
            n = tableSchema.computeN(catalog);
        }

        // check to see if the file exists, if not create it
        if (!tableFile.exists()) {
            tableFile.createNewFile();
            // create a new page and insert the new record into it
            Page _new = new Page(0, tableNumber, 1);
            tableSchema.addPageNumber(_new.getPageNumber());
            _new.addNewRecord(record);
            tableSchema.incrementNumRecords();
            // then add the page to the buffer
            this.addPageToBuffer(_new);

            if (catalog.isIndexingOn()) {
                // check if an index file already exists, if not create it, it should never exist at this point
                // if it does error out saying the database is corrupted
                if (!indexFile.exists()) {
                    indexFile.createNewFile();
                    // create a new leaf node and insert the new pk into it, this will be the first root node
                    LeafNode root = new LeafNode(tableNumber, 1, n, -1);
                    tableSchema.incrementNumIndexPages();
                    tableSchema.setRoot(1);
                    Bucket bucket = new Bucket(1, 0, record.getValues().get(primaryKeyIndex));
                    root.addBucket(bucket);

                    // then add the page to the buffer
                    this.addPageToBuffer(root);
                } else {
                    MessagePrinter.printMessage(MessageType.ERROR, "Database is corrupted. Index file found before table file was created.");
                }

                return;
            }
        } else {

            if (tableSchema.getNumPages() == 0) {
                Page _new = new Page(0, tableNumber, 1);
                tableSchema.addPageNumber(_new.getPageNumber());
                _new.addNewRecord(record);
                tableSchema.incrementNumRecords();
                // then add the page to the buffer
                this.addPageToBuffer(_new);
                if (catalog.isIndexingOn() && tableSchema.getNumIndexPages() == 0) {
                    // create a new leaf node and insert the new record into it, this will be the first root node
                    LeafNode root = new LeafNode(tableNumber, 1, n, -1);
                    tableSchema.incrementNumIndexPages();
                    tableSchema.setRoot(1);

                    // then add the page to the buffer
                    this.addPageToBuffer(root);
                }
            } else {

                if (catalog.isIndexingOn()) {
                    // set up while loop with the root as the first to search
                    Object pk = record.getValues().get(primaryKeyIndex);
                    Type pkType = tableSchema.getAttributeType(primaryKeyIndex);

                    Pair<Integer, Integer> location = new Pair<Integer,Integer>(tableSchema.getRootNumber(), -1);
                    BPlusNode node = null;
                    
                    while (location.second == -1) {
                        // read in node
                        node = this.getIndexPage(tableNumber, location.first);
                        location = node.insert(pk, pkType);
                    }

                    while (node.overfull()) {
                        // get array in node
                        InternalNode parent = null;
                        if (node.getParent() == -1) {
                            // this is the root node that is overfull
                            // create new parent node which will be the new root
                            parent = new InternalNode(tableNumber, tableSchema.incrementNumIndexPages(), n, -1);
                            node.setParent(parent.getPageNumber());
                            tableSchema.setRoot(parent.getPageNumber());
                        } else {
                            parent = (InternalNode)this.getIndexPage(tableNumber, node.getParent()); // only internals can be a parent
                        }

                        if (node instanceof InternalNode) {
                            InternalNode internal = (InternalNode)node;
                            ArrayList<Object> searchKeys = internal.getSK(); 
                            ArrayList<Pair<Integer, Integer>> pointers = internal.getPointers();

                            // split search keys into two
                            int skNum = searchKeys.size();
                            List<Object> firstSK = searchKeys.subList(0, skNum/2);
                            Object goingUp = searchKeys.get(skNum/2);
                            List<Object> secondSK = searchKeys.subList(skNum/2+1, skNum);

                            // split pointers into two
                            int pointNum = pointers.size();
                            int splitIndex = Math.ceilDiv(pointNum, 2);
                            List<Pair<Integer, Integer>> firstPointers = pointers.subList(0, splitIndex);
                            // no need for a going up for the pointers
                            List<Pair<Integer, Integer>> secondPointers = pointers.subList(splitIndex, pointNum);
                            
                            InternalNode newNode = new InternalNode(tableNumber, tableSchema.incrementNumIndexPages(), n, parent.getPageNumber());
                            
                            // set the searck keys and pointers for the two child nodes
                            internal.setSK(firstSK);
                            internal.setPointers(firstPointers);
                            newNode.setSK(secondSK);
                            newNode.setPointers(secondPointers);
            
                            // append new search key to parent
                            parent.addSearchKey(goingUp, new Pair<Integer,Integer>(newNode.getPageNumber(), -1)); 
                            
                            this.addPageToBuffer(newNode);
                        } else if (node instanceof LeafNode) {
                            LeafNode leaf = (LeafNode)node;
                            ArrayList<Bucket> sks = leaf.getSK();
                            
                            // split into two
                            List<Bucket> first = sks.subList(0, sks.size()/2);
                            List<Bucket> second = sks.subList(sks.size()/2, sks.size());

                            // get next search key
                            Object goingUp = second.get(0).getPrimaryKey();

                            // create new node
                            LeafNode newNode = new LeafNode(tableNumber, tableSchema.incrementNumIndexPages(), n, parent.getPageNumber());
                            leaf.setSK(first);
                            newNode.setSK(second);
                            newNode.assignNextLeaf(leaf.getNextLeaf().first);
                            leaf.assignNextLeaf(newNode.getPageNumber());

                            // append new search key to parent
                            parent.addSearchKey(goingUp, new Pair<Integer,Integer>(newNode.getPageNumber(), -1)); 

                            this.addPageToBuffer(newNode);
                        }
                        // get new parent and node
                        // repeat
                        node = parent;
                    }

                    // get page and insert
                    Page page = this.getPage(tableNumber, location.first);
                    if (!page.addNewRecord(record, location.second)) {
                        // page was full
                        this.pageSplit(page, record, tableSchema, primaryKeyIndex);
                    }
                    tableSchema.incrementNumRecords();
                } else {

                    for (Integer pageNumber : tableSchema.getPageOrder()) {
                        Page page = this.getPage(tableNumber, pageNumber);
                        if (page.getNumRecords() == 0) {
                            if (!page.addNewRecord(record)) {
                                // page was full
                                this.pageSplit(page, record, tableSchema, primaryKeyIndex);
                            }
                            tableSchema.incrementNumRecords();
                            break;
                        }

                        Record lastRecordInPage = page.getRecords().get(page.getRecords().size() - 1);
                        if ((record.compareTo(lastRecordInPage, primaryKeyIndex) < 0) ||
                            (pageNumber == tableSchema.getPageOrder().get(tableSchema.getPageOrder().size() - 1))) {
                            // record is less than lastRecordPage
                            if (!page.addNewRecord(record)) {
                                // page was full
                                this.pageSplit(page, record, tableSchema, primaryKeyIndex);
                            }
                            tableSchema.incrementNumRecords();
                            break;
                        }
                    }
                }
            }
        }
    }

    private Pair<Page, Record> deleteHelper(TableSchema schema, Object primaryKey) throws Exception {

        Integer tableNumber = schema.getTableNumber();
        int primaryIndex = schema.getPrimaryIndex();
        Page foundPage = null;

        // start reading pages
        // get page order
        List<Integer> pageOrder = schema.getPageOrder();

        // find the correct page
        for (int pageNumber : pageOrder) {
            Page page = this.getPage(tableNumber, pageNumber);

            // compare last record in page
            List<Record> foundRecords = page.getRecords();
            Record lastRecord = foundRecords.get(page.getNumRecords() - 1);
            int comparison = lastRecord.compareTo(primaryKey, primaryIndex);
            if (comparison == 0) {
                // found the record, delete it
                Record removed = page.deleteRecord(page.getNumRecords() - 1);
                return new Pair<Page,Record>(page, removed);
            } else if (comparison > 0) {
                // found the correct page
                foundPage = page;
                break;
            } else {
                // page was not found, continue
                continue;
            }
        }

        if (foundPage == null) {
            MessagePrinter.printMessage(MessageType.ERROR,
                    String.format("No record of primary key: (%d), was found.",
                            primaryKey));
        } else {
            // a page was found but deletion has yet to happen
            List<Record> recordsInFound = foundPage.getRecords();
            for (int i = 0; i < recordsInFound.size(); i++) {
                if (recordsInFound.get(i).compareTo(primaryKey, primaryIndex) == 0) {
                    Record removed = foundPage.deleteRecord(i);
                    return new Pair<Page, Record>(foundPage, removed);

                }
            }
            MessagePrinter.printMessage(MessageType.ERROR,
                    String.format("No record of primary key: (%d), was found.",
                            primaryKey));
        }
        return null;
    }

    private void checkDeletePage(TableSchema schema, Page page) throws Exception {
        if (page.getNumRecords() == 0) {
            // begin to delete the page by moving all preceding pages up
            List<Integer> pageOrder = schema.getPageOrder();
            for (int i = 0; i < schema.getPageOrder().size(); i++) {
                Page foundPage = this.getPage(schema.getTableNumber(), pageOrder.get(i));
                if (foundPage.getPageNumber() > page.getPageNumber()) {
                    foundPage.decrementPageNumber();
                    schema.setNumPages();
                }
            }

            // update the pageOrder of the schema
            schema.deletePageNumber(page.getPageNumber());
        }
    }

    public Record deleteRecord(int tableNumber, Object primaryKey) throws Exception {

        TableSchema schema = Catalog.getCatalog().getSchema(tableNumber);
        Catalog catalog = Catalog.getCatalog();
        Record deletedRecord = null;
        Page deletePage = null;
        Pair<Page, Record> deletedPair = null;

        if (catalog.isIndexingOn()) {
            deletedPair = deleteIndex(tableNumber, primaryKey);
        } else {
            deletedPair = this.deleteHelper(schema, primaryKey);
        }

        deletePage = deletedPair.first;
        deletedRecord = deletedPair.second;

        schema.decrementNumRecords();
        this.checkDeletePage(schema, deletePage);
        return deletedRecord;
    }

    private Pair<Page, Record> deleteIndex(int tableNumber, Object primaryKey) throws Exception {
        TableSchema schema = Catalog.getCatalog().getSchema(tableNumber);
        Page deletePage = null;
        
        // get pk data type
        Type pkType = schema.getAttributeType(schema.getPrimaryIndex());

        // find location
        Pair<Integer, Integer> location = new Pair<Integer,Integer>(schema.getRootNumber(), -1);
        while (location.second == -1) {
            // read in node
            BPlusNode node = this.getIndexPage(tableNumber, location.first);
            location = node.insert(primaryKey, pkType);
        }

        // get page and delete
        deletePage = this.getPage(tableNumber, location.first);
        return new Pair<Page, Record>(deletePage, deletePage.deleteRecord(location.second));

        // TODO merge/borrowing
        
    }

    public void updateRecord(int tableNumber, Record newRecord, Object primaryKey) throws Exception {

        Record oldRecord = deleteRecord(tableNumber, primaryKey); // if the delete was successful then deletePage != null

        Insert insert = new Insert(Catalog.getCatalog().getSchema(tableNumber).getTableName(), null);
        InsertQueryExcutor insertQueryExcutor = new InsertQueryExcutor(insert);

        try {
            insertQueryExcutor.validateRecord(newRecord);
            this.insertRecord(tableNumber, newRecord);
        } catch (Exception e) {
            // insert failed, restore the deleted record
            this.insertRecord(tableNumber, oldRecord);
            System.err.println(e.getMessage());
            throw new Exception();
        }
    }


    /**
     * Method to drop whole tables from the DB
     *
     * @param tableNumber - the tablenumber for the table we are removing
     */
    public void dropTable(int tableNumber) {

        // Checks the hardware for a tablefile. If it finds it remove it.
        String tablePath = this.getTablePath(tableNumber);
        File tableFile = new File(tablePath);
        String indexPath = this.getIndexingPath(tableNumber);
        File indexFile = new File(indexPath);
        try {
            // if its on the file system remove it.
            if (tableFile.exists()) {
                tableFile.delete();
            }

            // if BPlus exists, drop it
            if (indexFile.exists()) {
                tableFile.delete();
            }

            // for every page in the buffer that has this table number, remove it.
            List<BufferPage> toRemove = new ArrayList<>();
            for (BufferPage page : this.buffer) {
                if (tableNumber == page.getTableNumber()) {
                    toRemove.add(page);
                }
            }

            for (BufferPage page : toRemove) {
                this.buffer.remove(page);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * Method meant to alter table attributes universally
     *
     * @param tableNumber - the number of the table we are altering
     * @param op          - the operation we are performing on the table, add or
     *                    drop
     * @param attrName    - attrName name of the attr we are altering
     * @param val         - the default value if appliacable, otherwise null.
     * @return - null
     * @throws Exception
     */
    public Exception alterTable(int tableNumber, String op, String attrName, Object val, String isDeflt,
            List<AttributeSchema> attrList) throws Exception {
        Catalog catalog = Catalog.getCatalog();
        TableSchema currentSchemea = catalog.getSchema(tableNumber);
        TableSchema newSchema = new TableSchema(currentSchemea.getTableName());
        newSchema.setAttributes(attrList);

        // get all rows in old table
        List<Record> oldRecords = this.getAllRecords(tableNumber);
        List<Record> newRecords = new ArrayList<>();

        // determine value to add in the new column and add it
        Object newVal = isDeflt.equals("true") ? val : null;

        for (Record record : oldRecords) {
            if (op.equals("add")) {
                // if add col, add the new value to the record
                record.addValue(newVal);
                if (record.computeSize() > (catalog.getPageSize() - (Integer.BYTES * 2))) {
                    MessagePrinter.printMessage(MessageType.ERROR,
                            "Alter will cause a record to be greater than the page size. Aborting alter...");
                }
            } else if (op.equals("drop")) {
                // if drop col, remove the col to be removed
                List<Object> oldVals = record.getValues();
                for (int k = 0; k < currentSchemea.getAttributes().size(); k++) {
                    if (currentSchemea.getAttributes().get(k).getAttributeName().equals(attrName)) {
                        oldVals.remove(k);
                        break;
                    }
                }
                record.setValues(oldVals);
            } else {
                throw new Exception("unknown op");
            }
            newRecords.add(record);
        }

        // drop old table and create new one
        catalog.dropTableSchema(tableNumber);
        catalog.createTable(newSchema);

        for (Record record : newRecords) {
            this.insertRecord(tableNumber, record);
        }

        return null;
    }

    // ---------------------------- Page Buffer ------------------------------

    private BufferPage getLastPageInBuffer(PriorityQueue<BufferPage> buffer) {
        Object[] bufferArray = buffer.toArray();
        return ((BufferPage) bufferArray[bufferArray.length - 1]);
    }

    @Override
    public Page getPage(int tableNumber, int pageNumber) throws Exception {
        // check if page is in buffer
        for (int i = this.buffer.size()-1; i >= 0; i--) {
            Object[] bufferArray = this.buffer.toArray();
            BufferPage page = (BufferPage) bufferArray[i];
            if (page instanceof Page && page.getTableNumber() == tableNumber && page.getPageNumber() == pageNumber) {
                page.setPriority();
                return (Page) page;
            }
        }

        // read page from hardware into buffer
        readPageHardware(tableNumber, pageNumber);
        return (Page) getLastPageInBuffer(this.buffer);
    }

    @Override
    public BPlusNode getIndexPage(int tableNumber, int pageNumber) throws Exception {
        // check if page is in buffer
        for (int i = this.buffer.size()-1; i >= 0; i--) {
            Object[] bufferArray = this.buffer.toArray();
            BufferPage page = (BufferPage) bufferArray[i];
            if (page instanceof BPlusNode && page.getTableNumber() == tableNumber && page.getPageNumber() == pageNumber) {
                page.setPriority();
                return (BPlusNode) page;
            }
        }

        // read page from hardware into buffer
        readIndexPageHardware(tableNumber, pageNumber);
        return (BPlusNode) getLastPageInBuffer(this.buffer);
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

    private void readIndexPageHardware(int tableNumber, int pageNumber) throws Exception {
        // TODO
    }

    private void writePageHardware(BufferPage page) throws Exception {
        // TODO check between indexPage and regular page
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
        tableAccessFile.seek(tableAccessFile.getFilePointer() - catalog.getPageSize()); // move pointer back

        page.writeToHardware(tableAccessFile);
        tableAccessFile.close();
    }

    private void addPageToBuffer(BufferPage page) throws Exception {
        if (this.buffer.size() == this.bufferSize) {
            BufferPage lruPage = this.buffer.poll(); // assuming the first Page in the buffer is LRU
            if (lruPage.isChanged()) {
                this.writePageHardware(lruPage);
            }
        }
        this.buffer.add(page);
    }

    public void writeAll() throws Exception {
        for (BufferPage page : buffer) {
            if (page.isChanged()) {
                writePageHardware(page);
            }
        }
        this.buffer.removeAll(buffer);
    }

}
