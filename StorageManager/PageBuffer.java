package StorageManager;

import java.util.List;

import StorageManager.Objects.Page;

public class PageBuffer {
    private List<Page> buffer;
    private int bufferSize;

    public PageBuffer(int bufferSize) {
        this.bufferSize = bufferSize;
    }
}
