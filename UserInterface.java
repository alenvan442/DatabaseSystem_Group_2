import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import Parser.DDLParser;
import Parser.DMLParser;
import Parser.ParserCommon;
import StorageManager.TableSchema;
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
        try{
            ArrayList<String> tokens = ParserCommon.Tokenize(command);
            if (tokens.get(0).toLowerCase().equals("create") &&
                tokens.get(1).toLowerCase().equals("table")) {
                TableSchema TableSchema = DDLParser.parseCreateTable(tokens);
            } else if (tokens.get(0).toLowerCase().equals("drop") &&
                    tokens.get(1).toLowerCase().equals("table")) {
                DDLParser.parseDropTable(tokens);
            } else if (tokens.get(0).toLowerCase().equals("alter") &&
            tokens.get(1).toLowerCase().equals("table")) {
                HashMap<String, String> tableHash = DDLParser.parseAlterTable(tokens);
            } else if (tokens.get(0).toLowerCase().equals("insert") &&
                        tokens.get(1).toLowerCase().equals("into")) {
                DMLParser.parseInsert(command);
            } else if (tokens.get(0).toLowerCase().equals("display") &&
            tokens.get(1).toLowerCase().equals("schema")) {
                DMLParser.parseDisplaySchema(tokens);
                displaySchemaResult();
            } else if (tokens.get(0).toLowerCase().equals("display") &&
            tokens.get(1).toLowerCase().equals("info")) {
                String tableName  = DMLParser.parseDisplayInfo(tokens);
            } else {
                System.out.println("Not a valid command");
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }


    private void displayInfoResult() {

    }


    private void displaySchemaResult() {
        Catalog catalog = Catalog.getCatalog();

        System.out.println("Database Location: " + catalog.getDbLocation());
        System.out.println("Page Size: " + catalog.getPageSize());
        System.out.println("Buffer Size: " + catalog.getBufferSize());
        System.out.println("Table Schema: " + catalog.getSchemas());

    }


}
