package StorageManager.BPlusTree;

import Parser.Type;
import StorageManager.TableSchema;
import StorageManager.Objects.AttributeSchema;
import StorageManager.Objects.MessagePrinter;
import StorageManager.Objects.MessagePrinter.MessageType;
import StorageManager.Objects.Utility.Pair;

import java.io.RandomAccessFile;
import java.text.AttributedCharacterIterator.Attribute;
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

    public Pair<Integer, Integer> removePointer(int index) {
        if (index < 0) {
            index = this.pointers.size() - (index+1);
        }
        this.setChanged();
        return this.pointers.remove(index);
    }

    public ArrayList<Object> getSK() {
        return this.searchKeys;
    }

    public void setSK(List<Object> newSK) {
        this.searchKeys.clear();
        this.searchKeys.addAll(newSK);
        this.setChanged();
    }

    public Object deleteSK(int index) {
        if (index < 0) {
            index = this.searchKeys.size() - (index+1);
        }
        this.setChanged();
        return this.searchKeys.remove(index);
    }

    /**
     * Replaces a searchKey in the node and returns the replaced searchKey
     * @param newKey        The new searchKey to replace
     * @param pageNum       The pageNum of the pointer that we are using to determine which SK to replace
     * @param less          Whether or not the intended searchkey is less than or greater than the inputted pageNum
     *                          ex. P1 SK1 P2 SK2 P3
     *                              Say we know P2, and want to replace SK2, then we pass in the pageNum of P2
     *                              and set less to False, to replace SK2, if we set less to True, then it replaces SK1
     *
     * @return              The searchKey that was replaced
     * @throws Exception
     */
    public Object replaceSearchKey(Object newKey, int pageNum, boolean less) throws Exception {
        int replaceIndex = -1;
        for (int i = 0; i < this.pointers.size(); i++) {
            if (this.pointers.get(i).first == pageNum) {
                replaceIndex = i;
            }
        }

        if (replaceIndex == -1) {
            MessagePrinter.printMessage(MessageType.ERROR, "This should not happen: RemoveSearchKeyLeafNode");
            return null;
        }

        if (less) {
            // idea: we pass in the pageNum of the node that was modified/deleted from
            // if we merge left, we are merging with a node that is less than us, meaning
            // the corresponding search key is at a lower index
            replaceIndex--;
        }

        Object replaced = this.searchKeys.set(replaceIndex, newKey);
        this.setChanged();
        return replaced;
    }

    public void addSearchKey(Object val, int index) {
        if (index < 0) {
            index = this.searchKeys.size() - (index+1);
            // if index == -1, meaning should insert at end of array
            // index should then == array.size()
        }
        this.searchKeys.add(index, val);
        this.setChanged();
    }

    public void addPointer(Pair<Integer, Integer> pointer, int index) {
        if (index < 0) {
            index = this.pointers.size() - (index+1);
            // if index == -1, meaning should insert at end of array
            // index should then == array.size()
        }
        this.pointers.add(index, pointer);
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
            return null;
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
    public boolean underfull() {
        double res = this.n-1 / 2;
        int min = (int)Math.ceil(res);
        if (min <= this.searchKeys.size()) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean overfull() {
        int max = this.n-1;
        if (this.searchKeys.size() <= max) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean willOverfull(int count) {
        int max = this.n-1;
        if (this.searchKeys.size()+count <= max) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean willUnderfull() {
        double res = this.n-1 / 2;
        int min = (int)Math.ceil(res);
        if (min <= this.searchKeys.size()-1) {
            return true;
        } else {
            return false;
        }
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

    public Object getSearchKey(int pageNum, boolean less) throws Exception {
        int getIndex = -1;
        for (int i = 0; i < this.pointers.size(); i++) {
            if (this.pointers.get(i).first == pageNum) {
                getIndex = i;
            }
        }

        if (getIndex == -1) {
            MessagePrinter.printMessage(MessageType.ERROR, "This should not happen: RemoveSearchKeyLeafNode");
            return null;
        }

        if (less) {
            return this.searchKeys.get(getIndex-1);
        } else {
            return this.searchKeys.get(getIndex);
        }
    }

    @Override
    public void clear() {
        this.pointers.clear();
        this.searchKeys.clear();
        this.setChanged();
    }

    @Override
    public void readFromHardware(RandomAccessFile tableAccessFile, TableSchema tableSchema) throws Exception {
        int numSearchKeys = tableAccessFile.readInt();

        for (int i=0; i < numSearchKeys; ++i) {
            AttributeSchema attributeSchema = tableSchema.getAttributes().get(tableSchema.getPrimaryIndex());

            if (attributeSchema.getDataType().equalsIgnoreCase("integer")) {
                int value = tableAccessFile.readInt();
                this.searchKeys.add(value)
            } else if (attributeSchema.getDataType().equalsIgnoreCase("double")) {
                double value = tableAccessFile.readDouble();
                this.searchKeys.add(value);
            } else if (attributeSchema.getDataType().equalsIgnoreCase("boolean")) {
                boolean value = tableAccessFile.readBoolean();
                this.searchKeys.add(value);
            } else if (attributeSchema.getDataType().contains("char") || attributeSchema.getDataType().contains("varchar")) {
                String value = tableAccessFile.readUTF();
                this.searchKeys.add(value);
            }
        }


        for (int i=0; i < numSearchKeys + 1; ++i) {
            Pair<Integer, Integer> pair = new Pair<Integer,Integer>(null, null);
            pair.first = tableAccessFile.readInt();
            pair.second = tableAccessFile.readInt();
            this.pointers.add(pair);
        }
    }

    @Override
    public void writeToHardware(RandomAccessFile tableAccessFile) throws Exception {
        // write boolean indicator
        tableAccessFile.writeBoolean(this.type);
        tableAccessFile.writeInt(pageNumber);
        tableAccessFile.writeInt(this.parent);
        tableAccessFile.writeInt(this.searchKeys.size());


        for (Object searchKey: this.searchKeys) {
            if (searchKey instanceof Integer) {
                tableAccessFile.writeInt((Integer) searchKey);
            } else if (searchKey instanceof String) {
                tableAccessFile.writeUTF((String) searchKey);
            } else if (searchKey instanceof Double) {
                tableAccessFile.writeDouble((Double) searchKey);
            } else if (searchKey instanceof Boolean) {
                tableAccessFile.writeBoolean((Boolean) searchKey);
            } else {
                MessagePrinter.printMessage(MessageType.ERROR, "Not a valid data type for a search key");
            }
        }

        for (Pair<Integer, Integer> pointer : this.pointers) {
            tableAccessFile.writeInt(pointer.first);
            tableAccessFile.writeInt(pointer.second);
        }
    }

}

