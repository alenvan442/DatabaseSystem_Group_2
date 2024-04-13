package StorageManager.BPlusTree;

import Parser.Token;

public interface BPlusNode {
    public Bucket search(Object value, Token primaryKey);
}
