package StorageManager.Objects;

import java.util.List;

public class Page {
    private int numRecords;
    private List<Record> records;

    public Page(int numRecords) {
        this.numRecords = numRecords;
    }
}
