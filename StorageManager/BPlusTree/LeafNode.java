package StorageManager.BPlusTree;

import Parser.Token;
import StorageManager.Objects.Catalog;

import java.util.ArrayList;

public class LeafNode implements BPlusNode{
    ArrayList<Bucket> buckets;
    LeafNode nextLeaf;


    int tableNumber;
    public LeafNode(int tableNumber) {
        buckets = new ArrayList<>();
        this.tableNumber = tableNumber;

    }

    public void addBucket(Bucket b){
        buckets.add(b);
    }

    public void assignNextLeaf(LeafNode ln){
        nextLeaf=ln;
    }


    @Override
    public Bucket search(Token value) {
        Catalog cataog = Catalog.getCatalog();
        for(int i =0; i <buckets.size(); i++){
            if(value.getVal().compareTo(buckets.get(i).getPrimaryKey().getVal())<=0){
                //BELONGS HERE
                return buckets.get(i);
            }
        }
        return buckets.get(-1);

    }
}