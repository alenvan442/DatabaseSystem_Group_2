public class Main {
  public static void main(String[] args) {
    if (args.length != 3) {
      System.err.println("java Main <db_loc> <page_size> <buffer_size>");
      System.exit(0);
    } else {
      String dbLocation = args[0];
      int pageSize =  Integer.parseInt(args[1]);
      int bufferSize = Integer.parseInt(args[2]);
      Database database = new Database(dbLocation, pageSize, bufferSize);
      try {
        database.start();
      } catch (Exception e) {
        System.err.println("Failed to start database");
        if (e.getMessage() != null) {
          System.err.println(e.getMessage());
        }
        System.exit(0);
      }
    }
  }
}