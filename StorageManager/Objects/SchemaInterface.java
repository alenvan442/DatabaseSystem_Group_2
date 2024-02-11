package StorageManager.Objects;

import java.io.IOException;
import java.io.RandomAccessFile;

public interface SchemaInterface {

  public void loadSchema(RandomAccessFile catalogAccessFile) throws IOException;

  public void saveSchema(RandomAccessFile catalogAccessFile) throws IOException;
}
