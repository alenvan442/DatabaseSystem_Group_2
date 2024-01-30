package StorageManager.Objects;

import java.util.List;

public class Table {
    private int numPages;
    private List<Page> pages;

    public Table(int numPages, List<Page> pages) {
        this.numPages = numPages;
        this.pages = pages;
    }

    public int getNumPages() {
        return numPages;
    }

    public void setNumPages(int numPages) {
        this.numPages = numPages;
    }

    public List<Page> getPages() {
        return pages;
    }

    public void setPages(List<Page> pages) {
        this.pages = pages;
    }
}
