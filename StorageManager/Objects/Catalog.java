package StorageManager.Objects;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

import StorageManager.TableSchema;

public class Catalog implements CatalogInterface {
    private static Catalog catalog;
    private Dictionary<Integer, TableSchema> schemas;
    private String dbLocation;
    private String catalogLocation;
    private int pageSize;
    private int bufferSize;


    private Catalog(String catalogLocation, String dbLocation, int pageSize, int bufferSize) {
        this.catalogLocation = catalogLocation;
        this.dbLocation = dbLocation;
        this.pageSize = pageSize != -1 ? pageSize : loadCatalog();
    }

    public static void createCatalog(String dbLocation, String catalogLocation, int pageSize, int bufferSize) {
        catalog = new Catalog(catalogLocation, dbLocation, pageSize, bufferSize);
    }


    public static Catalog getCatalog() {
        return catalog;
    }

    public void saveCatalog() {

    }

    public void addTableSchema(TableSchema schema) {

    }

    private int loadCatalog() {
        return 0;
    }

    public Dictionary<Integer, TableSchema> getSchemas() {
        return schemas;
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
