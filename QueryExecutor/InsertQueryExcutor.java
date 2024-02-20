package QueryExecutor;


import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import StorageManager.StorageManager;
import StorageManager.TableSchema;
import StorageManager.Objects.AttributeSchema;
import StorageManager.Objects.Catalog;
import StorageManager.Objects.MessagePrinter;
import StorageManager.Objects.Record;
import StorageManager.Objects.MessagePrinter.MessageType;

public class InsertQueryExcutor implements QueryExecutorInterface{

  private String name;
  private List<Record> records;
  private String query;

  public InsertQueryExcutor(String name, List<Record> records, String query) {
    this.name = name;
    this.records = records;
    this.query = query;
  }

  @Override
  public void excuteQuery() throws Exception {
    validateQuery();
  }

  private void validateQuery() throws Exception {
    Catalog catalog = Catalog.getCatalog();
    TableSchema tableSchema = catalog.getSchema(this.name);
    List<AttributeSchema> attributeSchemas = tableSchema.getAttributes();
    for (Record record : this.records) {
      checkCorrectNumbberOfValues(attributeSchemas, record);
      checkDataTypes(attributeSchemas, record);

      // determine index of the primary key
      int primaryKeyIndex = tableSchema.getPrimaryIndex();
      List<AttributeSchema> attrs = tableSchema.getAttributes();

      // check unique constraints
      List<Integer> uniqueAttributes = new ArrayList<>();
      for (int i = 0; i < attrs.size(); i++) {
          if (attrs.get(i).isUnique()) {
              uniqueAttributes.add(i);
          }
      }

      // confirm unique
      if (uniqueAttributes.size() > 0) {
          this.checkUniqueContraints(uniqueAttributes, tableSchema.getTableNumber(), primaryKeyIndex, record);
      }
    }

  }


  private void checkCorrectNumbberOfValues(List<AttributeSchema> attributeSchemas, Record record) throws Exception {
    // check for correct number of values
    if (attributeSchemas.size() != record.getValues().size()) {
      StringBuilder expected = new StringBuilder();
      StringBuilder got = new StringBuilder();
      String row = "";

      Pattern pattern = Pattern.compile("\\((.*?)\\)");
      Matcher matcher = pattern.matcher(query);
      row = matcher.group(0);

      boolean addSpace = false;
      for (AttributeSchema attributeSchema: attributeSchemas) {
        if (addSpace) got.append(" ");
        expected.append(attributeSchema.getDataType());
        addSpace = true;
      }

      addSpace = false;
      for (Object value: record.getValues()) {
        if (addSpace) got.append(" ");
        String valueDataType = getDataType(value, attributeSchemas.get(record.getValues().indexOf(value)));
        got.append(valueDataType);
        addSpace = true;
      }

      MessagePrinter.printMessage(MessageType.ERROR, String.format("row %s: Too many attributes: expected (%s) for (%s)", row, expected, got));
    }
  }

  private void checkDataTypes(List<AttributeSchema> attributeSchemas, Record record) throws Exception {

    for (int i=0; i < attributeSchemas.size(); ++i) {
      if (!attributeSchemas.get(i).isNotNull()) {
        if (record.getValues().get(i) == null) {
          continue;
        }
      }

      String expected = attributeSchemas.get(i).getDataType();
      String got = getDataType(record.getValues().get(i), attributeSchemas.get(i));
      if (expected.equals("integer")) {
        if (!(record.getValues().get(i) instanceof Integer)) {
          MessagePrinter.printMessage(MessageType.ERROR, String.format("Invalid data type: expected (%s) got (%s)", expected, got));
        }
      } else if (expected.equals("double")) {
        if (!(record.getValues().get(i) instanceof Double)) {
          MessagePrinter.printMessage(MessageType.ERROR, String.format("Invalid data type: expected (%s) got (%s)", expected, got));
        }
      } else if (expected.equals("boolean")) {
        if (!(record.getValues().get(i) instanceof Boolean)) {
          MessagePrinter.printMessage(MessageType.ERROR, String.format("Invalid data type: expected (%s) got (%s)", expected, got));
        }
      } else if (expected.contains("char") || expected.contains("varchar")) {
        if (! (record.getValues().get(i) instanceof String)) {
          MessagePrinter.printMessage(MessageType.ERROR, String.format("Invalid data type: expected (%s) got (%s)", expected, got));
        } else {
          checkSizeOfStrings(((String)record.getValues().get(i)), expected);
        }
      }
    }
  }

  private void checkSizeOfStrings(String value, String dataType) throws Exception {
    Pattern pattern = Pattern.compile("\\((\\d+)\\)");
    Matcher matcher = pattern.matcher(dataType);
    int size = Integer.parseInt(matcher.group(0));
    pattern = Pattern.compile("\\((.*?)\\)");
    matcher = pattern.matcher(this.query);
    String row = matcher.group(0);
    if (dataType.contains("char")) {
      if (value.length() != size) {
        MessagePrinter.printMessage(MessageType.ERROR, String.format("row %s: %s can only accept %d chars; %s is %d", row, dataType, size, value, value.length()));
      }
    } else {
      if (value.length() > size) {
        MessagePrinter.printMessage(MessageType.ERROR, String.format("row %s: %s can only accept %d chars or less; %s is %d", row, dataType, size, value, value.length()));
      }
    }
  }


  private void checkUniqueContraints(List<Integer> uniqueAttributeIndexes, int tableNumber, int primaryKeyIndex, Record newRecord) throws Exception {
    List<Record> records = StorageManager.getStorageManager().getAllRecords(tableNumber);
    for (Record record : records){
        for (Integer attributeIndex : uniqueAttributeIndexes) {
            if (newRecord.compareTo(record, attributeIndex) == 0) {
                if (attributeIndex == primaryKeyIndex) {
                    MessagePrinter.printMessage(MessageType.ERROR, String.format("row (%d): Duplicate %s for row (%d)", printRow(record), "primary key", printRow(record)));
                } else {
                    MessagePrinter.printMessage(MessageType.ERROR, String.format("row (%d): Duplicate %s for row (%d)", printRow(record), "value", printRow(record)));
                }
            }
        }
    }
  }

  private String printRow(Record record) {
    StringBuilder row = new StringBuilder();
    Boolean addSpace = false;
    for (Object value: record.getValues()) {
      if (addSpace) row.append(" ");
      row.append(value.toString());
      addSpace = true;
    }
    return row.toString();
}

  private String getDataType(Object object, AttributeSchema attributeSchema) {
    if (object instanceof Integer) {
      return "integer";
    } else if (object instanceof Double) {
      return "double";
    } else if (object instanceof Boolean) {
      return "boolean";
    } else if (object instanceof String) {
      if (attributeSchema.getDataType().contains("char")) {
        return "char(" + ((String) object).length() + ")";
      } else {
        return "varchar(" + ((String) object).length() + ")";
      }
    }
    return "null";

  }

}
