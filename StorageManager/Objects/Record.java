package StorageManager.Objects;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.List;

import StorageManager.StorageManagerObjectIntereface;
import StorageManager.TableSchema;

public class Record implements java.io.Serializable, StorageManagerObjectIntereface {
    private List<Object> values;

    public Record(List<Object> values) {
        this.values = values;
    }

    public List<Object> getValues() {
        return values;
    }

    public void setValues(List<Object> values) {
        this.values = values;
    }


    public int comapreTo(Record other, int primaryKeyIndex) {
        Object thisKey = this.values.get(primaryKeyIndex);
        Object otherKey = other.values.get(primaryKeyIndex);

        if (thisKey instanceof String) {
            return ((String) thisKey).compareTo((String) otherKey);
        } else if (thisKey instanceof Integer) {
            return Integer.compare((Integer) thisKey, (Integer) otherKey);
        } else if (thisKey instanceof Boolean) {
            boolean thisBool = (Boolean) thisKey;
            boolean otherBool = (Boolean) otherKey;
            return Boolean.compare(otherBool, thisBool);
        } else if (thisKey instanceof Double) {
            return Double.compare((Double) thisKey, (Double) otherKey);
        } else {
            throw new IllegalArgumentException("Unsupported primary key type");
        }
    }

    /*
     * Returns the size of this record in number of bytes
     *
     * @return  the number of bytes this record is
     */
    @Override
    public int computeSize() {
        int size = 0;
        for (Object value: this.values) {
            if (value instanceof Integer) {
                size += Integer.BYTES;
            } else if (value instanceof String) {
                size += ((String) value) == "null" ? 0 : ((String) value).length();
            } else if (value instanceof Boolean) {
                size += 1;
            } else if (value instanceof Double) {
                size += Double.BYTES;
            }
        }
        return size;
    }

    @Override
    public void writeToHardware(RandomAccessFile tableAccessFile) throws IOException {
        for (Object value : this.values) {
            if (value instanceof Integer) {
                tableAccessFile.writeInt((Integer) value);
            } else if (value instanceof String) {
                tableAccessFile.writeUTF((String) value);
            } else if (value instanceof Double) {
                tableAccessFile.writeDouble((Double) value);
            } else if (value instanceof Boolean) {
                tableAccessFile.writeBoolean((Boolean) value);
            }
        }
    }

    @Override
    public void readFromHardware(RandomAccessFile tableAccessFile, TableSchema tableSchema) throws IOException {
        for (AttributeSchema attributeSchema : tableSchema.getAttributes()) {
            if (attributeSchema.getDataType().equalsIgnoreCase("integer")) {
                int value = tableAccessFile.readInt();
                this.values.add(value);
            } else if (attributeSchema.getDataType().equalsIgnoreCase("double")) {
                double value = tableAccessFile.readInt();
                this.values.add(value);
            } else if (attributeSchema.getDataType().equalsIgnoreCase("boolean")) {
                boolean value = tableAccessFile.readBoolean();
                this.values.add(value);
            } else if (attributeSchema.getDataType().toLowerCase().contains("char") || attributeSchema.getDataType().toLowerCase().contains("varchar")) {
                String value = tableAccessFile.readUTF();
                this.values.add(value);
            }
        }
    }
}
