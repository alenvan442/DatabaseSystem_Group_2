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
}
