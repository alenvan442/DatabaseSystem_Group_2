package Parser;

import java.util.Queue;
import java.util.Stack;

import Parser.WhereTreeNodes.WhereTree;
import Parser.WhereTreeNodes.Interfaces.OperatorNode;

public class WhereTreeBuilder {
  Queue<Token> postfixExpression;
  Stack<OperatorNode> operatorStack;

  public WhereTreeBuilder(Queue<Token> postfixExpression) {
    this.postfixExpression = postfixExpression;
    this.operatorStack = new Stack<>();
  }

  public WhereTree buildWhileTree() {
    return new WhereTree(null);
  }


}
