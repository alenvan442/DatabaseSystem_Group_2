package StorageManager.Objects;

import java.util.List;

import StorageManager.TableSchema;

public interface CatalogInterface {

  public void saveCatalog();

  public void addTableSchema(TableSchema schema);

  public List<TableSchema> getSchemas();

  public void setSchemas(List<TableSchema> schemas);

  public String getDbLocation();

  public String getCatalogLocation();

  public int getPageSize();

  public int getBufferSize();

}
