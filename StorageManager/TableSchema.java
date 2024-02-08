package StorageManager;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
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
    private List<Integer> pageOrder;
    private int numRecords;


    public TableSchema(String tableName, int tableNumber) {
      this.tableName = tableName;
      this.tableNumber = tableNumber == -1 ? this.hashName(): tableNumber;
      this.numPages = 0;
      this.pageOrder = new ArrayList<Integer>();
      this.numRecords = 0;
      this.attributes = new ArrayList<AttributeSchema>();
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
    public List<Integer> getPageOrder() {
      return this.pageOrder;
    }

    @Override
    public void setPageOrder(List<Integer> pageOrder) {
      this.pageOrder = pageOrder;
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

    /**
   * Saves the table schema information to the specified random access file.
   *
   * @param catalogAccessFile the random access file where the table schema information will be saved
   * @throws IOException if an I/O error occurs while writing to the random access file
   */
  @Override
  public void saveTableSchema(RandomAccessFile catalogAccessFile) throws IOException {
    // Write table name to the catalog file as UTF string
    catalogAccessFile.writeUTF(this.tableName);

    // Write table number to the catalog file
    catalogAccessFile.writeInt(this.tableNumber);

    // Write the number of pages to the catalog file
    catalogAccessFile.writeInt(this.numPages);

    // Write page order to the catalog file
    for (int i = 0; i < this.numPages; ++i) {
        catalogAccessFile.writeInt(this.pageOrder.get(i));
    }

    // Write the number of records to the catalog file
    catalogAccessFile.writeInt(this.numRecords);

    // Write the number of attributes to the catalog file
    catalogAccessFile.writeInt(this.attributes.size());

    // Iterate over each attribute and save its schema to the catalog file
    for (int i = 0; i < this.attributes.size(); ++i) {
        this.attributes.get(i).saveAttributeSchema(catalogAccessFile);
    }
  }


  @Override
  public void addAttribute(AttributeSchema attributeSchema) {
    this.attributes.add(attributeSchema);
  }

    /**
   * Loads the table schema information from the specified random access file.
   *
   * @param catalogAccessFile the random access file from which the table schema information will be loaded
   * @throws IOException if an I/O error occurs while reading from the random access file
   */
  @Override
  public void loadTableSchema(RandomAccessFile catalogAccessFile) throws IOException {
    // Read the number of pages from the catalog file
    this.numPages = catalogAccessFile.readInt();

    // Read page order from the catalog file
    for (int i = 0; i < this.numPages; ++i) {
        this.pageOrder.add(catalogAccessFile.readInt());
    }

    // Read the number of records from the catalog file
    this.numRecords = catalogAccessFile.readInt();

    // Read the number of attributes from the catalog file
    int numOfAttributes = catalogAccessFile.readInt();

    // Iterate over each attribute and load its schema from the catalog file
    for (int i = 0; i < numOfAttributes; ++i) {
        // Create a new AttributeSchema instance
        AttributeSchema attributeSchema = new AttributeSchema();

        // Load attribute schema from the catalog file
        attributeSchema.loadAttributeSchema(catalogAccessFile);

        // Add the loaded attribute schema to the list of attributes
        this.attributes.add(attributeSchema);
    }
  }
}
