package QueryExecutor;

import java.util.ArrayList;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    // execute - gets all records that are valid per the query
    this.validateQuery();
    this.orderBy();
    this.select();

    MessagePrinter.printMessage(MessageType.SUCCESS, null);
  }

  /**
   * Gets all relevant records, performs a cartesian product if necessary
   *
   * @param storageManager - Storage manager of the program
   * @return - List of relavent records
   * @throws Exception
   */
  private List<Record> getAllRecords(StorageManager storageManager) throws Exception {
    // call build schema to create the schema
    this.buildSchema();

    // check to see if there is only 1 table
    // if so, return all records from that table
    List<String> tables = select.getTableNames();
    List<Record> firstTable = storageManager.getAllRecords(select.getTableNames().get(0));
    if (tables.size() == 1) {
      return firstTable;
    }

    // if more than one table, get all records
    // of all of the tables, and compute the cartesian product of them

    // NOTE: CHECK FOR CLONING ISSUES?

    for (int i = 1; i < tables.size(); i++) {
      List<Record> nextTable = storageManager.getAllRecords(select.getTableNames().get(i));
      List<Record> resultTable = new ArrayList<>();
      for (int j = 0; j < firstTable.size(); j++) {
        for (int k = 0; k < nextTable.size(); k++) {
          resultTable.add(new Record(firstTable.get(j), nextTable.get(k)));
        }
      }
      firstTable = resultTable;
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
    StorageManager storageManager = StorageManager.getStorageManager();

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

  /**
   * Builds the schema for the result of a Cartesian Product.
   * saves the schema to this class
   *
   * @throws Exception
   */
  private void buildSchema() throws Exception {
    Catalog catalog = Catalog.getCatalog();

    if (select.getTableNames().size() == 1) {
      this.schema = catalog.getSchema(select.getTableNames().get(0));
    } else if (select.getTableNames().size() > 1) {

      // create new temp schema
      TableSchema temp = new TableSchema("temp");

      ArrayList<TableSchema> cartSchemas = new ArrayList<TableSchema>();

      // loop through each table name and get their schema
      List<String> tableNames = select.getTableNames();
      for (int i = 0; i < tableNames.size(); i++) {
        cartSchemas.add(catalog.getSchema(tableNames.get(i)));
      }

      // add each attribute in the form of x.y to the temp schema
      // where x is the table name and y is the attribute name
      // return the temp schema
      for (int i = 0; i < cartSchemas.size(); i++) {
        List<AttributeSchema> currentAttrSchema = cartSchemas.get(i).getAttributes();
        for (int j = 0; j < currentAttrSchema.size(); j++) {
          String name = tableNames.get(i) + "." + currentAttrSchema.get(i).getAttributeName();
          temp.addAttribute(new AttributeSchema(name, currentAttrSchema.get(i)));

        }
      }

      this.schema = temp;
    } else {
      MessagePrinter.printMessage(MessageType.ERROR, "No table present in the from clause");
    }

  }

  private void orderBy() throws Exception {
    String orderAttr = this.select.getOrderByAttribute();

    if (orderAttr == null) {
      return;
    }

    List<AttributeSchema> attrs = schema.getAttributes();
    List<String> potentialMatches = new ArrayList<>();
    List<Integer> foundIndex = new ArrayList<>();

    String[] spList = orderAttr.split("\\.");
    if (spList.length > 1) {
      potentialMatches.add(spList[1]);
    } else {
      potentialMatches.add(orderAttr.toLowerCase());
    }

    for (int i = 0; i < attrs.size(); i++) {
      if (potentialMatches.contains(attrs.get(i).getAttributeName())) {
        foundIndex.add(i);
      }
    }

    if (foundIndex.size() != 1) {
      MessagePrinter.printMessage(MessageType.ERROR, "Invalid attribute name: " + orderAttr + ".");
    }

    Collections.sort(this.records, (a, b) -> a.compareTo(b, foundIndex.get(0)));

  }

  private void select() throws Exception {
    List<Record> recordsWithSelectedAttributes = new ArrayList<>();
    List<String> attributeNames = new ArrayList<>();
    Set<String> selectedAttributeNames = new LinkedHashSet<>(this.select.getAttributeNames());

    if (selectedAttributeNames.contains("*")) {
      for (AttributeSchema attributeSchema : this.schema.getAttributes()) {
        attributeNames.add(attributeSchema.getAttributeName());
      }
      recordsWithSelectedAttributes.addAll(this.records);
    } else {
      Map<String, Integer> attributeIndexMap = new HashMap<>();
      for (int i = 0; i < this.schema.getAttributes().size(); i++) {
        AttributeSchema attributeSchema = this.schema.getAttributes().get(i);
        attributeIndexMap.put(attributeSchema.getAttributeName(), i);
      }

      for (String attributeName : selectedAttributeNames) {
        if (this.select.getTableNames().size() == 1 && attributeName.contains(".")) {
          attributeName = attributeName.split("\\.")[1];
        }
        if (!attributeIndexMap.containsKey(attributeName)) {
          MessagePrinter.printMessage(MessageType.ERROR,
              String.format("%s could not be matched with any attribute", attributeName));
        } else {
          attributeNames.add(attributeName);
        }
      }

      for (Record record : this.records) {
        Record newRecord = new Record(new ArrayList<>());
        for (String attributeName : attributeNames) {
          int index = attributeIndexMap.getOrDefault(attributeName, -1);
          if (index != -1) {
            newRecord.getValues().add(record.getValues().get(index));
          }
        }
        recordsWithSelectedAttributes.add(newRecord);
      }
    }

    // build result string
    StringBuilder resultStringBuilder = new StringBuilder();
    resultStringBuilder.append("\n");
    resultStringBuilder.append(buildResultString(recordsWithSelectedAttributes, attributeNames));
    System.out.println(resultStringBuilder.toString());
  }

}
