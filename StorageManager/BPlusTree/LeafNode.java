package StorageManager.BPlusTree;

import Parser.Token;
import Parser.Type;
import StorageManager.Objects.Catalog;
import StorageManager.Objects.MessagePrinter;
import StorageManager.Objects.MessagePrinter.MessageType;
import StorageManager.Objects.Utility.Pair;

import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

public class LeafNode extends BPlusNode{
    ArrayList<Bucket> buckets;
    Pair<Integer, Integer> nextLeaf;

    int tableNumber;
    public LeafNode(int tableNumber, int pageNumber, int n, int parent) {
        super(tableNumber, pageNumber, n, parent, true);
        buckets = new ArrayList<>();
    }

    public ArrayList<Bucket> getSK() {
        return this.buckets;
    }

    public void setSK(List<Bucket> newbuckets) {
        this.buckets.clear();
        this.buckets.addAll(newbuckets);
        this.setChanged();
    }

    public int addBucket(Bucket b){
        buckets.add(b);
        this.setChanged();
        // TODO insert into the correct spot and return the idnex it was inserted into
        return 0;
    }

    public void assignNextLeaf(LeafNode ln){
        nextLeaf = new Pair<Integer,Integer>(ln.pageNumber, -1);
        this.setChanged();
    }

    public void assignNextLeaf(int ln){
        nextLeaf = new Pair<Integer,Integer>(ln, -1);
        this.setChanged();
    }

    public Pair<Integer, Integer> getNextLeaf() {
        return this.nextLeaf;
    }


    @Override
    public Pair<Integer, Integer> search(Object value, Type type) throws Exception {
        for(int i = 0; i < buckets.size(); i++){
            try {
                Object pk = buckets.get(i).getPrimaryKey();
                boolean found = false;
                if (this.compareKey(pk, value, type) == 0) {
                    found = true;
                }

                if (found) {
                    //BELONGS HERE
                    Bucket obtained = buckets.get(i);
                    return new Pair<Integer, Integer>(obtained.getPageNumber(), obtained.getIndex());
                }
            }catch(Exception e){
                MessagePrinter.printMessage(MessagePrinter.MessageType.ERROR, "Error in data type");
            }
        }
        Bucket last = buckets.get(-1);
        return new Pair<Integer, Integer>(last.getPageNumber(), last.getIndex());
    }

    @Override
    public Pair<Integer, Integer> insert(Object value, Type type) throws Exception {
        // TODO insert the search key and the pointer
        // TODO return the new pointer ex. if incoming is less than something, then the pointer should be the same page, but one less index
        for(int i = 0; i < buckets.size(); i++){
            try {
                Object pk = buckets.get(i).getPrimaryKey();
                boolean found = false;
                int result = this.compareKey(pk, value, type);
                if (result == 0) {
                    throw new Exception("PrimaryKey is already in db");
                } else if (result < 0) {
                    found = true;
                }
             
                if (found) {
                    //BELONGS HERE
                    Bucket obtained = buckets.get(i);
                    return new Pair<Integer, Integer>(obtained.getPageNumber(), obtained.getIndex());
                }
            }catch(Exception e){
                MessagePrinter.printMessage(MessagePrinter.MessageType.ERROR, "Error in data type");
            }
        }
        Bucket last = buckets.get(-1);
        return new Pair<Integer, Integer>(last.getPageNumber(), last.getIndex());
    }

    @Override
    public Pair<Integer, Integer> delete(Object value, Type type) throws Exception {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'delete'");
    }

    @Override
    public void writeToHardware(RandomAccessFile tableAccessFile) throws Exception {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'writeToHardware'");
    }

    @Override
    public boolean underfull() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'underfull'");
    }

    @Override
    public boolean overfull() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'overfull'");
    }

    @Override
    public boolean willOverfull(int count) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'willOverfull'");
    }

    @Override
    public boolean willUnderfull() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'willUnderfull'");
    }

    @Override
    public void removeSearchKey(int index) {
        this.buckets.remove(index);
        this.setChanged();
    }

    @Override
    public void removeSearchKey(int pageNum, boolean less) throws Exception {
        int removeIndex = -1;
        for (int i = 0; i < this.buckets.size(); i++) {
            if (this.buckets.get(i).getPageNumber() == pageNum) {
                removeIndex = i;
            }
        }

        if (removeIndex == -1) {
            MessagePrinter.printMessage(MessageType.ERROR, "This should not happen: RemoveSearchKeyLeafNode");
            return;
        }

        // less variable is disregarded when dealing with leaf nodes
        this.buckets.remove(removeIndex);
        
        this.setChanged();
    }

    @Override
    public void clear() {
        this.buckets.clear();
        this.nextLeaf = null;
        this.setChanged();
    }


}