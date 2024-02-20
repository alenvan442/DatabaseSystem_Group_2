package QueryExecutor;

import java.util.ArrayList;
import java.util.List;

import StorageManager.StorageManager;
import StorageManager.TableSchema;
import StorageManager.Objects.AttributeSchema;
import StorageManager.Objects.Catalog;
import StorageManager.Objects.Record;

public class SelectQueryExecutor implements QueryExecutorInterface {
  private String name;
  private String query;

  public SelectQueryExecutor(String name, String query) {
    this.name = name;
    this.query = query;
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
    System.out.println("\n"+ buildResultString(record, attributeNames));
  }

  private String buildResultString(List<Record> records, List<String> attributeNames) {
    StringBuilder resultString = new StringBuilder();

    for (int i=0; i < attributeNames.size(); ++i) {
      resultString.append("-------");
    }

    resultString.append("\n");

    for (String attributeName : attributeNames) {
      resultString.append("| " + attributeName + " ");
    }

    resultString.append("|\n");

    for (int i=0; i < attributeNames.size(); ++i) {
      resultString.append("-------");
    }

    for (Record record : records) {
      for (Object value : record.getValues()) {
        resultString.append("| " + value.toString());
      }
      resultString.append("|\n");
    }

    return resultString.toString();


  }

  private TableSchema validateQuery() throws Exception {
    Catalog catalog = Catalog.getCatalog();
    return catalog.getSchema(this.name);
  }

}
