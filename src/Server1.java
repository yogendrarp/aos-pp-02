import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.stream.Collectors;

public class Server1 {
    //List of All hosted Files
    static ArrayList<String> files = new ArrayList<>(Arrays.asList("F1.txt", "F2.txt", "F3.txt", "F4.txt", "F5.txt", "F6.txt", "F7.txt", "F8.txt"));
    //Servers are aware of other two servers
    static ArrayList<String> otherServers = new ArrayList<>(Arrays.asList("localhost:5001", "localhost:5002"));
    static String path = "D:\\Code\\aos-pp-02-ra\\Server1\\";
    static ArrayList<Boolean> currReq = new ArrayList<>();
    static ArrayList<PriorityQueue<Message>> requestQueues = new ArrayList<PriorityQueue<Message>>();
    static HashSet<String> requests = new HashSet<>();
    static LamportsClock lamportsClock = new LamportsClock();

    public static void main(String[] args) throws IOException {
        //Compare clocks see comments in the respective class
        ClockComparator clockComparator = new ClockComparator();
        for (String f : files) {
            PriorityQueue<Message> priorityQueue = new PriorityQueue<>(clockComparator);
            requestQueues.add(priorityQueue);
            currReq.add(false);
        }
        ServerSocket server = null;
        String filesInfo = files.stream().map(Object::toString).collect(Collectors.joining(","));
        lamportsClock.clockValue = 0;
        for (int i = 0; i < files.size(); i++) {
            QueueProcessor queueProcessor = new QueueProcessor(requestQueues.get(i), "Server1" + files.get(i), requests, otherServers, currReq, path + files.get(i));
            new Thread(queueProcessor).start();
        }
        try {
            server = new ServerSocket(5000);
            System.out.println("Running Server 1 on port 5000");
            //Reuse address to cater multiple clients
            server.setReuseAddress(true);
            while (true) {
                Socket client = server.accept();
                ClientHandler clientHandler = new ClientHandler(client, requestQueues, filesInfo, lamportsClock, requests, path);
                new Thread(clientHandler).start();
                Thread.sleep(1000);
            }
        } catch (IOException | InterruptedException e) {
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
