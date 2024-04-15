package StorageManager.BPlusTree;

import Parser.Type;
import StorageManager.Objects.MessagePrinter;
import StorageManager.Objects.Utility.Pair;

import java.io.RandomAccessFile;
import java.util.ArrayList;

public class InternalNode extends BPlusNode {
    private ArrayList<Object> searchKeys;
    private ArrayList<Pair<Integer, Integer>> pointers; // there should be searchKeys+1 pointers

    /**
     * type size times pageSize = n
     * @param tableNumber   Table number this node is associated with
     * @param pageNumber    pageNumber the index of this page on hardware
     * @param n             size
     * @param parent        Index of the parent node
     */
    public InternalNode(int tableNumber, int pageNumber, int n, int parent){
        super(tableNumber, pageNumber, n, parent);
        this.searchKeys = new ArrayList<>();
        this.pointers = new ArrayList<>();
    }

    public void addSearchKey(Object val) {
        // TODO ensure the node is sorted
        this.searchKeys.add(val);
    }

    public void setParent(int parent) {
        this.parent = parent;
    }

    /**
     * comparison function which searches for the value's place among its child nodes.
     * @param value
     * @returns the found node, for an insert or without a match, this is the node BEFORE the value's place.
     */
    public Pair<Integer, Integer> search(Object value, Type type) throws Exception {
        if (pointers.size() == 0){
            //error case?
            MessagePrinter.printMessage(MessagePrinter.MessageType.ERROR, "Internal node has no children?");
        }
        for (int i = 0; i < searchKeys.size(); i++) {
            Object searchVal = searchKeys.get(i);
            switch (type) {
                case INTEGER:
                    if ((int) searchVal > (int) value) { //I LOVE TYPECASTING RAHHHH
                        return pointers.get(i);
                    }
                case DOUBLE:
                    if ((double) searchVal > (double) value) { //I LOVE TYPECASTING RAHHHH
                        return pointers.get(i);
                    }
                case STRING:
                    String searchString = (String) searchVal;
                    String inString = (String) value;
                    if (searchString.compareTo(inString) > 0) { //I LOVE TYPECASTING RAHHHH
                        return pointers.get(i);
                    }
                case BOOLEAN: //this seems really silly but may as well, defining false < true
                    if ((boolean) searchVal){ //if true, the value in can go before regardless of if it's true or false.
                        return pointers.get(i);
                    }
                default:
                    MessagePrinter.printMessage(MessagePrinter.MessageType.ERROR, "Invalid type for B+ tree index!");
                    return new Pair<>(-1, -1);
            }
        }
        return pointers.get(pointers.size());

    }

    @Override
    public Pair<Integer, Integer> insert(Object value, Type type) throws Exception {
        return this.search(value, type);
    }

    @Override
    public Pair<Integer, Integer> delete(Object value, Type type) throws Exception {
        return this.search(value, type);
    }

    @Override
    public Pair<Integer, Integer>[] update(Object value, Type type) throws Exception {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'update'");
    }

    @Override
    public void writeToHardware(RandomAccessFile tableAccessFile) throws Exception {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'writeToHardware'");
    }

}

