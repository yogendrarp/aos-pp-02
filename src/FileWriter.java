import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class FileWriter {
    /**
     * Static Method to Append to the file specified
     * @param path
     * @param msg
     */
    public static void AppendToFile(String path, String msg) {
        try {
            msg += "\n";
            Files.write(Paths.get(path), msg.getBytes(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
