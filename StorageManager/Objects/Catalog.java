package StorageManager.Objects;

import java.util.List;

import StorageManager.TableSchema;

public class Catalog implements CatalogInterface {
    private static Catalog catalog;
    private List<TableSchema> schemas;
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

    public List<TableSchema> getSchemas() {
        return schemas;
    }

    public void setSchemas(List<TableSchema> schemas) {
        this.schemas = schemas;
    }

    public String getDbLocation() {
        return dbLocation;
    }

    public void setDbLocation(String dbLocation) {
        this.dbLocation = dbLocation;
    }

    public String getCatalogLocation() {
        return catalogLocation;
    }

    public void setCatalogLocation(String catalogLocation) {
        this.catalogLocation = catalogLocation;
    }

    public int getPageSize() {
        return pageSize;
    }

    public int getBufferSize() {
        return bufferSize;
    }

    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

}
