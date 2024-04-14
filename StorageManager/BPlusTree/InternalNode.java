package StorageManager.BPlusTree;

import Parser.Type;
import StorageManager.Objects.Utility.Pair;

import java.io.RandomAccessFile;
import java.util.ArrayList;

public class InternalNode extends BPlusNode {
    private ArrayList<BPlusNode> children;
    private Object value; // the search value of the node, mayormaynot be needed.

    /**
     * type size times pageSize = n
     * @param value - search value
     * @param n - Size
     */
    public InternalNode(Object value, int tableNumber, int pageNumber, int n){
        super(tableNumber, pageNumber, n);
        this.value = value;
        children = new ArrayList<BPlusNode>();
    }

    /**
     * comparison function which searches for the value's place among its child nodes.
     * @param value
     * @returns the found node, for an insert or without a match, this is the node BEFORE the value's place.
     */
    public Pair<Integer, Integer> search(Object value, Type type) {
        return null;
    }

    @Override
    public Pair<Integer, Integer> insert(Object value, Type type) throws Exception {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'insert'");
    }

    @Override
    public Pair<Integer, Integer> delete(Object value, Type type) throws Exception {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'delete'");
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

