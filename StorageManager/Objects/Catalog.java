package StorageManager.Objects;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

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
        this.pageSize = pageSize != -1 ? pageSize : 0;
        loadCatalog();
    }

    public static void createCatalog(String dbLocation, String catalogLocation, int pageSize, int bufferSize) {
        catalog = new Catalog(catalogLocation, dbLocation, pageSize, bufferSize);
    }


    public static Catalog getCatalog() {
        return catalog;
    }

    public void saveCatalog() throws IOException {
        ByteArrayOutputStream catalogByteArray = new ByteArrayOutputStream();
        ByteBuffer integerBuffer = ByteBuffer.allocate(Integer.BYTES * 3);
        integerBuffer.putInt(pageSize);
        integerBuffer.putInt(bufferSize);
        integerBuffer.putInt(schemas.size());
        catalogByteArray.write(integerBuffer.array());
        while (schemas.keys().hasMoreElements()) {
            Integer tableNum = schemas.keys().nextElement();
            catalogByteArray.write(schemas.get(tableNum).convertToBytes());
        }
        File schema = new File(this.catalogLocation + "/schema");
        FileOutputStream fileOutputStream = new FileOutputStream(schema.getAbsolutePath(), false);
        catalogByteArray.writeTo(fileOutputStream);
    }

    public void addTableSchema(TableSchema schema) {
        schemas.put(schema.getTableNumber(), schema);
    }

    private int loadCatalog() {
        return 0;
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
