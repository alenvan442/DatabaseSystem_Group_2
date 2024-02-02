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
    public int byteSize() {
        // TODO
        return 0;
    }
}
