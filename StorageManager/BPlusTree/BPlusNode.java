package StorageManager.BPlusTree;

import Parser.Type;
import StorageManager.Objects.BufferPage;
import StorageManager.Objects.Utility.Pair;

public abstract class BPlusNode extends BufferPage{
    protected int n;
    protected int parent; // this wil be the pageNumber of the parent

    public BPlusNode(int tableNumber, int pageNumber, int n, int parent) {
        super(tableNumber, pageNumber);
        this.n = n;
        this.parent = parent;
    }

    /**
     * Will search the node to find and return the location of a specific search key
     * @param value     The search key to find
     * @param type      The data type of the search key
     * @return          A Pair of two integer in the format <page_num, index>
     * @throws Exception
     */
    public abstract Pair<Integer, Integer> search(Object value, Type type) throws Exception;

    /**
     * Will search the node to find and return the location of where a new search
     * key should be inserted.
     * 
     * Will then insert the search key value into that location in the leaf node
     * If we find the search key already present in the leaf node, reject the insert
     * And raise an error.
     * 
     * If rules are now in violation, split
     * @param value     The search key to find
     * @param type      The data type of the search key
     * @return          A Pair of two integer in the format <page_num, index>
     * @throws Exception
     */
    public abstract Pair<Integer, Integer> insert(Object value, Type type) throws Exception;

    /**
     * Will search the node to find and return the location of a specific search key
     * 
     * Will then delete the search key from the leaf node
     * 
     * If rules are now in violation, borrow/merge
     * @param value     The search key to find
     * @param type      The data type of the search key
     * @return          A Pair of two integer in the format <page_num, index>
     * @throws Exception
     */
    public abstract Pair<Integer, Integer> delete(Object value, Type type) throws Exception;

    /**
     * Will search the node to find and return the location of a specific search key
     * 
     * Will perform a delete, and then an insert. Will then return the location of both instances
     * If during the insert, an error occurs, revert back and reinsert the old search key
     * 
     * @param value     The search key to find
     * @param type      The data type of the search key
     * @return          An array of two Pairs of two integer in the format <page_num, index>.
     *                      The first pair will hold the pointer to the location in which the 
     *                      record should be deleted from, and the second pair holds the pointer
     *                      of the location in which the new record should be inserted into
     * @throws Exception
     */
    public abstract Pair<Integer, Integer>[] update(Object value, Type type) throws Exception;
}
