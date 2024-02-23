package StorageManager;

import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

import StorageManager.Objects.AttributeSchema;
import StorageManager.Objects.SchemaInterface;

public class TableSchema implements SchemaInterface {
    private int tableNumber;
    private String tableName;
    private List<AttributeSchema> attributes;
    private int numPages;
    private List<Integer> pageOrder;
    private int numRecords;

    public TableSchema(String tableName, int tableNumber) {
      this.tableName = tableName;
      this.tableNumber = tableNumber;
      this.numPages = 0;
      this.pageOrder = new ArrayList<Integer>();
      this.numRecords = 0;
      this.attributes = new ArrayList<AttributeSchema>();
    }

    public TableSchema(String tableName) {
      this.tableName = tableName;
      this.tableNumber = this.hashName();
      this.numPages = 0;
      this.pageOrder = new ArrayList<Integer>();
      this.numRecords = 0;
      this.attributes = new ArrayList<AttributeSchema>();
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


    public List<AttributeSchema> getAttributes() {
      return attributes;
    }

    public void setAttributes(List<AttributeSchema> attributes) {
      this.attributes = attributes;
    }

    public int getNumPages() {
      return numPages;
    }


    public void setNumPages() {
      this.numPages = this.pageOrder.size();
    }

    public void incrementNumRecords() {
      this.numRecords += 1;
    }

    public void decrementNumRecords() {
      this.numRecords -= 1;
    }

    public List<Integer> getPageOrder() {
      return this.pageOrder;
    }

    public void setPageOrder(List<Integer> pageOrder) {
      this.pageOrder = pageOrder;
    }

    public void addPageNumber(int pageNumber) {
      this.pageOrder.add(pageNumber);
      this.setNumPages();
    }

    public void addPageNumber(int numberBefore, int pageNumber) {
      int index = 0;
      for (int i = 0; i < this.pageOrder.size(); i++) {
        if (this.pageOrder.get(i) == numberBefore) {
          index = i;
          break;
        }
      }

      this.pageOrder.add(index+1, pageNumber);
      this.setNumPages();

    }

    public int getRecords() {
      return numRecords;
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
   * {@inheritDoc}
   */
  @Override
  public void saveSchema(RandomAccessFile catalogAccessFile) throws Exception {
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
        this.attributes.get(i).saveSchema(catalogAccessFile);
    }
  }

  public void addAttribute(AttributeSchema attributeSchema) {
    this.attributes.add(attributeSchema);
  }

  /**
    * {@inheritDoc}
   */
  @Override
  public void loadSchema(RandomAccessFile catalogAccessFile) throws Exception {
    // Read the number of pages from the catalog file
    this.numPages = catalogAccessFile.readInt();

    // Read page order from the catalog file
    for (int i = 0; i < this.numPages; i++) {
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
        attributeSchema.loadSchema(catalogAccessFile);

        // Add the loaded attribute schema to the list of attributes
        this.attributes.add(attributeSchema);
    }
  }

    public int getPrimaryIndex() {
      // determine index of the primary key
      int primaryIndex = -1;
      for (int i = 0; i < this.attributes.size(); i++) {
          if (this.attributes.get(i).isPrimaryKey()) {
              primaryIndex = i;
          }
      }
      return primaryIndex;
    }

}
