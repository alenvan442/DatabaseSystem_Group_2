package StorageManager.Objects;

import java.util.List;

public class Table {
    private int numPages;
    private List<Page> pages;

    public Table(int numPages, List<Page> pages) {
        this.numPages = numPages;
        this.pages = pages;
    }

    public void addPage(Page page) {

    }

    public int getNumPage() {
        return this.numPages;
    }
}
