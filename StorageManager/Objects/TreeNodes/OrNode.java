package StorageManager.Objects.TreeNodes;

import StorageManager.TableSchema;
import StorageManager.Objects.Record;
import StorageManager.Objects.TreeNodes.Interfaces.ComparisonNode;

public class OrNode implements ComparisonNode {
    
    ComparisonNode leftChild;
    ComparisonNode rigthChild;

    public OrNode(ComparisonNode left, ComparisonNode right) {
        this.leftChild = left;
        this.rigthChild = right;
    }

    @Override
    public boolean evaluate(TableSchema schema, Record record) {
        return leftChild.evaluate(schema, record) || rigthChild.evaluate(schema, record);
    }

}
