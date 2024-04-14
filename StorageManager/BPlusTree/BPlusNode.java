package StorageManager.BPlusTree;

import Parser.Type;
import StorageManager.Objects.Utility.Pair;

public interface BPlusNode {
    public Pair<Integer, Integer> search(Object value, Type type) throws Exception;
}
