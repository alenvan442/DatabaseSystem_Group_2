package StorageManager;

import java.util.List;
import StorageManager.Objects.Page;

public class PageBuffer {
    private List<Page> buffer;

    public PageBuffer() {
    }

    public List<Page> getBuffer() {
        return buffer;
    }

    public void setBuffer(List<Page> buffer) {
        this.buffer = buffer;
    }
}
