package StorageManager.Objects;

public class Attribute implements java.io.Serializable {
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
        return attributeName;
    }

    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public boolean isNotNull() {
        return notNull;
    }

    public void setNotNull(boolean notNull) {
        this.notNull = notNull;
    }

    public boolean isPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(boolean primaryKey) {
        this.primaryKey = primaryKey;
    }

    public boolean isUnique() {
        return unique;
    }

    public void setUnique(boolean unique) {
        this.unique = unique;
    }

}
