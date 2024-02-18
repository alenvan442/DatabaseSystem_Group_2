package Parser;

import StorageManager.Objects.AttributeSchema;
import StorageManager.Objects.Catalog;
import StorageManager.Objects.Page;
import StorageManager.Objects.Table;
import StorageManager.StorageManager;
import StorageManager.TableSchema;

import java.awt.*;
import java.util.ArrayList;
import java.util.Objects;
import java.util.List;

public class DMLParser extends ParserCommon{

    public static void parseInsert(String dmlStatement) throws Exception {
        // Format: insert into <name> values <tuples>;

        ArrayList<AttributeSchema> attributes = new ArrayList<>();
        Catalog catalog = Catalog.getCatalog();

        ArrayList<String> tokens = Tokenize(dmlStatement);
        if (tokens.size() != 5) {
            throw new IllegalArgumentException("Insert command should be five arguments in total");
        }
        if (!tokens.getFirst().equalsIgnoreCase("insert")
                && !tokens.get(1).equalsIgnoreCase("into")
                && !tokens.get(3).equalsIgnoreCase("values"))
        {
            throw new IllegalArgumentException("Incorrect insert command. Check it. ");
        }

        String tableName = tokens.get(2).toLowerCase();

        // create list of tuples.. ((1, "  ", 2, 3), (2, " ", 3, 4)...)
        List<List<String>> tuples = new ArrayList<>();
        for (List<String> tuple: tuples) {
            if (!tuple.getFirst().equals("(")|| !tuple.contains(",") || !tuple.get(2).startsWith("\""))
            {
                throw new IllegalArgumentException("Incorrect tuple format");
            }

        }
        /**
        for (TableSchema schema: catalog.getSchemas().values()) {
            if (schema.getTableName().contains(tableName)) {

            }
        }
         **/
        // TODO

        // get the Catalog, access the map include the schema,
        // attributes have the match the one in table, else throw error
        // access tableName to get the attribute

        // insert, update, check if values are passed in

    }

    public static void parseSelect(String dmlStatement) throws Exception{
        // Format: select * from <name>;

        ArrayList<String> tokens = Tokenize(dmlStatement);
        Catalog catalog = Catalog.getCatalog();

        if (tokens.size() != 4 || !tokens.getFirst().equalsIgnoreCase("select")
                             || !tokens.get(1).equalsIgnoreCase("*")
                            || !tokens.get(2).equals("from")
                            || tokens.get(3).isEmpty())
        {
            throw new IllegalArgumentException("Incorrect select command");
        }
        else
        {
            String tableName = tokens.get(3).toLowerCase();
            ArrayList<AttributeSchema> attributes = new ArrayList<>();
            for (TableSchema schema: catalog.getSchemas().values()) {
                if (schema.getTableName().contains(tableName)){

                    //TODO
                }
            }

        }

    }

    public static void parseDisplay(String dmlStatement) throws Exception {
        // Command: display schema
        // It will show: database location, page size, buffer, size, and table schema
        // Command: display info <name>
        // It will show: table name, table schema, # of pages, # of records

        ArrayList<String> tokens = Tokenize(dmlStatement);
        Catalog catalog = Catalog.getCatalog();

        if (tokens.getFirst().equalsIgnoreCase("display")
                && tokens.get(1).equalsIgnoreCase("schema"))
        {
            System.out.println("Database Location: " + catalog.getDbLocation());
            System.out.println("Page Size: " + catalog.getPageSize());
            System.out.println("Buffer Size: " + catalog.getBufferSize());
            System.out.println("Table Schema: " + catalog.getSchemas());
        }
        else if (tokens.getFirst().equalsIgnoreCase("display")
                && tokens.get(1).equalsIgnoreCase("info")
                && tokens.size() == 3)
        {
            String tableName = tokens.get(2).toLowerCase();

            for (TableSchema tableSchema: catalog.getSchemas().values()) {
                if (tableSchema.getTableName().contains(tableName)) {
                    System.out.println("Table Name: " + tableName);
                    System.out.println("Table Schema: " + tableSchema.getTableName());
                    System.out.println("Number of Pages: " + tableSchema.getNumPages());
                    System.out.println("Number of Records: " + tableSchema.getRecords());
                }
                else{
                    throw new Exception("Table not found");
                }
            }
        }
        else
        {
            throw new Exception("Invalid Display Command. Format: display info <name>");
        }
    }

    public static void parseDelete(String dmlStatement) {

    }

    public static void parseUpdate(String dmlStatement) {
        // delete / insert record
    }
}
