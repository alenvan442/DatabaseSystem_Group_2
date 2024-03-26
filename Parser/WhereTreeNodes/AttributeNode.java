package Parser.WhereTreeNodes;

import java.util.List;

import Parser.WhereTreeNodes.Interfaces.OperandNode;
import StorageManager.TableSchema;
import StorageManager.Objects.AttributeSchema;
import StorageManager.Objects.MessagePrinter;
import StorageManager.Objects.Record;
import StorageManager.Objects.MessagePrinter.MessageType;

/*
* Node that holds an attribute name, this should be a leaf
*/
public class AttributeNode implements OperandNode {

    String dataName;

    public AttributeNode(String name) {
        this.dataName = name;
    }

    @Override
    public Object getValue(TableSchema schema, Record record) throws Exception {
        List<AttributeSchema> attrs = schema.getAttributes();

        for (int i = 0; i < attrs.size(); i++)  {
            if (attrs.get(i).getAttributeName().equals(dataName.toLowerCase())) {
                return record.getValues().get(i);
            }
        }

        MessagePrinter.printMessage(MessageType.ERROR, "Invalid attribute name: " + this.dataName + ".");
        return null;

    }

}
