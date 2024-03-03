package TreeNodes.Interfaces;

import StorageManager.TableSchema;
import StorageManager.Objects.Record;

/*
 * Used for all comparison operators: +, -, *, /, and, or
 */
public interface OperatorNode {

    public boolean evaluate(TableSchema schema, Record record);

}
