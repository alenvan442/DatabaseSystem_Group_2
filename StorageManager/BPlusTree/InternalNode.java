package StorageManager.BPlusTree;


import Parser.Token;
import java.util.ArrayList;

public class InternalNode implements BPlusNode {
    private ArrayList<BPlusNode> children;
    private int n;
    private Token value; // the search value of the node, mayormaynot be needed.

    /**
     * type size times pageSize = n
     * @param PKType - The type (integer, double, char, varchar, bool?) of the primary key, implying data size
     * @param pageSize - Size of the page
     */
    public InternalNode(Token PKType, int pageSize){

    }

    /**
     * comparison function which searches for the value's place among its child nodes.
     * @param value
     * @returns the found node, for an insert or without a match, this is the node BEFORE the value's place.
     */
    public BPlusNode search(Token value){
        return null;
    }

}

