import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.stream.Collectors;

public class Server1 {

    static ArrayList<String> files = new ArrayList<>(Arrays.asList("F1.txt", "F2.txt", "F3.txt"));
    static String path = "D:\\Code\\aos-pp-02-ra\\Server1\\";
    static Queue<Message> requestQueue = new LinkedList<>();

    public static void main(String[] args) throws IOException {
        ServerSocket server = null;
        String filesInfo = files.stream().map(Object::toString).collect(Collectors.joining(","));
        QueueProcessor queueProcessor = new QueueProcessor(requestQueue);
        new Thread(queueProcessor).start();
        try {
            server = new ServerSocket(5000);
            System.out.println("Running Server 1 on port 5000");
            server.setReuseAddress(true);
            while (true) {
                Socket client = server.accept();
                System.out.println("New Client connected" + client.getInetAddress().getHostAddress());
                ClientHandler clientHandler = new ClientHandler(client, requestQueue, filesInfo);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (server != null) {
                try {
                    server.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
