package Parser.WhereTreeNodes;

import Parser.WhereTreeNodes.Interfaces.OperatorNode;
import StorageManager.TableSchema;
import StorageManager.Objects.Record;

public class WhereTree {
  private OperatorNode root;


  public WhereTree(OperatorNode root) {
    this.root = root;
  }

  public boolean evaluate(TableSchema tableSchema, Record record) throws Exception {
    return root.evaluate(tableSchema, record);
  }


}
