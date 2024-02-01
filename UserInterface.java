import java.util.Scanner;

import Parser.DDLParser;
import Parser.DMLParser;



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
                processUserCommand(command.trim());
            }
        }
    }

    public void processUserCommand(String command) {
        System.out.println("Processing command: " + command);
        if (command.toLowerCase().contains("create table")) {
            DDLParser.parseCreateTable(command);
        } else if (command.toLowerCase().contains("drop table")) {
            DDLParser.parseDropTable(command);
        } else if (command.toLowerCase().contains("alter table")) {
            DDLParser.parseAlterTable(command);
        } else if (command.toLowerCase().contains("insert into")) {
            DMLParser.parseInsert(command);
        } else if (command.toLowerCase().contains("display schema") || command.toLowerCase().contains("display info")) {
            DMLParser.parseDisplay(command);
        } else {
            System.err.println("ERROR: " + command + " is not a valid command");
        }
    }
}
