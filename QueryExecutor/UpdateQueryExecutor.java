package QueryExecutor;

import java.util.ArrayList;
import java.util.List;

import Parser.Insert;
import Parser.Update;
import StorageManager.StorageManager;
import StorageManager.TableSchema;
import StorageManager.Objects.AttributeSchema;
import StorageManager.Objects.Catalog;
import StorageManager.Objects.MessagePrinter;
import StorageManager.Objects.Record;
import StorageManager.Objects.MessagePrinter.MessageType;

public class UpdateQueryExecutor implements QueryExecutorInterface{
  private Update update;
  private List<Record> newRecords;
  private List<Object> primaryKeys;
  private TableSchema tableSchema;

  public UpdateQueryExecutor(Parser.Update update) {
    this.update = update;
    this.newRecords = new ArrayList<>();
    this.primaryKeys = new ArrayList<>();
  }

  @Override
  public void excuteQuery() throws Exception {
    validateQuery();
    StorageManager storageManager = StorageManager.getStorageManager();
    for (int i=0; i < this.newRecords.size(); ++i) {
      storageManager.updateRecord(tableSchema.getTableNumber(), this.newRecords.get(i), storageManager);
    }
    MessagePrinter.printMessage(MessageType.SUCCESS, null);
  }


  private void validateQuery() throws Exception {
    Catalog catalog = Catalog.getCatalog();
    tableSchema = catalog.getSchema(this.update.getTableName());

    AttributeSchema attributeSchema = null;
    int attrbuteIndex = 0;

    for (AttributeSchema aSchema : tableSchema.getAttributes()) {
      if (aSchema.getAttributeName().equals(update.getColumn())) {
        attrbuteIndex = tableSchema.getAttributes().indexOf(aSchema);
        attributeSchema = aSchema;
      }
    }

    if (attributeSchema == null) {
      MessagePrinter.printMessage(MessageType.ERROR, update.getColumn() + " does not exist for table " + update.getTableName());
    }


    List<Record> records = StorageManager.getStorageManager().getAllRecords(tableSchema.getTableNumber());
    Insert insert = new Insert(this.update.getTableName(), null);
    int primaryKeyIndex = tableSchema.getPrimaryIndex();
    InsertQueryExcutor insertQueryExcutor = new InsertQueryExcutor(insert);
    for (Record record : records) {
      if (update.getWhereTree().evaluate(tableSchema, record)) {
        record.getValues().set(attrbuteIndex, this.update.getValue());
        insertQueryExcutor.validateRecord(record);
        this.newRecords.add(record);
        this.primaryKeys.add(record.getValues().get(primaryKeyIndex));
      }
    }
  }

}
