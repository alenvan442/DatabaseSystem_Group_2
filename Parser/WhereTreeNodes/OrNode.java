package Parser.WhereTreeNodes;

import Parser.WhereTreeNodes.Interfaces.OperatorNode;
import StorageManager.TableSchema;
import StorageManager.Objects.Record;

public class OrNode implements OperatorNode {

    OperatorNode leftChild;
    OperatorNode rigthChild;

    public OrNode(OperatorNode left, OperatorNode right) {
        this.leftChild = left;
        this.rigthChild = right;
    }

    @Override
    public boolean evaluate(TableSchema schema, Record record) throws Exception {
        return leftChild.evaluate(schema, record) || rigthChild.evaluate(schema, record);
    }

}
