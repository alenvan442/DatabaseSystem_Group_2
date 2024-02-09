package StorageManager.Objects;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import StorageManager.StorageManager;
import StorageManager.TableSchema;

public class Catalog implements java.io.Serializable, CatalogInterface{
    private static Catalog catalog;
    private Dictionary<Integer, TableSchema> schemas;
    private String dbLocation;
    private String catalogLocation;
    private int pageSize;
    private int bufferSize;


    private Catalog(String catalogLocation, String dbLocation, int pageSize, int bufferSize) {
        this.catalogLocation = catalogLocation;
        this.dbLocation = dbLocation;
        this.bufferSize = bufferSize;
        this.schemas = new Hashtable<Integer,TableSchema>();
        if (pageSize == -1) {
            loadCatalog();
        } else {
            this.pageSize = pageSize;
        }
    }

    public static void createCatalog(String dbLocation, String catalogLocation, int pageSize, int bufferSize) {
        catalog = new Catalog(catalogLocation, dbLocation, pageSize, bufferSize);
    }


    public static Catalog getCatalog() {
        return catalog;
    }

    /**
     * Saves the catalog to the storage.
     * The catalog information is saved to a file specified by the catalog location,
     * along with the schema information for each table.
     *
     * @throws IOException if an I/O error occurs while saving the catalog
    */
    public void saveCatalog() {
        try {
            // Save catalog to hardware and obtain a random access file
            File schemaFile = new File(this.catalogLocation + "/schema");
            RandomAccessFile catalogAccessFile = new RandomAccessFile(schemaFile, "r");
            catalogAccessFile.seek(0);

            // Write page size to the catalog file
            catalogAccessFile.writeInt(this.pageSize);

            // Write the number of schemas to the catalog file
            catalogAccessFile.writeInt(this.schemas.size());

            // Iterate over each table schema and save it to the catalog file
            Enumeration<Integer> tableNums = schemas.keys();
            while (tableNums.hasMoreElements()) {
                int tableNum = tableNums.nextElement();
                this.schemas.get(tableNum).saveSchema(catalogAccessFile);
            }
        } catch (IOException e) {
            // Print the stack trace in case of an exception
            e.printStackTrace();
        }
    }


    public void addTableSchema(TableSchema schema) {
        schemas.put(schema.getTableNumber(), schema);
    }

    /**
     * Loads the catalog from the storage.
     * The catalog information is loaded from a file specified by the catalog location,
     * including the schema information for each table.
     *
     * @throws IOException if an I/O error occurs while loading the catalog
    */
    @Override
    public void loadCatalog() {
        try {
            // Load catalog from hardware and obtain a random access file
            File schemaFile = new File(this.catalogLocation + "/schema");
            RandomAccessFile catalogAccessFile = new RandomAccessFile(schemaFile, "rw");

            // Read page size from the catalog file
            this.pageSize = catalogAccessFile.readInt();

            // Read the number of tables from the catalog file
            int numOfTables = catalogAccessFile.readInt();

            // Iterate over each table schema and load it from the catalog file
            for (int i = 0; i < numOfTables; ++i) {
                // Read the length of the table name
                int tableNameLength = catalogAccessFile.readShort();

                // Read the bytes representing the table name
                byte[] tableNameBytes = new byte[tableNameLength];
                catalogAccessFile.read(tableNameBytes);

                // Convert the byte array to a String representing the table name
                String tableName = new String(tableNameBytes);

                // Read the table number
                int tableNumber = catalogAccessFile.readInt();

                // Create a new table schema instance and load it from the catalog file
                TableSchema tableSchema = new TableSchema(tableName, tableNumber);
                tableSchema.loadSchema(catalogAccessFile);

                // Add the loaded table schema to the schemas map
                this.schemas.put(tableNumber, tableSchema);
            }
        } catch (IOException e) {
            // Print the stack trace in case of an exception
            e.printStackTrace();
        }
    }

    //  * Method that deletes the schema for a table that is being dropped.
    //  * @param tableNumber - The old table # for the table we are updating due to being dropped.

    @Override
    public void dropTableSchema(int tableNumber){
        schemas.remove(tableNumber);
        StorageManager.getStorageManager().dropTable(tableNumber);
        //call StorageManager
    }

    /**
     * Method that updates the Schema for a particular Table based on the result of an Alter command.
     * @param tableNumber - The # for the Table we are updating the schemea of.
     * @param op - the alter operation we performed on this table, ei: drop attr or add attr
     * @param attrName - Name of the Attr that is being altered in the table.
     * @param attrType - Type of the Attr that is being altered in the table.
     * @param notNull - The attribute cannot be null.
     * @param pKey - Whether or not the attribute is a primary key.
     * @param unique - Whether or not the attribute has to be unique.
     * @throws Exception - Should only be thrown if the method is called in an incorrect fashion.
     * @return - If drop - The previous index of the dropped item. If add - the index of the new attr.
     */
    @Override
    public int alterTableSchema(int tableNumber,String op, String attrName, String attrType, boolean notNull,
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
        StorageManager.getStorageManager().alterTable(tableNumber, op, attrName, attrType, notNull, pKey, unique);
        return returnIndex;
    }

    @Override
    public void createTable(String attributeName, String attributeType, boolean isNotNull, boolean isUnique, boolean isPrimaryKey) {

    }

    public Dictionary<Integer, TableSchema> getSchemas() {
        return schemas;
    }

    public TableSchema getSchema(int tableNumber) {
        return schemas.get(tableNumber);
    }

    public void setSchemas(List<TableSchema> schemas) {
        Dictionary<Integer, TableSchema> _new = new Hashtable<Integer,TableSchema>();
        for (TableSchema tableSchema : schemas) {
            _new.put(tableSchema.getTableNumber(), tableSchema);
        }
        this.schemas = _new;
    }

    public void setSchemas(Dictionary<Integer, TableSchema> schemas) {
        this.schemas = schemas;
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
