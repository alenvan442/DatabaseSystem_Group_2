package Parser.WhereTreeNodes;

import java.util.ArrayList;
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
        List<String> potentialMatches = new ArrayList<>();
        List<Object> foundVal = new ArrayList<>();

        potentialMatches.add(dataName.toLowerCase());
        String[] spList = dataName.split(".");
        if (spList.length > 1) {
            potentialMatches.add(spList[1]);
        }

        for (int i = 0; i < attrs.size(); i++)  {
            if (potentialMatches.contains(attrs.get(i).getAttributeName())) {
                return record.getValues().get(i);
            }
        }

        if (foundVal.size() != 1) {
            MessagePrinter.printMessage(MessageType.ERROR, "Invalid attribute name: " + this.dataName + ".");
        }
        return null;

    }

}
