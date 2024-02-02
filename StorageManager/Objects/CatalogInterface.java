package StorageManager.Objects;

import java.util.Dictionary;
import java.util.List;

import StorageManager.TableSchema;

public interface CatalogInterface {

  public void saveCatalog();

  public void addTableSchema(TableSchema schema);

  /*
   * Get all table schemas in the catalog
   * Key:   tableNumber
   * Value: tableSchema
   * 
   * @return  a dictionary consisting all of the table schemas
   */
  public Dictionary<Integer, TableSchema> getSchemas();

  /*
   * Overloaded method that takes a list of tableSchemas
   * and converts them into a dictionary of <Integer, TableSchema>
   * where the key is the tableNumber.
   * 
   * @param schemas   list of table schemas
   */
  public void setSchemas(List<TableSchema> schemas);

  /*
   * Overloaded method that takes a dictionary of tableSchemas
   * and sets the local catalog to it.
   * 
   * @param schemas   dictionary of table schemas
   */
  public void setSchemas(Dictionary<Integer, TableSchema> schemas);

  public String getDbLocation();

  public String getCatalogLocation();

  public int getPageSize();

  public int getBufferSize();

}
