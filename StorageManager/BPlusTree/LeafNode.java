package StorageManager.BPlusTree;

public class LeafNode implements Node{
    Bucket bucket;

    public LeafNode(int pageNumber, int index){
        bucket= new Bucket(pageNumber, index);
    }
    
}
