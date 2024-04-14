package StorageManager.BPlusTree;

import Parser.Token;
import Parser.Type;
import StorageManager.Objects.Catalog;
import StorageManager.Objects.MessagePrinter;
import StorageManager.Objects.Utility.Pair;

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
    public Pair<Integer, Integer> search(Object value, Type type) throws Exception {

        for(int i=0; i<buckets.size(); i++){
            try {
                Object pk = buckets.get(i).getPrimaryKey();
                boolean found = false;
                switch (type) {
                    case Type.INTEGER:
                        int incoming = (Integer) value;
                        int current = (Integer) pk;
                        if (incoming == current) {
                            found = true;
                        }
                        break;
                    case Type.DOUBLE:
                        double incomingD = (Double) value;
                        double currentD = (Double) pk;
                        if (incomingD == currentD) {
                            found = true;
                        }
                        break;
                    case Type.BOOLEAN:
                        boolean incomingB = (Boolean) value;
                        boolean currentB = (Boolean) pk;
                        if (incomingB == currentB) {
                            found = true;
                        }
                        break;
                    case Type.STRING:
                        String incomingS = (String) value;
                        String currentS = (String) pk;
                        if (incomingS == currentS) {
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


}