package StorageManager;

import java.util.List;

import StorageManager.Objects.Attribute;

public class TableSchema {
    private int tableNumber;
    private String tableName;
    private List<Attribute> attributes;
    private Attribute primaryKey;

    public TableSchema(String tableName) {

    }

    public int getTableNumber() {
        return 0;
    }

    public String getTableName() {
        return null;
    } 

    public void setTableName(String tableName) {

    }

    public Attribute getPrimaryKey() {
        return this.primaryKey;
    }

    public void setPrimaryKey(Attribute attribute) {
    
    }

    public void addAttribute(Attribute attribute) {

    }

    public boolean removeAttribute(Attribute attribute) {
        return false;
    }

    public List<Attribute> getAttribtues() {
        return this.attributes;
    }


}
