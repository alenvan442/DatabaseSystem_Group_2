package Parser;

import StorageManager.Objects.AttributeSchema;
import StorageManager.Objects.Catalog;
import StorageManager.Objects.MessagePrinter;
import StorageManager.Objects.Page;
import StorageManager.Objects.Table;
import StorageManager.Objects.MessagePrinter.MessageType;
import StorageManager.StorageManager;
import StorageManager.TableSchema;

import javax.management.Attribute;
import java.util.ArrayList;
import java.util.Objects;
import java.util.List;

public class DMLParser extends ParserCommon{

    public static void parseInsert(String dmlStatement) throws Exception {
        // Format: insert into <name> values <tuples>;
        ArrayList<AttributeSchema> attributes = new ArrayList<>();
        Catalog catalog = Catalog.getCatalog();
        String tableName;

        ArrayList<String> tokens = Tokenize(dmlStatement);

        if (!tokens.getFirst().equalsIgnoreCase("insert")
                && !tokens.get(1).equalsIgnoreCase("into")
                && !tokens.get(3).equalsIgnoreCase("values"))
        {
            throw new IllegalArgumentException("Incorrect insert command. Check it. ");
        }
        if (Character.isLetter(tokens.get(2).charAt(0)) && keywordCheck(tokens.get(2))) {
            tableName = tokens.get(2).toLowerCase();
        }
        else {
            throw new Exception("table name must all be alphabets");
        }
        /**
        List<List<String>> tuples = new ArrayList<>();
        for (List<String> tuple: tuples) {
            if (!tuple.getFirst().equals("(")|| !tuple.contains(",") || !tuple.get(2).startsWith("\""))
            {
            }
        }**/
        int i = 4;
        while (!tokens.get(i).equals(")") && i < 99999)
        {


        }
        // TODO
        // get the Catalog, access the map include the schema,
        // attributes have the match the one in table, else throw error
        // access tableName to get the attribute
        // insert, update, check if values are passed in

    }

    public static ArrayList<String> parseSelect(String dmlStatement) throws Exception{
        // Format: select * from <name>;
        ArrayList<String> tokens = Tokenize(dmlStatement);
        Catalog catalog = Catalog.getCatalog();
        ArrayList<String> attributes = new ArrayList<>();
        String tableName;

        if (tokens.size() != 4 || !tokens.getFirst().equalsIgnoreCase("select")
                             || !tokens.get(1).equalsIgnoreCase("*")
                            || !tokens.get(2).equals("from")
                            || tokens.get(3).isEmpty())
        {
            MessagePrinter.printMessage(MessageType.ERROR, "incorrect format");
        }
        else
        {   // if the command is correct, check if table name exists
            tableName = tokens.get(3).toLowerCase();
            for (TableSchema schema: catalog.getSchemas().values()) {
                if (!schema.getTableName().contains(tableName)){
                    MessagePrinter.printMessage(MessageType.ERROR, "table not found");
                }
                else
                {
                    attributes.add(String.valueOf(schema.getAttributes()));
                }
            }
        }
        return attributes;
    }

    public static void parseDisplaySchema(ArrayList<String> tokens) throws Exception {
        if (!tokens.get(0).equalsIgnoreCase("display") && !tokens.get(1).equalsIgnoreCase("schema")
                && !tokens.get(2).equals(";")) {
            MessagePrinter.printMessage(MessageType.ERROR, "");
        }
    }

    public static String parseDisplayInfo(ArrayList<String> tokens) throws Exception {
        // Command: display schema
        // It will show: database location, page size, buffer, size, and table schema
        // Command: display info <name>
        // It will show: table name, table schema, # of pages, # of records
        String tableName;

        if (tokens.get(0).equalsIgnoreCase("display")
        && tokens.get(1).equalsIgnoreCase("info")
        && tokens.get(3).equals(";")
        && tokens.size() == 4)
        {
            tableName = tokens.get(2).toLowerCase();
            return tableName;
        }
        else {
            MessagePrinter.printMessage(MessageType.ERROR, "incorrect format");
            return null;
        }
    }

    public static void parseDelete(String dmlStatement) {

    }

    public static void parseUpdate(String dmlStatement) {
        // delete / insert record
    }
}
