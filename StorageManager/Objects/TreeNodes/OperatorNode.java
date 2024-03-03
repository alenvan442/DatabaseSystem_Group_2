package StorageManager.Objects.TreeNodes;

import StorageManager.TableSchema;

/*
* Node that holds what operation is to be performed (+, -, *, /, and, or, etc.)
* This will always be the root node
*/
public class OperatorNode {

    WhereLeafNodeInterface rigthChild;
    WhereLeafNodeInterface leftChild;

    public boolean evaluate(TableSchema schema, Record record) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'evaluate'");
    }

}
