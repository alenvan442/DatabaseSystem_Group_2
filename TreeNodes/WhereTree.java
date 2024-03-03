package TreeNodes;

import StorageManager.TableSchema;
import StorageManager.Objects.Record;
import TreeNodes.Interfaces.OperatorNode;

public class WhereTree {
  private OperatorNode root;


  public WhereTree(OperatorNode root) {
    this.root = root;
  }

  public boolean evaluate(TableSchema tableSchema, Record record) {
    return root.evaluate(null, null);
  }


}
