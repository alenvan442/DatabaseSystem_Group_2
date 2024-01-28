package Parser;

import javax.xml.catalog.Catalog;
import StorageManager.StorageManager;

public class DMLParser {
    private Catalog catalog;
    private StorageManager storageManager;

    public DMLParser(Catalog catalog, StorageManager storageManager) {
        this.catalog = catalog;
        this.storageManager = storageManager;
    }

    public void parseInsert(String dmlStatement) {
        // Format: insert into <name> values <tuples>;
        // Example: insert into foo values (1 "foo" true 2.1);

        String[] parts = dmlStatement.split(" ");
        if (parts.length < 4 || !parts[0].equalsIgnoreCase("insert")
                || !parts[1].equalsIgnoreCase("into")
                || !parts[3].equalsIgnoreCase("values")) {
            throw new IllegalArgumentException("Incorrect insert command");

        }

        String tableName = parts[2];


    }

    public void parseSelect(String dmlStatement) {
        // Command: select * from <name>;
        // Example: select * from foo;

        String[] parts = dmlStatement.split(" ");
        if (parts.length < 4 || !parts[0].equalsIgnoreCase("select")
                             || !parts[2].equalsIgnoreCase("from")) {
            throw new IllegalArgumentException("Incorrect select command");
        }

    }

    public void parseDisplay(String dmlStatement) {
        // Command: display schema
        // It will show: database location, page size, buffer, size, and table schema
        // Command: display info <name>
        // It will show: table name, table schema, number of pages, number of records

        String[] parts = dmlStatement.split(" ");

        if (parts.length < 3 || !parts[0].equalsIgnoreCase("display")) {
            throw new IllegalArgumentException("Incorrect display command");
        }

        if (parts[1].equalsIgnoreCase("schema")) {
            System.out.println("Database Location: " + " ");
            System.out.println("Page Size: " + " ");
            System.out.println("Buffer Size: " + " ");
            System.out.println("Table Schema: " + " ");
        }
        else if (parts[1].equalsIgnoreCase("info") && parts.length == 3) {
            String tableName = parts[2];

            System.out.println("Table Name: " + tableName);
            System.out.println("Table Schema: " + " ");
            System.out.println("Number of Pages: " + " ");
            System.out.println("Number of Records: " + " ");
        }
        else {
            System.out.println("Invalid Display Command");
        }

    }

    public void parseDelete(String dmlStatement) {

    }

    public void parseUpdate(String dmlStatement) {

    }
}
