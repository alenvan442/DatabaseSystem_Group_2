package QueryExecutor;

import StorageManager.TableSchema;
import StorageManager.Objects.Catalog;

public class DDLQueryExecutor implements QueryExecutorInterface{

  private String name;
  private String action;
  private TableSchema tableSchema;
  private String defaultValue; // alter use
  private String attributeName; //alter use
  private String dataType; // alter use
  private String attributeAction;


  public DDLQueryExecutor(String name, String action, String defaultValue, String attributeName, String dataType,
      String attributeAction) {
    this.name = name;
    this.action = action;
    this.defaultValue = defaultValue;
    this.attributeName = attributeName;
    this.dataType = dataType;
    this.attributeAction = attributeAction;
  }

  public DDLQueryExecutor(String name, String action) {
    this.name = name;
    this.action = action;
  }

  public DDLQueryExecutor(String action, TableSchema tableSchema) {
    this.action = action;
    this.tableSchema = tableSchema;
  }





  @Override
  public void excuteQuery() throws Exception {
    // validate and execute
    Catalog catalog = Catalog.getCatalog();
    switch (this.action) {
      case "create":
        catalog.createTable(this.tableSchema);
        break;
      case "drop":
        TableSchema tableSchema = catalog.getSchema(this.name);
        catalog.dropTableSchema(tableSchema.getTableNumber());
        break;
      case "alter":
        tableSchema = catalog.getSchema(this.name);
        
      default:
        break;
    }
  }

}
