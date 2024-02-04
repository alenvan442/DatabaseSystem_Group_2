package StorageManager;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import StorageManager.Objects.AttributeSchema;

public class TableSchema implements TableSchemaInterface {
    private int tableNumber;
    private String tableName;
    private List<AttributeSchema> attributes;
    private int numPages;
    private int numRecords;


    public TableSchema(String tableName, ArrayList<Attribute> attributes) {
      this.tableName = tableName;
      this.tableNumber = this.hashName();
      this.numPages = 0;
      this.numRecords = 0;
      this.attributes = attributes;
    }

    @Override
    public int getTableNumber() {
      return tableNumber;
    }

    @Override
    public String getTableName() {
      return tableName;
    }

    @Override
    public void setTableName(String tableName) {
      this.tableName = tableName;
      this.tableNumber = this.hashName();
    }

    @Override
    public List<AttributeSchema> getAttributes() {
      return attributes;
    }

    @Override
    public void setAttributes(List<AttributeSchema> attributes) {
      this.attributes = attributes;
    }

    @Override
    public int getNumPages() {
      return numPages;
    }

    @Override
    public void setNumPages(int numPages) {
      this.numPages = numPages;
    }

    @Override
    public int getRecords() {
      return numRecords;
    }

    @Override
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

    public byte[] convertToBytes() throws IOException {
      ByteArrayOutputStream tableSchemaByteArray = new ByteArrayOutputStream();
      tableSchemaByteArray.write(this.tableName.getBytes(StandardCharsets.UTF_8));
      ByteBuffer integerBufer = ByteBuffer.allocate(Integer.BYTES * 4);
      integerBufer.putInt(tableNumber);
      integerBufer.putInt(numPages);
      integerBufer.putInt(attributes.size());
      for (AttributeSchema attribute: attributes) {
        tableSchemaByteArray.write(attribute.convertToBytes());
      }
      // loop through the attributes
      return tableSchemaByteArray.toByteArray();
    }

}
