import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import Parser.DDLParser;
import Parser.DMLParser;
import Parser.ParserCommon;
import Parser.Select;
import Parser.Token;
import QueryExecutor.DDLQueryExecutor;
import QueryExecutor.InsertQueryExcutor;
import QueryExecutor.SelectQueryExecutor;
import StorageManager.TableSchema;
import StorageManager.Objects.AttributeSchema;
import StorageManager.Objects.Catalog;
import StorageManager.Objects.MessagePrinter;
import StorageManager.Objects.Record;
import StorageManager.Objects.MessagePrinter.MessageType;

public class UserInterface {
    private Scanner scanner;

    public UserInterface() {
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        System.out.println("\nPlease enter commands, enter <quit> to shutdown the db\n");
        while (true) {
            StringBuilder userInpBuilder = new StringBuilder();
            System.out.print("CASE-C QL> ");

            while (true) {
                String line = scanner.nextLine().trim();

                if (line.equalsIgnoreCase("<quit>")) {
                    return;
                }

                userInpBuilder.append(line).append(" ");

                if (line.endsWith(";")) {
                    break;
                }
            }

            String userInput = userInpBuilder.toString().trim();

            if (!userInput.endsWith(";")) {
                System.err.println("ERROR: Each command must end with a semicolon (;).");
                continue;
            }

            String[] commands = userInput.split(";");
            for (String command : commands) {
                processUserCommand(command.trim().concat(";"));
            }
        }
    }

    private void processUserCommand(String command) {
        try {
            ArrayList<Token> tokens = ParserCommon.Tokenize(command);
            if (tokens.get(0).getVal().equalsIgnoreCase("create") &&
                    tokens.get(1).getVal().equalsIgnoreCase("table")) {
                TableSchema TableSchema = DDLParser.parseCreateTable(tokens);
                DDLQueryExecutor ddlQueryExecutor = new DDLQueryExecutor("create", TableSchema);
                ddlQueryExecutor.excuteQuery();
            } else if (tokens.get(0).getVal().equalsIgnoreCase("drop") &&
                    tokens.get(1).getVal().equalsIgnoreCase("table")) {
                String tableName = DDLParser.parseDropTable(tokens);
                DDLQueryExecutor ddlQueryExecutor = new DDLQueryExecutor(tableName, "drop");
                ddlQueryExecutor.excuteQuery();
            } else if (tokens.get(0).getVal().equalsIgnoreCase("alter") &&
                    tokens.get(1).getVal().equalsIgnoreCase("table")) {
                HashMap<String, String> tableAlterInfo = DDLParser.parseAlterTable(tokens);
                DDLQueryExecutor ddlQueryExecutor = new DDLQueryExecutor(tableAlterInfo.get("tableName"), "alter",
                        tableAlterInfo.get("deflt"), tableAlterInfo.get("attriname"), tableAlterInfo.get("type"),
                        tableAlterInfo.get("adddrop"),tableAlterInfo.get("isDeflt"));
                ddlQueryExecutor.excuteQuery();
            } else if (tokens.get(0).getVal().equalsIgnoreCase("insert") &&
                    tokens.get(1).getVal().equalsIgnoreCase("into")) {
                Map<String, List<Record>> insertMap = DMLParser.parseInsert(tokens);
                InsertQueryExcutor insertQueryExcutor = new InsertQueryExcutor(
                        insertMap.entrySet().iterator().next().getKey(),
                        insertMap.entrySet().iterator().next().getValue(), command);
                insertQueryExcutor.excuteQuery();
            } else if (tokens.get(0).getVal().equalsIgnoreCase("select")) {
                Select select = DMLParser.parseSelect(tokens);
                SelectQueryExecutor selectQueryExecutor = new SelectQueryExecutor(select);
                selectQueryExecutor.excuteQuery();
            } else if (tokens.get(0).getVal().equalsIgnoreCase("display") &&
                    tokens.get(1).getVal().equalsIgnoreCase("schema")) {
                DMLParser.parseDisplaySchema(tokens);
                displaySchemaResult();
            } else if (tokens.get(0).getVal().equalsIgnoreCase("display") &&
                    tokens.get(1).getVal().equalsIgnoreCase("info")) {
                String tableName = DMLParser.parseDisplayInfo(tokens);
                displayInfoResult(tableName);
            } else {
                System.err.println("Not a valid command");
            }
        } catch (IndexOutOfBoundsException e) {
            System.err.println("Unexpected end to command");
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    private void displayInfoResult(String tableName) throws Exception {
        Catalog catalog = Catalog.getCatalog();
        TableSchema tableSchema = catalog.getSchema(tableName);
        System.out.println("Table name: " + tableSchema.getTableName());
        for (AttributeSchema attributeSchema : tableSchema.getAttributes()) {
            System.out.println(
                    "\t" + attributeSchema.getAttributeName() + ": " + attributeSchema.getDataType() + " "
                            + (attributeSchema.isPrimaryKey() ? "primaryKey"
                                : ((attributeSchema.isNotNull() ? "notnull" : "") +
                                    (attributeSchema.isUnique() ? " unique" : ""))));

        }
        System.out.println("Pages: " + tableSchema.getNumPages() + "\n" +
                "Records: " + tableSchema.getRecords());
        MessagePrinter.printMessage(MessageType.SUCCESS, null);
    }

    private void displaySchemaResult() throws Exception {
        Catalog catalog = Catalog.getCatalog();

        System.out.println("DB Location: " + catalog.getDbLocation() + "\n" +
                "Page Size: " + catalog.getPageSize() + "\n" +
                "Buffer Size: " + catalog.getBufferSize() + "\n");

        Map<Integer, TableSchema> tables = catalog.getSchemas();
        if (tables.isEmpty()) {
            System.out.println("No tables to display");
            MessagePrinter.printMessage(MessageType.SUCCESS, null);
        } else {
            System.out.println("Tables:\n");
            for (Integer tableNumber : tables.keySet()) {
                TableSchema tableSchema = tables.get(tableNumber);
                System.out.println("Table name: " + tableSchema.getTableName());
                for (AttributeSchema attributeSchema : tableSchema.getAttributes()) {
                    System.out.println(
                            "\t" + attributeSchema.getAttributeName() + ": " + attributeSchema.getDataType() + " "
                                    + (attributeSchema.isPrimaryKey() ? "primaryKey"
                                            : ((attributeSchema.isNotNull() ? "notnull" : "") +
                                                (attributeSchema.isUnique() ? " unique" : ""))));

                }
                System.out.println("Pages: " + tableSchema.getNumPages() + "\n" +
                        "Records: " + tableSchema.getRecords() + "\n");
            }
            MessagePrinter.printMessage(MessageType.SUCCESS, null);
        }

    }

}
