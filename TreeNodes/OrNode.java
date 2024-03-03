package TreeNodes;

import StorageManager.TableSchema;
import StorageManager.Objects.Record;
import TreeNodes.Interfaces.OperatorNode;

public class OrNode implements OperatorNode {

    OperatorNode leftChild;
    OperatorNode rigthChild;

    public OrNode(OperatorNode left, OperatorNode right) {
        this.leftChild = left;
        this.rigthChild = right;
    }

    @Override
    public boolean evaluate(TableSchema schema, Record record) {
        return leftChild.evaluate(schema, record) || rigthChild.evaluate(schema, record);
    }

}
