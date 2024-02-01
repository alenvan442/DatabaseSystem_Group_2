package StorageManager.Objects;

import java.util.Comparator;
import java.util.List;

public class Page implements java.io.Serializable, Comparator<Page> {
    private int numRecords;
    private List<Record> records;
    private int priority;
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

    public void setPriority(int pritority) {
        this.priority = pritority;
    }

    @Override
    public int compare(Page o1, Page o2) {
        return o1.priority - o2.priority;
    }




}
