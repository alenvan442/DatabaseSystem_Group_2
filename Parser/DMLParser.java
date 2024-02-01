package Parser;

import StorageManager.Objects.Catalog;
import StorageManager.Objects.Page;
import StorageManager.Objects.Table;
import StorageManager.StorageManager;
import StorageManager.TableSchema;

public class DMLParser {

    private static Catalog catalog;
    private static StorageManager storageManager;
    private static Table table;
    private static Page page;
    private static TableSchema tableSchema;


    public static void parseInsert(String dmlStatement) {
        // Format: insert into <name> values <tuples>;
        // Example: insert into foo values (1 "foo" true 2.1);

        String[] parts = dmlStatement.split(" ");
        if (parts.length < 4 || !parts[0].equalsIgnoreCase("insert")
                || !parts[1].equalsIgnoreCase("into")
                || !parts[3].equalsIgnoreCase("values"))
        {
            throw new IllegalArgumentException("Incorrect insert command");
        }
        String tableName = parts[2];
    }

    public static void parseSelect(String dmlStatement) {
        // Command: select * from <name>;
        // Example: select * from foo;

        String[] parts = dmlStatement.split(" ");
        if (parts.length < 4 || !parts[0].equalsIgnoreCase("select")
                             || !parts[2].equalsIgnoreCase("from"))
        {
            throw new IllegalArgumentException("Incorrect select command");
        }

    }

    public static void parseDisplay(String dmlStatement) {
        // Command: display schema
        // It will show: database location, page size, buffer, size, and table schema
        // Command: display info <name>
        // It will show: table name, table schema, # of pages, # of records

        String[] parts = dmlStatement.split(" ");

        if (parts.length < 2 || parts.length > 3 || !parts[0].equalsIgnoreCase("display"))
        {
            throw new IllegalArgumentException("Incorrect display command");
        }

        if (parts[1].equalsIgnoreCase("schema"))
        {
            System.out.println("Database Location: " + catalog.getDbLocation());
            System.out.println("Page Size: " + catalog.getPageSize());
            System.out.println("Buffer Size: " + catalog.getBufferSize());
            System.out.println("Table Schema: " + catalog.getSchemas());
        }
        else if (parts[1].equalsIgnoreCase("info") && parts.length == 3)
        {
            System.out.println("Table Name: " + tableSchema.getTableName());
            System.out.println("Table Schema: " + catalog.getSchemas());
            System.out.println("Number of Pages: " + table.getNumPages());
            System.out.println("Number of Records: " + page.getNumRecords());
        }
        else
        {
            System.out.println("Invalid Display Command");
        }
    }

    public static void parseDelete(String dmlStatement) {

    }

    public static void parseUpdate(String dmlStatement) {

    }
}
