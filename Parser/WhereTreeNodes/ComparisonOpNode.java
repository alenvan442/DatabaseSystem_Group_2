package Parser.WhereTreeNodes;

import Parser.WhereTreeNodes.Interfaces.OperandNode;
import Parser.WhereTreeNodes.Interfaces.OperatorNode;
import StorageManager.TableSchema;

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
        // we will check to see if the data types of what the getValue() 
        // of the left and right childs returns are the same here
            // if not raise error
    }

}
