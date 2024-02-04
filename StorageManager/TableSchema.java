package StorageManager;

import java.util.ArrayList;
import java.util.List;

import StorageManager.Objects.Attribute;

public class TableSchema implements TableSchemaInterface {
    private int tableNumber;
    private String tableName;
    private List<Attribute> attributes;
    private int numPages;
    private int numRecords;


    public TableSchema(String tableName, ArrayList<Attribute> attributes) {
      this.tableName = tableName;
      this.tableNumber = this.hashName();
      this.numPages = 0;
      this.numRecords = 0;
      this.attributes = attributes;
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
      return numRecords;
    }


    public void setRecords(int numRecords) {
      this.numRecords = numRecords;
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
