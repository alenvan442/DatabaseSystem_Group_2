package QueryExecutor;

import java.util.ArrayList;
import java.util.List;

import Parser.Select;
import Parser.WhereTreeNodes.WhereTree;
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
  private List<Record> records;

  public SelectQueryExecutor(Select select) {
    this.select = select;
  }

  @Override
  public void excuteQuery() throws Exception {
    // execute
    this.validateQuery();

    List<String> attributeNames = new ArrayList<>();
    for (AttributeSchema attributeSchema : this.schema.getAttributes()) {
      attributeNames.add(attributeSchema.getAttributeName());
    }

    // buid result string
    System.out.println("\n" + buildResultString(this.records, attributeNames));
    MessagePrinter.printMessage(MessageType.SUCCESS, null);
  }

  private List<Record> getAllRecords(StorageManager storageManager) throws Exception {
    Catalog catalog = Catalog.getCatalog();
    // TODO call validate query to create the schema
    this.validateQuery();

    // TODO check to see if there is only 1 table
    // if so, return all records from that table
    List<String> tables = select.getTableNames();
    List<Record> firstTable = storageManager.getAllRecords(catalog.getSchema( select.getTableNames().get(0)).getTableNumber());
    if(tables.size()>1){
      return firstTable;
    }

    // TODO if more than one table, get all records
    // of all of the tables, and compute the cartesian product of them

    //List<Record> resultTable = new ArrayList<>();

    for(int i=1; i<tables.size(); i++){
      List<Record> nextTable = storageManager.getAllRecords(catalog.getSchema( select.getTableNames().get(i)).getTableNumber());
      List<Record> resultTable = new ArrayList<>();
      for( int j=0; j<firstTable.size(); j++){
          for(int k=0; k<nextTable.size(); k++){
            resultTable.add(new Record(firstTable.get(j),nextTable.get(k)));
        }
      }
      //Is this legal?
      firstTable= resultTable;
    }
    
    return firstTable;

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
    StorageManager storageManager = StorageManager.getStorageManager();

    if (select.getTableNames().size() > 1) {
      this.schema = this.buildCartesianSchema();
    } else {
      this.schema = catalog.getSchema(select.getTableNames().get(0));
    }

    List<Record> allRecords = this.getAllRecords(storageManager);

    this.records = new ArrayList<>();
    if (this.select.getWhereTree() != null) {
      WhereTree where = this.select.getWhereTree();
      for (Record i : allRecords) {
        if (where.evaluate(this.schema, i)) {
          this.records.add(i);
        }
      }
    } else {
      this.records.addAll(allRecords);
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
