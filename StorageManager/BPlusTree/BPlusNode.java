package StorageManager.BPlusTree;


import Parser.Type;
import StorageManager.Objects.BufferPage;
import StorageManager.Objects.MessagePrinter;
import StorageManager.Objects.Utility.Pair;

public abstract class BPlusNode extends BufferPage {
    protected int n;
    protected boolean type; // 0/false for internal, 1/true for leaf
    protected int parent; // this wil be the pageNumber of the parent

    public BPlusNode(int tableNumber, int pageNumber, int n, int parent, boolean type) {
        super(tableNumber, pageNumber);
        this.n = n;
        this.parent = parent;
        this.type = type;
    }

    public int getParent() {
        return this.parent;
    }

    public void setParent(int parent) {
        this.parent = parent;
        this.setChanged();
    }

    /**
     * Compares to see if an incoming key is less than the currently viewing key
     * @param thisKey       The current key to compare with
     * @param incomingKey   The incoming key to compare/look for
     * @param type          The data type of the search keys
     * @return:             0: The two are equal
     *                      <0: this < other
     *                      >0: this > other
     * @throws Exception
     */
    protected int compareKey(Object thisKey, Object otherKey, Type type) throws Exception {
        switch (type) {
            case INTEGER:
                return Integer.compare((Integer) thisKey, (Integer) otherKey);
            case DOUBLE:
                return Double.compare((Double) thisKey, (Double) otherKey);
            case STRING:
                String searchString = (String) thisKey;
                String inString = (String) otherKey;
                return searchString.compareTo(inString);
            case BOOLEAN: //this seems really silly but may as well, defining false < true
                boolean thisBool = (Boolean) thisKey;
                boolean otherBool = (Boolean) otherKey;
                return Boolean.compare(thisBool, otherBool);
            default:
                MessagePrinter.printMessage(MessagePrinter.MessageType.ERROR, "Invalid type for B+ tree index!");
                throw new IllegalArgumentException("Unsupported datatype");
        }
    }

    /**
     * Removes a search key at a given index, will also remove the corresponding pointer as well
     * @param int       Index to remove at
     */
    public abstract void removeSearchKey(int index);

    /**
     * Removes a search key at a given index, will also remove the corresponding pointer as well
     * the passed in pageNum will be used to determine what the index to remove is
     * @param pageNum       Helps determine where the search key to remove is
     * @param less          Determines if the search key to remove is before (True)
     *                          or after (False) the passed in pageNum pointer
     * @throws Exception
     */
    public abstract void removeSearchKey(int pageNum, boolean less) throws Exception;

    /**
     * Determines if this current node is underfull
     * @return      True if it is
     */
    public abstract boolean underfull();

    /**
     * Determines if this current node is overfull
     * @return      True if it is
     */
    public abstract boolean overfull();

    /**
     * Given a number, determine whether or not if we added that many
     * more search keys, would we be overfull.
     * @return      True if it will cause an overful, otherwise false
     */
    public abstract boolean willOverfull(int count);

    /**
     * If a single searchKey is removed, will this node be underfull?
     * @return      True if it will be underfull, False otherwise
     */
    public abstract boolean willUnderfull();

    /**
     * @pageNum     decrement any pointer that points to another BPlusNode, 
     *              and whose pageNumber is greater than the passed in pageNum
     */
    public abstract void decrementNodePointerPage(int pageNum);

    /**
     * Clears the node. Used in a merge
     * Removes all search keys and pointers from this node
     */
    public abstract void clear();

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
}
