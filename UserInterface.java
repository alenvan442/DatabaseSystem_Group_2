import java.util.Scanner;


import StorageManager.StorageManager;
import StorageManager.Objects.Catalog;

public class UserInterface {
    private StorageManager storageManager;
    private Scanner scanner;
    private Catalog catalog;

    public UserInterface(StorageManager storageManager, Catalog catalog, Scanner scanner) {
        this.storageManager = storageManager;
        this.catalog = catalog;
        this.scanner = scanner;
    }

    public void start() {
        System.out.println("\nPlease enter commands, enter <quit> to shutdown the db\n");
        while (true) {
            System.out.print("CASE-C QL> ");
            String userInput = scanner.nextLine().trim();
            if (userInput.equalsIgnoreCase("<quit>")) {
                System.out.println("Safely shutting down the database...");
                System.out.println("Purging page buffer...");
                System.out.println("Saving catalog...\n");
                System.out.println("Exiting the database...");
                break;
            }
            processUserCommand(userInput);
        }

        scanner.close();

    }

    public void processUserCommand(String command) {

    }
}
