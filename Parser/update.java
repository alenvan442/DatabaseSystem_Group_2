package Parser;

import Parser.WhereTreeNodes.WhereTree;

public class update {
  String name;
  String column;
  Object value;
  WhereTree whereTree;

  public update(String name, String column, Object value, WhereTree whereTree) {
    this.name = name;
    this.column = column;
    this.value = value;
    this.whereTree = whereTree;
  }
}
