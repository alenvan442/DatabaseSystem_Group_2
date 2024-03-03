package StorageManager.Objects.TreeNodes;

import StorageManager.TableSchema;
import StorageManager.Objects.Record;

/*
 * Interface for the leaf nodes
 */
public interface WhereLeafNodeInterface {
    
    /*
     * Returns the value of the leaf node
     * If the leaf node is a literal, then return the literal
     * If the leaf node is a column name, retrieve the value from the passed in record
     * data types will be validated in the operator node
     */
    public Object getValue(TableSchema schema, Record record);

}
