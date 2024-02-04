package StorageManager;

import java.util.List;

import StorageManager.Objects.Attribute;

public interface TableSchemaInterface {
  public int getTableNumber();

  public String getTableName();

  public void setTableName(String tableName);

  public List<Attribute> getAttributes();

  public void setAttributes(List<Attribute> attributes);

  public int getNumPages();

  public void setNumPages(int numPages);

  public int getRecords();

  public void setRecords(int records);
}
