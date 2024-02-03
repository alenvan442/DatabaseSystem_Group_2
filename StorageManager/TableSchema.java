package StorageManager;

import java.util.List;

import StorageManager.Objects.Attribute;

public class TableSchema implements TableSchemaInterface {
    private int tableNumber;
    private String tableName;
    private List<Attribute> attributes;
    private int numPages;
    private int records;


    public TableSchema(String tableName) {
      this.tableName = tableName;
      this.tableNumber = this.hashName();
    }


    public int getTableNumber() {
      return tableNumber;
    }

    public String getTableName() {
      return tableName;
    }


    public void setTableName(String tableName) {
      this.tableName = tableName;
      this.tableNumber = this.hashName();
    }


    public List<Attribute> getAttributes() {
      return attributes;
    }


    public void setAttributes(List<Attribute> attributes) {
      this.attributes = attributes;
    }


    public int getNumPages() {
      return numPages;
    }


    public void setNumPages(int numPages) {
      this.numPages = numPages;
    }


    public int getRecords() {
      return records;
    }


    public void setRecords(int records) {
      this.records = records;
    }

    private int hashName() {
      char[] chars = this.tableName.toLowerCase().toCharArray();
      int hash = 0;
      int index = 0;
      for (char c : chars) {
          hash += Character.hashCode(c) + index;
          index++;
      }
      return hash;
  }

}
