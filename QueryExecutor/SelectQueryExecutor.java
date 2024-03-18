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

  public SelectQueryExecutor(Select select) {
    this.select = select;
  }

  @Override
  public void excuteQuery() throws Exception {
    // validate
    TableSchema tableSchema = validateQuery();

    // execute
    StorageManager storageManager = StorageManager.getStorageManager();
    List<Record> record = storageManager.getAllRecords(tableSchema.getTableNumber());
    List<String> attributeNames = new ArrayList<>();
    for (AttributeSchema attributeSchema : tableSchema.getAttributes()) {
      attributeNames.add(attributeSchema.getAttributeName());
    }

    // buid result string
    System.out.println("\n" + buildResultString(record, attributeNames));
    MessagePrinter.printMessage(MessageType.SUCCESS, null);
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

  private TableSchema validateQuery() throws Exception {
    Catalog catalog = Catalog.getCatalog();
    return catalog.getSchema(select.getTableNames().get(0));
  }

}
