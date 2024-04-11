package StorageManager.BPlusTree;


import Parser.Token;
import java.util.ArrayList;

public class InternalNode implements BPlusNode {
    private ArrayList<BPlusNode> children;
    private int n;
    private Object value; // the search value of the node, mayormaynot be needed.

    /**
     * type size times pageSize = n
     * @param value - search value
     * @param n - Size
     */
    public InternalNode(int n, Object value){
        this.n = n;
        this.value = value;
        children = new ArrayList<BPlusNode>();
    }

    /**
     * comparison function which searches for the value's place among its child nodes.
     * @param value
     * @returns the found node, for an insert or without a match, this is the node BEFORE the value's place.
     */
    public BPlusNode search(Object value){
        return null;
    }

}

