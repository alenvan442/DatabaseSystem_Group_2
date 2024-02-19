package StorageManager.Objects;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import StorageManager.StorageManager;
import StorageManager.TableSchema;
import StorageManager.Objects.MessagePrinter.MessageType;

public class Catalog implements java.io.Serializable, CatalogInterface{
    private static Catalog catalog;
    private Map<Integer, TableSchema> schemas;
    private String dbLocation;
    private String catalogLocation;
    private int pageSize;
    private int bufferSize;


    private Catalog(String catalogLocation, String dbLocation, int pageSize, int bufferSize) throws Exception {
        this.catalogLocation = catalogLocation;
        this.dbLocation = dbLocation;
        this.bufferSize = bufferSize;
        this.schemas = new HashMap<>();
        if (pageSize == -1) {
            loadCatalog();
        } else {
            this.pageSize = pageSize;
        }
    }

    public static void createCatalog(String dbLocation, String catalogLocation, int pageSize, int bufferSize) throws Exception {
        catalog = new Catalog(catalogLocation, dbLocation, pageSize, bufferSize);
    }


    public static Catalog getCatalog() {
        return catalog;
    }

    /**
     * {@inheritDoc}
     * @throws Exception
    */
    public void saveCatalog() throws Exception {
        // Save catalog to hardware and obtain a random access file
        File schemaFile = new File(this.catalogLocation + "/schema");
        RandomAccessFile catalogAccessFile = new RandomAccessFile(schemaFile, "rw");

        // Write page size to the catalog file
        catalogAccessFile.writeInt(this.pageSize);

        // Write the number of schemas to the catalog file
        catalogAccessFile.writeInt(this.schemas.size());

        // Iterate over each table schema and save it to the catalog file
        for (int tableNum : this.schemas.keySet()) {
            this.schemas.get(tableNum).saveSchema(catalogAccessFile);
        }

        catalogAccessFile.close();

    }

    /**
     * {@inheritDoc}
     * @throws Exception
    */
    @Override
    public void loadCatalog() throws Exception {
        // Load catalog from hardware and obtain a random access file
        File schemaFile = new File(this.catalogLocation + "/schema");
        RandomAccessFile catalogAccessFile = new RandomAccessFile(schemaFile, "r");

        // Read page size from the catalog file
        this.pageSize = catalogAccessFile.readInt();

        // Read the number of tables from the catalog file
        int numOfTables = catalogAccessFile.readInt();

        // Iterate over each table schema and load it from the catalog file
        for (int i = 0; i < numOfTables; ++i) {
            // Read table Name
            String tableName = catalogAccessFile.readUTF();

            // Read the table number
            int tableNumber = catalogAccessFile.readInt();

            // Create a new table schema instance and load it from the catalog file
            TableSchema tableSchema = new TableSchema(tableName, tableNumber);
            tableSchema.loadSchema(catalogAccessFile);

            // Add the loaded table schema to the schemas map
            this.schemas.put(tableNumber, tableSchema);
        }

        catalogAccessFile.close();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dropTableSchema(int tableNumber){
        schemas.remove(tableNumber);
        StorageManager.getStorageManager().dropTable(tableNumber);
        //call StorageManager
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int alterTableSchema(int tableNumber,String op, String attrName, String attrType, Object val ,boolean notNull,
                                boolean pKey, boolean unique) throws Exception {
        TableSchema table = schemas.get(tableNumber);
        int returnIndex = -1;

        boolean has = false;
        List<AttributeSchema> attrList = table.getAttributes();
        if(op.equals("drop")){
            for(int i=0; i<attrList.size(); i++) {
                if(attrList.get(i).getAttributeName().equals(attrName)){
                    attrList.remove(i);
                    returnIndex = i;
                }
            }
        } else if (op.equals("add")) {
            for(int i=0; i<attrList.size(); i++){
                AttributeSchema currentAttr = attrList.get(i);
                if(currentAttr.getAttributeName().equals(attrName)){
                    has = true;
                }
            }
            if(!has) {
                attrList.add(new AttributeSchema(attrName, attrType, notNull, pKey, unique));
                returnIndex = attrList.size()-1;
            }
        }else{
            throw new Exception("Invalid Command");
        }
        table.setAttributes(attrList);

        if(returnIndex== -1){
            throw new Exception("The schema alter add or drop was not accounted for correctly.");
        }
        //call new storage manager method.
        StorageManager.getStorageManager().alterTable(tableNumber, op, attrName, val);
        return returnIndex;
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public void createTable(TableSchema tableSchema) throws Exception {
        for (TableSchema schema : this.schemas.values()) {
            if (tableSchema.getTableName().equals(schema.getTableName())) {
                MessagePrinter.printMessage(MessageType.ERROR, "Table of name" +  schema.getTableName() + "already exists");
            }
        }
        this.schemas.put(tableSchema.getTableNumber(), tableSchema);
    }

    public Map<Integer, TableSchema> getSchemas() {
        return schemas;
    }

    public TableSchema getSchema(int tableNumber) {
        return schemas.get(tableNumber);
    }

    public TableSchema getSchema(String tableName) throws Exception {
        for (Integer tableNuber: this.schemas.keySet()) {
            TableSchema tableSchema = getSchema(tableNuber);
            if (tableSchema.getTableName() == tableName) {
                return tableSchema;
            }
        }
        MessagePrinter.printMessage(MessageType.ERROR, String.format("table: %s does not exist", tableName));
        return null;
    }

    public void setSchemas(List<TableSchema> schemas) {
        Map<Integer, TableSchema> _new = new Hashtable<Integer,TableSchema>();
        for (TableSchema tableSchema : schemas) {
            _new.put(tableSchema.getTableNumber(), tableSchema);
        }
        this.schemas = _new;
    }

    public void addSchemas(TableSchema tableSchema) {
        this.schemas.put(tableSchema.getTableNumber(), tableSchema);
    }

    public String getDbLocation() {
        return dbLocation;
    }

    public String getCatalogLocation() {
        return catalogLocation;
    }

    public int getPageSize() {
        return pageSize;
    }

    public int getBufferSize() {
        return bufferSize;
    }

}
