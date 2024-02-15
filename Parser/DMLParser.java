package Parser;

import StorageManager.Objects.AttributeSchema;
import StorageManager.Objects.Catalog;
import StorageManager.Objects.Page;
import StorageManager.Objects.Table;
import StorageManager.StorageManager;
import StorageManager.TableSchema;

import java.util.ArrayList;

public class DMLParser extends ParserCommon{

    private static Catalog catalog;
    private static StorageManager storageManager;
    private static Table table;
    private static Page page;
    private static TableSchema tableSchema;

    public static void parseInsert(String dmlStatement) throws Exception {
        // Format: insert into <name> values <tuples>;
        // Example: insert into foo values (1 "foo" true 2.1);
        ArrayList<AttributeSchema> attributes = new ArrayList<>();

        ArrayList<String> tokens = Tokenize(dmlStatement);
        if (tokens.size() != 5) {
            throw new IllegalArgumentException("Insert command should be five arguments in total");
        }
        if (!tokens.getFirst().equalsIgnoreCase("insert")
                && !tokens.get(1).equalsIgnoreCase("into")
                && !tokens.get(2).equals("values")) {
            throw new IllegalArgumentException("Incorrect insert command. Check it. ");
        }

        String tableName;

        //if (tableName.charAt(0) == "A") {

        //}


        // get the Catalog, access the map include the schema,
            // attributes have the match the one in table, else throw error
        // access tableName to get the attribute

        // insert, update, check if values are passed in


    }

    public static void parseSelect(String dmlStatement) throws Exception{
        // Command: select * from <name>;
        // Example: select * from foo;

        String[] parts = dmlStatement.split(" ");
        if (parts.length < 4 || !parts[0].equalsIgnoreCase("select")
                             || !parts[2].equalsIgnoreCase("from"))
        {
            throw new IllegalArgumentException("Incorrect select command");
        }
        //TODO

    }

    public static void parseDisplay(String dmlStatement) throws Exception {
        // Command: display schema
        // It will show: database location, page size, buffer, size, and table schema
        // Command: display info <name>
        // It will show: table name, table schema, # of pages, # of records

        ArrayList<String> tokens = Tokenize(dmlStatement);

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
            System.out.println("Table Name: " + tableSchema.getTableName());
            System.out.println("Table Schema: " + catalog.getSchemas());
            System.out.println("Number of Pages: " + table.getNumPages());
            System.out.println("Number of Records: " + page.getNumRecords());
        }
        else
        {
            throw new Exception("Invalid Display Command or Incorrect table name");
        }
    }

    public static void parseDelete(String dmlStatement) {

    }

    public static void parseUpdate(String dmlStatement) {
        // delete / insert record
    }
}
