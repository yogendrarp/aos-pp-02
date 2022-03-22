import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.stream.Collectors;

public class Server3 {

    static ArrayList<String> files = new ArrayList<>(Arrays.asList("F1.txt", "F2.txt", "F3.txt"));
    static String path = "D:\\Code\\aos-pp-02-ra\\Server1\\";
    static PriorityQueue<Message> requestQueue;
    static LamportsClock lamportsClock = new LamportsClock();

    public static void main(String[] args) throws IOException {
        ClockComparator clockComparator = new ClockComparator();
        requestQueue = new PriorityQueue<>(clockComparator);
        ServerSocket server = null;
        String filesInfo = files.stream().map(Object::toString).collect(Collectors.joining(","));
        QueueProcessor queueProcessor = new QueueProcessor(requestQueue);
        new Thread(queueProcessor).start();
        try {
            server = new ServerSocket(5002);
            System.out.println("Running Server 3 on port 5002");
            server.setReuseAddress(true);
            while (true) {
                Socket client = server.accept();
                ClientHandler clientHandler = new ClientHandler(client, requestQueue, filesInfo, lamportsClock);
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
