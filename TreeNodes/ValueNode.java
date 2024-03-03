package TreeNodes;

import StorageManager.TableSchema;
import StorageManager.Objects.Record;
import TreeNodes.Interfaces.OperandNode;

/*
* A literal value node, this should be a leaf
*/
public class ValueNode implements OperandNode {

    private String dataType;
    private Object value;

    public ValueNode(String type, Object value) {
        this.dataType = type;
        this.value = value;
    }

    @Override
    public Object getValue(TableSchema schema, Record record) {
        return this.value;
    }

}
