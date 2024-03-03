package TreeNodes;

import StorageManager.TableSchema;
import TreeNodes.Interfaces.OperandNode;
import TreeNodes.Interfaces.OperatorNode;

/*
* Node that holds what operation is to be performed (=, <=, <, >, >=, !=)
* This will always be the root node
*/
public class ComparisonOpNode implements OperatorNode {

    OperandNode rigthChild;
    OperandNode leftChild;
    String operator;

    public ComparisonOpNode(OperandNode left, OperandNode right, String operator) {
        this.leftChild = left;
        this.rigthChild = right;
        this.operator = operator;
    }

    @Override
    public boolean evaluate(TableSchema schema, StorageManager.Objects.Record record) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'evaluate'");
        // idea: get the value of left and right child, then case switch based on operator
        // then use operator to return a boolean.
    }

}
