package StorageManager.BPlusTree;

import Parser.Type;
import StorageManager.TableSchema;
import StorageManager.Objects.AttributeSchema;
import StorageManager.Objects.MessagePrinter;
import StorageManager.Objects.MessagePrinter.MessageType;
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
        buckets = new ArrayList<>();
    }

    public ArrayList<Bucket> getSK() {
        return this.buckets;
    }

    public void setSK(List<Bucket> newbuckets) {
        this.buckets.clear();
        this.buckets.addAll(newbuckets);
        this.setChanged();
    }

    public void addBucket(Bucket b, int index){
        if (index < 0) {
            index = this.buckets.size() - (index + 1);
        }
        buckets.add(index, b);
        this.setChanged();
    }

    public void assignNextLeaf(LeafNode ln){
        nextLeaf = new Pair<Integer,Integer>(ln.pageNumber, -1);
        this.setChanged();
    }

    public void assignNextLeaf(int ln){
        nextLeaf = new Pair<Integer,Integer>(ln, -1);
        this.setChanged();
    }

    public Pair<Integer, Integer> getNextLeaf() {
        return this.nextLeaf;
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
     *
     * @param value
     * @param type
     * @param newPointer
     * @throws Exception
     */
    public void replacePointer(Object value, Type type, Pair<Integer, Integer> newPointer) throws Exception {
        for(int i = 0; i < buckets.size(); i++){
            try {
                Object pk = buckets.get(i).getPrimaryKey();
                boolean found = false;
                if (this.compareKey(pk, value, type) == 0) {
                    found = true;
                }

                if (found) {
                    //BELONGS HERE
                    buckets.get(i).setPointer(newPointer.first, newPointer.second);
                    this.setChanged();
                }
            }catch(Exception e){
                MessagePrinter.printMessage(MessagePrinter.MessageType.ERROR, "Error in data type");
            }
        }
        // no matching key was found
        MessagePrinter.printMessage(MessageType.ERROR, "No matching key of " + value.toString() + " was found");
    }

    @Override
    public Pair<Integer, Integer> insert(Object value, Type type) throws Exception {
        for(int i = 0; i < buckets.size(); i++){
            try {
                Bucket curr = buckets.get(i);
                Object pk = curr.getPrimaryKey();
                int result = this.compareKey(pk, value, type);
                if (result == 0) {
                    throw new Exception("PrimaryKey is already in db");
                } else if (result > 0) {
                    // it is previous in line
                    Bucket _new = new Bucket(this.pageNumber, curr.getIndex()+1, value);
                    this.buckets.add(i-1, _new);
                    this.setChanged();
                    return new Pair<Integer, Integer>(_new.getPageNumber(), _new.getIndex());
                }
            }catch(Exception e){
                MessagePrinter.printMessage(MessagePrinter.MessageType.ERROR, "Error in data type");
                return null;
            }
        }
        // if we reach this, then we need to insert at end of leafnode
        Bucket _new = new Bucket(this.pageNumber, this.buckets.size(), value);
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
        double res = this.n-1 / 2;
        int min = (int)Math.ceil(res);
        if (min <= this.buckets.size()) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean overfull() {
        int max = this.n-1;
        if (this.buckets.size() <= max) {
            return true;
        } else {
            return false;
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
        double res = this.n-1 / 2;
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

}