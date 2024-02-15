package StorageManager.Objects;

import java.util.List;

public class Record implements java.io.Serializable {
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

    /*
     * Returns the size of this record in number of bytes
     *
     * @return  the number of bytes this record is
     */
    public int computeSize() {
        int size = 0;
        for (Object value: this.values) {
            if (value instanceof Integer) {
                size += Integer.BYTES;
            } else if (value instanceof String) {
                size += ((String) value).length();
            } else if (value instanceof Boolean) {
                size += 1;
            } else if (value instanceof Double) {
                size += Double.BYTES;
            }
        }
        return size;
    }
}
