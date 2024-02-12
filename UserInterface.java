import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import Parser.DDLParser;
import Parser.DMLParser;
import StorageManager.TableSchema;
import StorageManager.Objects.AttributeSchema;
import StorageManager.Objects.Catalog;
import StorageManager.Objects.MessagePrinter;
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

    public void processUserCommand(String command) {
        try{
            System.out.println("Processing command: " + command);
            if (command.toLowerCase().startsWith("create table")) {
                DDLParser.parseCreateTable(command);
            } else if (command.toLowerCase().startsWith("drop table")) {
                DDLParser.parseDropTable(command);
            } else if (command.toLowerCase().startsWith("alter table")) {
                DDLParser.parseAlterTable(command);
            } else if (command.toLowerCase().startsWith("insert into")) {
                DMLParser.parseInsert(command);
            } else if (command.toLowerCase().startsWith("display schema") || command.toLowerCase().startsWith("display info")) {
                DMLParser.parseDisplay(command);
            } else {
                MessagePrinter.printMessage(MessageType.ERROR, command + "Is not valid");
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
