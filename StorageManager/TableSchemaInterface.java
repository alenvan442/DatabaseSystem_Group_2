package StorageManager;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.List;

import StorageManager.Objects.AttributeSchema;

public interface TableSchemaInterface {
  public int getTableNumber();

  public String getTableName();

  public void setTableName(String tableName);

  public List<AttributeSchema> getAttributes();

  public void setAttributes(List<AttributeSchema> attributes);

  public void addAttribute(AttributeSchema attributeSchema);

  public int getNumPages();

  public void setNumPages(int numPages);

  public List<Integer> getPageOrder();

  public void setPageOrder(List<Integer> pageOrder);

  public int getRecords();

  public void setRecords(int records);

  public void saveTableSchema(RandomAccessFile catalogAccessFile) throws IOException;

  public void loadTableSchema(RandomAccessFile catalogAccessFile) throws IOException;
}
