package QueryExecutor;

import java.util.ArrayList;
import java.util.List;

import Parser.Select;
import StorageManager.StorageManager;
import StorageManager.TableSchema;
import StorageManager.Objects.AttributeSchema;
import StorageManager.Objects.Catalog;
import StorageManager.Objects.MessagePrinter;
import StorageManager.Objects.Record;
import StorageManager.Objects.MessagePrinter.MessageType;

public class SelectQueryExecutor implements QueryExecutorInterface {
  private Select select;
  private TableSchema schema;

  public SelectQueryExecutor(Select select) {
    this.select = select;
  }

  @Override
  public void excuteQuery() throws Exception {
    // execute
    StorageManager storageManager = StorageManager.getStorageManager();
    this.validateQuery();
    List<Record> record = storageManager.getAllRecords(this.schema.getTableNumber());
    List<String> attributeNames = new ArrayList<>();
    for (AttributeSchema attributeSchema : this.schema.getAttributes()) {
      attributeNames.add(attributeSchema.getAttributeName());
    }

    // buid result string
    System.out.println("\n" + buildResultString(record, attributeNames));
    MessagePrinter.printMessage(MessageType.SUCCESS, null);
  }

  private List<Record> getRecords() {
    Catalog catalog = Catalog.getCatalog();
    // TODO call validate query to create the schema

    // TODO check to see if there is only 1 table
    // if so, return all records from that table

    // TODO if more than one table, get all records
    // of all of the tables, and compute the cartesian product of them
    
  }

  private String buildResultString(List<Record> records, List<String> attributeNames) {
    int numAttributes = attributeNames.size();
    int[] columnWidths = new int[numAttributes];

    // Calculate maximum width for each column
    for (int i = 0; i < numAttributes; i++) {
      int maxWidth = attributeNames.get(i).length();
      for (Record record : records) {
        Object value = record.getValues().get(i);
        if (value != null) {
          maxWidth = Math.max(maxWidth, value.toString().length());
        }
      }
      columnWidths[i] = maxWidth;
    }

    StringBuilder resultString = new StringBuilder();

    // Build top border
    for (int width : columnWidths) {
      resultString.append("-").append("-".repeat(width + 2)); // Add 2 for padding
    }
    resultString.append("-\n");

    // Build attribute names row
    for (int i = 0; i < numAttributes; i++) {
      String attributeName = attributeNames.get(i);
      resultString.append(String.format("| %-" + (columnWidths[i]) + "s ", attributeName));
    }
    resultString.append("|\n");

    // Build separator row
    for (int i = 0; i < numAttributes; i++) {
      resultString.append("|" + "-".repeat(columnWidths[i] + 2)); // Add 2 for padding
    }
    resultString.append("|\n");

    // Build data rows
    for (Record record : records) {
      for (int i = 0; i < numAttributes; i++) {
        Object value = record.getValues().get(i);
        String formattedValue = (value == null) ? "" : value.toString();
        resultString.append(String.format("| %-" + (columnWidths[i]) + "s ", formattedValue));
      }
      resultString.append("|\n");
    }

    return resultString.toString();
  }

  private void validateQuery() throws Exception {
    Catalog catalog = Catalog.getCatalog();
    if (select.getTableNames().size() > 1) {
      this.schema = this.buildCartesianSchema();
    } else {
      this.schema = catalog.getSchema(select.getTableNames().get(0));
    }
  }

  private TableSchema buildCartesianSchema() {
    // create new temp schema
    TableSchema temp = new TableSchema("temp");

    // TODO loop through each table name and get their schema
    // then add each attribute in the form of x.y to the temp schema
    // where x is the table name and y is the attribute name
    // return the temp schema
   
    return temp;

  }

}
