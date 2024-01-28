package StorageManager.Objects;

public class Attribute {
    private String attributeName;
    private String dataType;
    private boolean notNull;
    private boolean primaryKey;
    private boolean unique;

    public Attribute(String attributeName, String dataType, boolean notNull, boolean primaryKey, boolean unique) {
        this.attributeName = attributeName;
        this.dataType = dataType;
        this.notNull = notNull;
        this.primaryKey = primaryKey;
        this.unique = unique;
    }

    public String getAttributeName() {
        return this.attributeName;
    }

    public String getDataType() {
        return this.dataType;
    }

    public boolean isNull() {
        return !this.notNull;
    }

    public boolean isPrimaryKey() {
        return this.primaryKey;
    }

    public boolean isUnique() {
        return this.unique;
    }

        

}
