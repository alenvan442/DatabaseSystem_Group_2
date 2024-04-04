package StorageManager.BPlusTree;

public class Bucket {

    private int pageNumber;

    private int index; //TODO: Double Check datatype.

    public Bucket(int pageNumber, int index){
        this.pageNumber=pageNumber;
        this.index=index;
    }

    public int getIndex() {
        return index;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public double calculateBucketSize(){
        int floor; //TODO: double check
        int pageSize; //TODO: Find page size

    return floor (pageSize / (Integer.BYTES + Integer.BYTES) );
    }
}
