package StorageManager.BPlusTree;

import Parser.Token;
import StorageManager.Objects.Catalog;

import static java.lang.Math.floor;

public class Bucket {

    private int pageNumber;

    private int index;

    private Object primaryKey;

    /**
     * Soecial type of page
     * @param pageNumber - corresponding page #
     * @param index - index on the page for the bucket.
     */
    public Bucket(int pageNumber, int index, Token pk){
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

    /**
     * calculates the size of the bucket
     * @return - size of the bucket
     */
    public double calculateBucketSize(){
        Catalog catalog = Catalog.getCatalog();
        //int floor;
        int pageSize;
        pageSize = catalog.getPageSize();

    return floor (pageSize / (Integer.BYTES + Integer.BYTES) );
    }

}
