package StorageManager.Objects.TreeNodes;

import java.util.List;

import StorageManager.TableSchema;
import StorageManager.Objects.AttributeSchema;
import StorageManager.Objects.MessagePrinter;
import StorageManager.Objects.Record;
import StorageManager.Objects.MessagePrinter.MessageType;
import StorageManager.Objects.TreeNodes.Interfaces.OperandNode;

/*
* Node that holds an attribute name, this should be a leaf
*/
public class AttributeNode implements OperandNode {

    String dataType;
    String dataName;

    public AttributeNode(String type, String name) {
        this.dataType = type;
        this.dataName = name;
    }

    @Override
    public Object getValue(TableSchema schema, Record record) {
        List<AttributeSchema> attrs = schema.getAttributes();
        
        for (int i = 0; i < attrs.size(); i++)  {
            if (attrs.get(i).getAttributeName().equals(dataName.toLowerCase())) {
                return record.getValues().get(i);
            }
        }

        return null; // invalid dataName TODO use messageprinter

    }
    
}
