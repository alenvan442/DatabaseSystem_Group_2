package StorageManager.Objects;

public class MessagePrinter {
  // Enum to represent message types
  public enum MessageType {
      SUCCESS,
      ERROR
  }

  // Method to print messages
  public static void printMessage(MessageType messageType, String message) {
      switch (messageType) {
          case SUCCESS:
              System.out.println(message + "\nSUCCESS");
              break;
          case ERROR:
              System.err.println(message + "\nERROR");
              break;
          default:
              throw new IllegalArgumentException("Unsupported message type: " + messageType);
      }
  }
}
