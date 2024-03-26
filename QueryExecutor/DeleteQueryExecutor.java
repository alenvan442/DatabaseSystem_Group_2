package QueryExecutor;

import java.util.List;

import Parser.Delete;
import StorageManager.StorageManager;
import StorageManager.TableSchema;
import StorageManager.Objects.Catalog;
import StorageManager.Objects.Record;

public class DeleteQueryExecutor implements QueryExecutorInterface{
  private Delete delete;
  private List<Object> primaryKeys;
  private TableSchema tableSchema;

  public DeleteQueryExecutor(Parser.Delete delete) {
    this.delete = delete;
  }

  @Override
  public void excuteQuery() throws Exception {
    validateQuery();
    StorageManager storageManager = StorageManager.getStorageManager();
    for (Object primaryKey: primaryKeys) {
      storageManager.deleteRecord(tableSchema.getTableNumber(), primaryKey);
    }
  }

  private void validateQuery() throws Exception {
    Catalog catalog = Catalog.getCatalog();
    tableSchema = catalog.getSchema(this.delete.getTableName());

    if (this.delete.getWhereTree() != null) {
      List<Record> records = StorageManager.getStorageManager().getAllRecords(tableSchema.getTableNumber());
      int primaryKeyIndex = tableSchema.getPrimaryIndex();
      for (Record record : records) {
        if (this.delete.getWhereTree().evaluate(tableSchema, record)) {
          this.primaryKeys.add(record.getValues().get(primaryKeyIndex));
        }
      }
    }
  }
}
