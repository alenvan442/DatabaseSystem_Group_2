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

public class InsertQueryExcutor implements QueryExecutorInterface {

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

    for (Record record : this.records) {
      int tableNumber = validateQuery(record);
      StorageManager.getStorageManager().insertRecord(tableNumber, record);
    }

    MessagePrinter.printMessage(MessageType.SUCCESS, null);
  }

  private int validateQuery(Record record) throws Exception {
    Catalog catalog = Catalog.getCatalog();
    TableSchema tableSchema = catalog.getSchema(this.name);
    List<AttributeSchema> attributeSchemas = tableSchema.getAttributes();
    checkCorrectNumberOfValues(attributeSchemas, record);
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

    return tableSchema.getTableNumber();

  }

  private void checkCorrectNumberOfValues(List<AttributeSchema> attributeSchemas, Record record) throws Exception {
    // check for correct number of values
    if (attributeSchemas.size() != record.getValues().size()) {
      StringBuilder expected = new StringBuilder();
      StringBuilder got = new StringBuilder();
      String row = "";

      Pattern pattern = Pattern.compile("\\((.*?)\\)");
      Matcher matcher = pattern.matcher(query);
      while (matcher.find()) {
        row = matcher.group(0);
      }

      getGotAndExpected(attributeSchemas, record, got, expected);

      MessagePrinter.printMessage(MessageType.ERROR,
          String.format("row %s: Too many attributes: expected (%s) for (%s)", row, expected, got));
    }
  }

  private void checkDataTypes(List<AttributeSchema> attributeSchemas, Record record) throws Exception {

    for (int i = 0; i < attributeSchemas.size(); ++i) {
      if (!attributeSchemas.get(i).isNotNull()) {
        if (record.getValues().get(i) == null) {
          continue;
        }
      }

      Pattern pattern = Pattern.compile("\\((.*?)\\)");
      Matcher matcher = pattern.matcher(this.query);

      String row = "";
      while (matcher.find()) {
        row = matcher.group(this.records.indexOf(record));
      }

      String expectedDataType = attributeSchemas.get(i).getDataType();


      StringBuilder expected = new StringBuilder();
      StringBuilder got = new StringBuilder();


      if (expectedDataType.equals("integer")) {
        if (!(record.getValues().get(i) instanceof Integer)) {
          getGotAndExpected(attributeSchemas, record, got, expected);
          MessagePrinter.printMessage(MessageType.ERROR,
              String.format("row %s: Invalid data type: expected (%s) got (%s).", row, expected, got));
        }
      } else if (expectedDataType.equals("double")) {
        if (!(record.getValues().get(i) instanceof Double)) {
          getGotAndExpected(attributeSchemas, record, got, expected);
          MessagePrinter.printMessage(MessageType.ERROR,
              String.format("row %s: Invalid data type: expected (%s) got (%s).", row, expected, got));
        }
      } else if (expectedDataType.equals("boolean")) {
        if (!(record.getValues().get(i) instanceof Boolean)) {
          getGotAndExpected(attributeSchemas, record, got, expected);
          MessagePrinter.printMessage(MessageType.ERROR,
              String.format("row %s: Invalid data type: expected (%s) got (%s).", row, expected, got));
        }
      } else if (expectedDataType.contains("char") || expectedDataType.contains("varchar")) {
        if (!(record.getValues().get(i) instanceof String)) {
          getGotAndExpected(attributeSchemas, record, got, expected);
          MessagePrinter.printMessage(MessageType.ERROR,
              String.format("row %s: Invalid data type: expected (%s) got (%s).", row, expected, got));
        } else {
          checkSizeOfStrings(((String) record.getValues().get(i)), expectedDataType);
        }
      } else {
        MessagePrinter.printMessage(MessageType.ERROR,
            String.format("row %s: Invalid data type: expected (%s) got (%s).", row, expected, got));
      }
    }
  }

  private void checkSizeOfStrings(String value, String dataType) throws Exception {
    Pattern pattern = Pattern.compile("\\((\\d+)\\)");
    Matcher matcher = pattern.matcher(dataType);
    int size = 0;
    while (matcher.find()) {
      size = Integer.parseInt(matcher.group(1));
    }
    pattern = Pattern.compile("\\((.*?)\\)");
    matcher = pattern.matcher(this.query);

    String row = "";
    while (matcher.find()) {
      row = matcher.group(0);
    }

    if (dataType.contains("varchar")) {
      if (value.length() > size) {
        MessagePrinter.printMessage(MessageType.ERROR, String.format(
            "row %s: %s can only accept %d chars or less; %s is %d", row, dataType, size, value, value.length()));
      }
    } else {
      if (value.length() != size) {
        MessagePrinter.printMessage(MessageType.ERROR,
            String.format("row %s: %s can only accept %d chars; \"%s\" is %d", row, dataType, size, value, value.length()));
      }
    }
  }

  private void checkUniqueContraints(List<Integer> uniqueAttributeIndexes, int tableNumber, int primaryKeyIndex,
      Record newRecord) throws Exception {
    List<Record> records = StorageManager.getStorageManager().getAllRecords(tableNumber);
    for (Record record : records) {
      for (Integer attributeIndex : uniqueAttributeIndexes) {
        if (newRecord.compareTo(record, attributeIndex) == 0) {
          if (attributeIndex == primaryKeyIndex) {
            MessagePrinter.printMessage(MessageType.ERROR, String.format("row (%s): Duplicate %s for row (%s)",
                printRow(record), "primary key", printRow(record)));
          } else {
            MessagePrinter.printMessage(MessageType.ERROR,
                String.format("row (%s): Duplicate %s for row (%s)", printRow(record), "value", printRow(record)));
          }
        }
      }
    }
  }

  private String printRow(Record record) {
    StringBuilder row = new StringBuilder();
    Boolean addSpace = false;
    for (Object value : record.getValues()) {
      if (addSpace)
        row.append(" ");
      row.append(value.toString());
      addSpace = true;
    }
    return row.toString();
  }

  private void getGotAndExpected(List<AttributeSchema> attributeSchemas, Record record, StringBuilder got,
      StringBuilder expected) {
    boolean addSpace = false;
    for (AttributeSchema attributeSchema : attributeSchemas) {
      if (addSpace)
        expected.append(" ");
      expected.append(attributeSchema.getDataType());
      addSpace = true;
    }

    addSpace = false;
    for (Object value : record.getValues()) {
      if (addSpace)
        got.append(" ");
      String valueDataType = getDataType(value, expected.toString().contains("char") ? "char": "varchar");
      got.append(valueDataType);
      addSpace = true;
    }
  }

  public static String getDataType(Object object, String dataTypeForString) {
    if (object instanceof Integer) {
      return "integer";
    } else if (object instanceof Double) {
      return "double";
    } else if (object instanceof Boolean) {
      return "boolean";
    } else if (object instanceof String) {
      if (dataTypeForString.contains("char")) {
        return "char(" + ((String) object).length() + ")";
      } else {
        return "varchar(" + ((String) object).length() + ")";
      }
    }
    return "null";

  }

}
