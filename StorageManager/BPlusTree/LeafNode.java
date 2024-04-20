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
                switch (type) {
                    case INTEGER:
                        int incoming = (Integer) value;
                        int current = (Integer) pk;
                        if (incoming == current) {
                            found = true;
                        }
                        break;
                    case DOUBLE:
                        double incomingD = (Double) value;
                        double currentD = (Double) pk;
                        if (incomingD == currentD) {
                            found = true;
                        }
                        break;
                    case BOOLEAN:
                        boolean incomingB = (Boolean) value;
                        boolean currentB = (Boolean) pk;
                        if (incomingB == currentB) {
                            found = true;
                        }
                        break;
                    case STRING:
                        String incomingS = (String) value;
                        String currentS = (String) pk;
                        if (incomingS.equals(currentS)) {
                            found = true;
                        }
                        break;
                    default:
                        MessagePrinter.printMessage(MessagePrinter.MessageType.ERROR, "Error in data type");
                        break;
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
                switch (type) {
                    case INTEGER:
                        int incoming = (Integer) value;
                        int current = (Integer) pk;
                        if (incoming < current) {
                            found = true;
                        }else if(incoming == current){
                            throw new Exception("PrimaryKey is already in db");
                        }
                        break;
                    case DOUBLE:
                        double incomingD = (Double) value;
                        double currentD = (Double) pk;
                        if (incomingD < currentD) {
                            found = true;
                        }else if(incomingD == currentD){
                            throw new Exception("PrimaryKey is already in db");
                        }
                        break;
                    case BOOLEAN:
                        boolean incomingB = (Boolean) value;
                        boolean currentB = (Boolean) pk;
                        if(incomingB==currentB){
                            throw new Exception("PrimaryKey is already in db");
                        }
                        //TODO: fix the comparison

                        /*
                        if (incomingB.compareTo(currentB)) {
                            found = true;
                        }else if(incomingB == currentB){
                            throw new Exception("PrimaryKey is already in db");
                        }
                         */
                        break;
                    case STRING:
                        String incomingS = (String) value;
                        String currentS = (String) pk;
                        //TODO: String Comparisons
                        if (incomingS.equals(currentS)) {
                            found = true;
                        }else if(incomingS == currentS){
                            throw new Exception("PrimaryKey is already in db");
                        }
                        break;
                    default:
                        MessagePrinter.printMessage(MessagePrinter.MessageType.ERROR, "Error in data type");
                        break;
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


}