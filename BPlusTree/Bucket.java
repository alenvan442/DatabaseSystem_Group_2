package BPlusTree;

public class Bucket {

    private int pageNumber;
    private int index;

    private Object primaryKey;

    /**
     * Soecial type of page
     * @param pageNumber - corresponding page #
     * @param index - index on the page for the bucket.
     */
    public Bucket(int pageNumber, int index, Object pk){
        this.pageNumber=pageNumber;
        this.index=index;
        primaryKey = pk;
    }

    public Object getPrimaryKey() {
        return primaryKey;
    }

    public int getIndex() {
        return index;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public void setPointer(int pageNum, int index) {
        this.pageNumber = pageNum;
        this.index = index;
    }

}
