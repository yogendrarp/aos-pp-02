import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.stream.Collectors;

public class Server1 {

    static ArrayList<String> files = new ArrayList<>(Arrays.asList("F1.txt", "F2.txt", "F3.txt"));
    static String path = "D:\\Code\\aos-pp-02-ra\\Server1\\";

    static ArrayList<PriorityQueue<Message>> requestQueues = new ArrayList<PriorityQueue<Message>>();
    static LamportsClock lamportsClock = new LamportsClock();

    public static void main(String[] args) throws IOException {
        ClockComparator clockComparator = new ClockComparator();
        for (String f : files) {
            PriorityQueue<Message> priorityQueue = new PriorityQueue<>(clockComparator);
            requestQueues.add(priorityQueue);
        }
        ServerSocket server = null;
        String filesInfo = files.stream().map(Object::toString).collect(Collectors.joining(","));
        lamportsClock.clockValue = 0;
        for (int i = 0; i < files.size(); i++) {
            QueueProcessor queueProcessor = new QueueProcessor(requestQueues.get(i), "Server1" + files.get(i));
            new Thread(queueProcessor).start();
        }
        try {
            server = new ServerSocket(5000);
            System.out.println("Running Server 1 on port 5000");
            server.setReuseAddress(true);
            while (true) {
                Socket client = server.accept();
                ClientHandler clientHandler = new ClientHandler(client, requestQueues, filesInfo, lamportsClock);
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
