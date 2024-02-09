package StorageManager.Objects;

public interface CatalogInterface {

  public void createTable(String attributeName, String attributeType, boolean isNotNull, boolean isUnique, boolean isPrimaryKey);
  public void dropTableSchema(int tableNumber);

  public int alterTableSchema(int tableNumber,String op, String attrName, String attrType, boolean notNull,
                                boolean pKey, boolean unique) throws Exception;
                                
  public void loadCatalog();
  public void saveCatalog();
}
