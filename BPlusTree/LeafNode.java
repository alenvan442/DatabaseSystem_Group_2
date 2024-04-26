package BPlusTree;

import Parser.Type;
import StorageManager.TableSchema;
import StorageManager.Objects.AttributeSchema;
import StorageManager.Objects.MessagePrinter;
import StorageManager.Objects.MessagePrinter.MessageType;
import StorageManager.Objects.Page;
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
        this.buckets = new ArrayList<>();
        this.nextLeaf = null;
    }

    /**
     * Get all buckets of this leaf node
     * @return a list of all of the buckets
     */
    public ArrayList<Bucket> getSK() {
        return this.buckets;
    }

    /**
     * set the buckets of this node to the incoming list
     * @param newbuckets    The list of buckets to set to
     */
    public void setSK(List<Bucket> newbuckets) {
        this.buckets.clear();
        this.buckets.addAll(newbuckets);
        this.setChanged();
    }

    /**
     * Add a bucket at a specific index
     * @param b         The bucket to add
     * @param index     Where to add the bucket
     */
    public void addBucket(Bucket b, int index){
        if (index < 0) {
            index = this.buckets.size() - (index + 1);
        }
        buckets.add(index, b);
        this.setChanged();
    }

    /**
     * Assign a new pointer that points to the next leaf node
     * @param ln    The next leafnode to point to
     */
    public void assignNextLeaf(LeafNode ln){
        nextLeaf = new Pair<Integer,Integer>(ln.pageNumber, -1);
        this.setChanged();
    }

    /**
     * Assign a new pointer that points to the next leaf node
     * @param ln    The pageNumber of the leafNode to point to
     */
    public void assignNextLeaf(int ln){
        nextLeaf = new Pair<Integer,Integer>(ln, -1);
        this.setChanged();
    }

    /**
     * Returns the pointer to the next leaf node
     * @return      A pointer <PageNumber, -1>
     */
    public Pair<Integer, Integer> getNextLeaf() {
        return this.nextLeaf;
    }

    /**
     * Decrements all pointers that consist of a page that is greater than the passed in value
     * @param page      The page number that was just deleted, so decrement any pages after this
     */
    public void decrementPointerPage(int page) {
        for (Bucket b : this.buckets) {
            if (b.getPageNumber() > page) {
                b.setPointer(b.getPageNumber()-1, b.getIndex());
            }
        }
        this.setChanged();
    }


    @Override
    public Pair<Integer, Integer> search(Object value, Type type) throws Exception {
        for(int i = 0; i < buckets.size(); i++){
            try {
                Object pk = buckets.get(i).getPrimaryKey();
                boolean found = false;
                if (this.compareKey(pk, value, type) == 0) {
                    found = true;
                }

                if (found) {
                    //BELONGS HERE
                    Bucket obtained = buckets.get(i);
                    return new Pair<Integer, Integer>(obtained.getPageNumber(), obtained.getIndex());
                }
            }catch(Exception e){
                MessagePrinter.printMessage(MessagePrinter.MessageType.ERROR, "Error in data type");
                return null;
            }
        }
        // no matching key was found
        MessagePrinter.printMessage(MessageType.ERROR, "No matching key of " + value.toString() + " was found");
        return null;
    }

    /**
     * Start somewhere on the page, loop through every bucket until we find the record to update
     * update the record, then increment to the next bucket and record, then match again.
     * Theoretically, they should be in order.
     * This is typically used after a DB page got split so we need to update the pointer
     * of all buckets that were in the pages that got split
     * @param startIndex    where to start on the passed in page
     * @param type          datatype of PK
     * @param page          the page to update
     * @param skIndex       index of the SK/PK
     * @return              the index where we leave off but have not yet updated
     * @throws Exception
     */
    public int replacePointerMultiple(int startIndex, Type type, Page page, int skIndex) throws Exception {
        int pageIndex = startIndex;
        for (int i = 0; i < this.buckets.size(); i++) {
            if (pageIndex >= page.getRecords().size()) {
                break;
            }

            Object sk = page.getRecords().get(pageIndex).getValues().get(skIndex);
            Bucket curr = this.buckets.get(i);
            int result = this.compareKey(sk, curr.getPrimaryKey(), type);
            if (result == 0) {
                // found match => update pointer
                curr.setPointer(page.getPageNumber(), pageIndex);
                this.setChanged();
                pageIndex++;
            } else if (result < 0) {
                MessagePrinter.printMessage(MessageType.ERROR, "B+ tree out of order or missing record: replacePointerMultiple");
                return -1;
            }
        }
        return pageIndex;
    }

    @Override
    public Pair<Integer, Integer> insert(Object value, Type type) throws Exception {
        for(int i = 0; i < buckets.size(); i++){
            try {
                Bucket curr = buckets.get(i);
                Object pk = curr.getPrimaryKey();
                int result = this.compareKey(value, pk, type);
                if (result == 0) {
                    throw new Exception("PrimaryKey is already in db");
                } else if (result < 0) {
                    // it goes before the current, or in otherwords
                    // it goes where the current is, pushing the current forward in index
                    Bucket _new = new Bucket(curr.getPageNumber(), curr.getIndex(), value);
                    this.buckets.add(i, _new);
                    this.setChanged();
                    return new Pair<Integer, Integer>(_new.getPageNumber(), _new.getIndex());
                }
            }catch(Exception e){
                MessagePrinter.printMessage(MessagePrinter.MessageType.ERROR, "Error in data type");
                return null;
            }
        }
        // if we reach this, then we need to insert at end of leafnode
        Bucket _new = new Bucket(this.buckets.get(this.buckets.size()-1).getPageNumber(), this.buckets.get(this.buckets.size()-1).getIndex()+1, value);
        this.buckets.add(_new);
        this.setChanged();
        return new Pair<Integer, Integer>(_new.getPageNumber(), _new.getIndex());
    }

    @Override
    public Pair<Integer, Integer> delete(Object value, Type type) throws Exception {
        for(int i = 0; i < buckets.size(); i++){
            try {
                Bucket curr = buckets.get(i);
                Object pk = curr.getPrimaryKey();
                boolean found = false;
                int result = this.compareKey(pk, value, type);
                if (result == 0) {
                    found = true;
                }

                if (found) {
                    // delete the found bucket
                    Bucket deleted = this.buckets.remove(i);
                    //System.out.println("Deleted from node: " + deleted.getPrimaryKey());
                    this.setChanged();
                    return new Pair<Integer, Integer>(deleted.getPageNumber(), deleted.getIndex());
                }
            }catch(Exception e){
                MessagePrinter.printMessage(MessagePrinter.MessageType.ERROR, "Error in data type");
                return null;
            }
        }
        // no matching key was found
        MessagePrinter.printMessage(MessageType.ERROR, "No matching key of " + value.toString() + " was found");
        return null;
    }

    @Override
    public boolean underfull() {
        double res = (this.n-1) / 2;
        int min = (int)Math.ceil(res);
        if (min <= this.buckets.size()) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public boolean overfull() {
        int max = this.n-1;
        if (this.buckets.size() <= max) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public boolean willOverfull(int count) {
        int max = this.n-1;
        if (this.buckets.size()+count <= max) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean willUnderfull() {
        double res = (this.n-1) / 2;
        int min = (int)Math.ceil(res);
        if (min <= this.buckets.size()-1) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void removeSearchKey(int index) {
        this.buckets.remove(index);
        this.setChanged();
    }

    @Override
    public void removeSearchKey(int pageNum, boolean less) throws Exception {
        int removeIndex = -1;
        for (int i = 0; i < this.buckets.size(); i++) {
            if (this.buckets.get(i).getPageNumber() == pageNum) {
                removeIndex = i;
            }
        }

        if (removeIndex == -1) {
            MessagePrinter.printMessage(MessageType.ERROR, "This should not happen: RemoveSearchKeyLeafNode");
            return;
        }

        // less variable is disregarded when dealing with leaf nodes
        this.buckets.remove(removeIndex);

        this.setChanged();
    }

    @Override
    public void clear() {
        this.buckets.clear();
        this.nextLeaf = null;
        this.setChanged();
    }

    @Override
    public void readFromHardware(RandomAccessFile tableAccessFile, TableSchema tableSchema) throws Exception {
        int numSearchKeys = tableAccessFile.readInt();
        int nextLeaf = tableAccessFile.readInt();

        if (nextLeaf == -1) {
            this.nextLeaf = null;
        } else {
            this.nextLeaf = new Pair<Integer,Integer>(nextLeaf, -1);
        }

        ArrayList<Object> searchKeys = new ArrayList<>();
        for (int i=0; i < numSearchKeys; ++i) {
            AttributeSchema attributeSchema = tableSchema.getAttributes().get(tableSchema.getPrimaryIndex());

            if (attributeSchema.getDataType().equalsIgnoreCase("integer")) {
                int value = tableAccessFile.readInt();
                searchKeys.add(value);
            } else if (attributeSchema.getDataType().equalsIgnoreCase("double")) {
                double value = tableAccessFile.readDouble();
                searchKeys.add(value);
            } else if (attributeSchema.getDataType().equalsIgnoreCase("boolean")) {
                boolean value = tableAccessFile.readBoolean();
                searchKeys.add(value);
            } else if (attributeSchema.getDataType().contains("char") || attributeSchema.getDataType().contains("varchar")) {
                String value = tableAccessFile.readUTF();
                searchKeys.add(value);
            }
        }


        for (int i=0; i < numSearchKeys; ++i) {
            int pageNumber = tableAccessFile.readInt();
            int index = tableAccessFile.readInt();
            Bucket bucket = new Bucket(pageNumber, index, searchKeys.get(i));
            this.buckets.add(bucket);
        }
    }

    @Override
    public void writeToHardware(RandomAccessFile tableAccessFile) throws Exception {
        tableAccessFile.writeBoolean(this.type);
        tableAccessFile.writeInt(this.pageNumber);
        tableAccessFile.writeInt(this.parent);
        tableAccessFile.writeInt(this.buckets.size());

        if (this.nextLeaf == null) {
            tableAccessFile.writeInt(-1);
        } else {
            tableAccessFile.writeInt(this.nextLeaf.first);
        }

        for (Bucket bucket : this.buckets) {
            Object searchKey = bucket.getPrimaryKey();
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

        for (Bucket bucket : this.buckets) {
            tableAccessFile.writeInt(bucket.getPageNumber());
            tableAccessFile.writeInt(bucket.getIndex());
        }
    }

    @Override
    public void decrementNodePointerPage(int pageNum) {
        if (this.getNextLeaf() != null && this.getNextLeaf().first > pageNum) {
            this.assignNextLeaf(this.getNextLeaf().first-1);
        }
        this.setChanged();
    }


}