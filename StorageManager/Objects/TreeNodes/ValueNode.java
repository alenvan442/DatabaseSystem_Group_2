package StorageManager.Objects.TreeNodes;

import StorageManager.TableSchema;
import StorageManager.Objects.Record;

/*
* A literal value node, this should be a leaf
*/
public class ValueNode implements WhereLeafNodeInterface {
    
    private String dataType;
    private Object value;
    
    @Override
    public Object getValue(TableSchema schema, Record record) {
        return this.value;
    }

}
