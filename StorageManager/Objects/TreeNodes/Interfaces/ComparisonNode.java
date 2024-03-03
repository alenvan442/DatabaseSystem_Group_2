package StorageManager.Objects.TreeNodes.Interfaces;

import StorageManager.TableSchema;
import StorageManager.Objects.Record;

/*
 * Used for all comparison operators: +, -, *, /, and, or
 */
public interface ComparisonNode {
     
    public boolean evaluate(TableSchema schema, Record record);

}
