package StorageManager.Objects;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class AttributeSchema implements java.io.Serializable {
    private String attributeName;
    private String dataType;
    private boolean notNull;
    private boolean primaryKey;
    private boolean unique;

    public AttributeSchema(String attributeName, String dataType, boolean notNull, boolean primaryKey, boolean unique) {
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

    public byte[] convertToBytes() throws IOException {
        ByteArrayOutputStream attributeByteArray = new ByteArrayOutputStream();
        attributeByteArray.write(this.attributeName.getBytes(StandardCharsets.UTF_8));
        attributeByteArray.write(this.dataType.getBytes(StandardCharsets.UTF_8));
        attributeByteArray.write(this.notNull ? new byte[]{1} : new byte[]{0});
        attributeByteArray.write(this.primaryKey ? new byte[]{1} : new byte[]{0});
        attributeByteArray.write(this.unique ? new byte[]{1} : new byte[]{0});
        return attributeByteArray.toByteArray();
    }

}
