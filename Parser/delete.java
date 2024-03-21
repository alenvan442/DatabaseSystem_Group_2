package Parser;

import Parser.WhereTreeNodes.WhereTree;

public class delete {
  String name;
  WhereTree whereTree;
  
  public delete(String name, WhereTree whereTree) {
    this.name = name;
    this.whereTree = whereTree;
  }
}
