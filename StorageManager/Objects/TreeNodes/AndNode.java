package StorageManager.Objects.TreeNodes;

import StorageManager.TableSchema;
import StorageManager.Objects.Record;
import StorageManager.Objects.TreeNodes.Interfaces.OperatorNode;

public class AndNode implements OperatorNode {

    OperatorNode leftChild;
    OperatorNode rigthChild;

    public AndNode(OperatorNode left, OperatorNode right) {
        this.leftChild = left;
        this.rigthChild = right;
    }

    @Override
    public boolean evaluate(TableSchema schema, Record record) {
        return leftChild.evaluate(schema, record) && rigthChild.evaluate(schema, record);
    }

}
