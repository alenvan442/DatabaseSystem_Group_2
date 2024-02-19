import java.util.ArrayList;
import java.util.Scanner;

import Parser.DDLParser;
import Parser.DMLParser;
import Parser.ParserCommon;
import StorageManager.Objects.Catalog;


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
                    System.out.println("Safely shutting down the database...\n" +
                                            "Purging page buffer...\n" +
                                            "Saving catalog...\n" +
                                            "Exiting the database...");
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
        System.out.println("Processing command: " + command);
        try{
            ArrayList<String> tokens = ParserCommon.Tokenize(command);
            if (command.toLowerCase().startsWith("create table")) {
                // DDLParser.parseCreateTable(command);
            } else if (command.toLowerCase().startsWith("drop table")) {
                DDLParser.parseDropTable(command);
            } else if (command.toLowerCase().startsWith("alter table")) {
                DDLParser.parseAlterTable(command);
            } else if (command.toLowerCase().startsWith("insert into")) {
                DMLParser.parseInsert(command);
            } else if (command.toLowerCase().startsWith("display schema")) {
                DMLParser.parseDisplaySchema(tokens);
                displaySchemaResult();
            } else if (command.toLowerCase().startsWith("display info")) {
                String tableName  = DMLParser.parseDisplayInfo(tokens);
            } else {
                System.out.println("Not a valid command");
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }


    private void displaySchemaResult() {
        Catalog catalog = Catalog.getCatalog();

        System.out.println("Database Location: " + catalog.getDbLocation());
        System.out.println("Page Size: " + catalog.getPageSize());
        System.out.println("Buffer Size: " + catalog.getBufferSize());
        System.out.println("Table Schema: " + catalog.getSchemas());

    }


}
