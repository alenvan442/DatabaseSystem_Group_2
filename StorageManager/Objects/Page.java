package StorageManager.Objects;

import java.util.List;

public class Page implements java.io.Serializable {
    private int numRecords;
    private List<Record> records;
    private boolean changed;

    public Page(int numRecords) {
        this.numRecords = numRecords;
    }

    public int getNumRecords() {
        return numRecords;
    }

    public void setNumRecords(int numRecords) {
        this.numRecords = numRecords;
    }

    public List<Record> getRecords() {
        return records;
    }

    public void setRecords(List<Record> records) {
        this.records = records;
    }

    public boolean isChanged() {
        return this.changed;
    }

}
