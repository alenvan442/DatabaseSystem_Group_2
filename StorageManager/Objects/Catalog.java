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
        // Retrieve the storage manager instance
        StorageManager storageManager = StorageManager.getStorageManager();

        // Create a file for the schema
        File schemaFile = new File(this.catalogLocation + "/schema");
        RandomAccessFile catalogAccessFile;

        try {
            // Save catalog to the storage and obtain a random access file
            catalogAccessFile = storageManager.saveCatalog(schemaFile);
            catalogAccessFile.seek(0);

            // Write page size to the catalog file
            catalogAccessFile.writeInt(this.pageSize);

            // Write the number of schemas to the catalog file
            catalogAccessFile.writeInt(this.schemas.size());

            // Iterate over each table schema and save it to the catalog file
            Enumeration<Integer> tableNums = schemas.keys();
            while (tableNums.hasMoreElements()) {
                int tableNum = tableNums.nextElement();
                this.schemas.get(tableNum).saveTableSchema(catalogAccessFile);
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
    private void loadCatalog() {
        // Retrieve the storage manager instance
        StorageManager storageManager = StorageManager.getStorageManager();

        // Create a file for the schema
        File schemaFile = new File(this.catalogLocation + "/schema");
        RandomAccessFile catalogAccessFile;

        try {
            // Load catalog from the storage and obtain a random access file
            catalogAccessFile = storageManager.loadCatalog(schemaFile);

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
                tableSchema.loadTableSchema(catalogAccessFile);

                // Add the loaded table schema to the schemas map
                this.schemas.put(tableNumber, tableSchema);
            }
        } catch (IOException e) {
            // Print the stack trace in case of an exception
            e.printStackTrace();
        }
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
