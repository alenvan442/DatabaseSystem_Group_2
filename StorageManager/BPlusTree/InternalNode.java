package StorageManager.BPlusTree;

import Parser.Type;
import StorageManager.Objects.MessagePrinter;
import StorageManager.Objects.MessagePrinter.MessageType;
import StorageManager.Objects.Utility.Pair;

import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

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
        super(tableNumber, pageNumber, n, parent, false);
        this.searchKeys = new ArrayList<>();
        this.pointers = new ArrayList<>();
    }

    public ArrayList<Pair<Integer, Integer>> getPointers() {
        return this.pointers;
    }

    public void setPointers(List<Pair<Integer, Integer>> newPointers) {
        this.pointers.clear();
        this.pointers.addAll(newPointers);
        this.setChanged();
    }

    public ArrayList<Object> getSK() {
        return this.searchKeys;
    }

    public void setSK(List<Object> newSK) {
        this.searchKeys.clear();
        this.searchKeys.addAll(newSK);
        this.setChanged();
    }

    /**
     * 
     * @param newKey
     * @param pageNum
     * @param type
     * @param less
     * @throws Exception
     */
    public void replaceSearchKey(Object newKey, int pageNum, boolean less) throws Exception {
        int replaceIndex = -1;
        for (int i = 0; i < this.pointers.size(); i++) {
            if (this.pointers.get(i).first == pageNum) {
                replaceIndex = i;
            }
        }

        if (replaceIndex == -1) {
            MessagePrinter.printMessage(MessageType.ERROR, "This should not happen: RemoveSearchKeyLeafNode");
            return;
        }

        if (less) {
            // idea: we pass in the pageNum of the node that was modified/deleted from
            // if we merge left, we are merging with a node that is less than us, meaning
            // the corresponding search key is at a lower index
            replaceIndex--;
        }

        this.searchKeys.set(replaceIndex, newKey);
        this.setChanged();
    }

    /**
     * Insert a new search key. This is used during the split algorithm for overfull conflicts
     * @param val       The search key to append
     * @param pointer   The pointer for the new node that was created.
     *                  this node will consist of the second half of the search keys
     */
    public void addSearchKey(Object val, Pair<Integer, Integer> pointer) {
        // TODO ensure the node is sorted and insert the pointer at the +1 index.
        // we insert the pointer into the +1 index since the same index will still hold the pointer to the old node
        int i = 0;
        this.searchKeys.add(i, val);
        this.pointers.add(i+1, pointer);
        this.setChanged();
    }

    /**
     * Retrieves the two neighbors
     * @param pageNum   The page we are finding it's neighbors
     * @return          Two integers in the format [leftNeighbor, rightNeighbor]
     *                      where the integers are the corresponding pageNumbers
     */
    public Pair<Integer, Integer> getNeighbors(int pageNum) {
        int leftPageNum = 0;
        int currentIndex = 0;
        int rightPageNum = 0;
        for (int i = 0; i < pointers.size(); i++) {
            if (this.pointers.get(i).first == pageNum) {
                currentIndex = i;
                break;
            }
        }
        if (currentIndex == 0) {
            leftPageNum = -2; 
            rightPageNum = this.pointers.get(currentIndex + 1).first;
        } else if (currentIndex == this.pointers.size()-1) {
            leftPageNum = this.pointers.get(currentIndex - 1).first;
            rightPageNum = -2;
        } else {
            leftPageNum = this.pointers.get(currentIndex - 1).first;
            rightPageNum = this.pointers.get(currentIndex + 1).first;
        }

        return new Pair<Integer,Integer>(leftPageNum, rightPageNum);
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
            if (this.compareKey(searchVal, value, type) > 0) {
                return pointers.get(i);
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
        this.searchKeys.remove(index);
        this.pointers.remove(index);
        this.setChanged();
    }

    @Override
    public void removeSearchKey(int pageNum, boolean less) throws Exception {
        int replaceIndex = -1;
        for (int i = 0; i < this.pointers.size(); i++) {
            if (this.pointers.get(i).first == pageNum) {
                replaceIndex = i;
            }
        }

        if (replaceIndex == -1) {
            MessagePrinter.printMessage(MessageType.ERROR, "This should not happen: RemoveSearchKeyLeafNode");
            return;
        }

        if (less) {
            // idea: we pass in the pageNum of the node that was modified/deleted from
            // if we merge left, we are merging with a node that is less than us, meaning
            // the corresponding search key is at a lower index

            // for merge lefts
            this.pointers.remove(replaceIndex);
            this.searchKeys.remove(replaceIndex-1);
        } else {
            // for merge rights
            this.pointers.remove(replaceIndex-1);
            this.searchKeys.remove(replaceIndex);
        }
        
        this.setChanged();
    }

    @Override
    public void clear() {
        this.pointers.clear();
        this.searchKeys.clear();
        this.setChanged();
    }

}

