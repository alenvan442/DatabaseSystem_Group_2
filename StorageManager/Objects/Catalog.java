package StorageManager.Objects;

import java.util.List;

import StorageManager.TableSchema;

public class Catalog {
    private List<TableSchema> schemas;
    private String dbLocation;
    private String catalogLocation;
    private int pageSize;
    private int bufferSize;

    public Catalog(String catalogLocation, String dbLocation, int bufferSize) {
        this.catalogLocation = catalogLocation;
        this.dbLocation = dbLocation;
        this.bufferSize = bufferSize;

    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public void saveCatalog() {

    }

    public void addTableSchema(TableSchema schema) {

    }

    protected void loadCatalog() {

    }

}
