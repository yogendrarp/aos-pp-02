import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Queue;

public class QueueProcessor implements Runnable {
    PriorityQueue<Message> requestQueue;
    String queueName;
    HashSet<String> requests;

    public QueueProcessor(PriorityQueue<Message> requestQueue, String queueName, HashSet<String> requests) {
        this.requestQueue = requestQueue;
        this.queueName = queueName;
        this.requests = requests;
    }

    public void run() {
        while (true) {
            try {
                if (requestQueue.size() > 0) {
                    System.out.println("Processing");
                    Message msg = requestQueue.peek();
                    System.out.println(msg);
                    System.out.println(msg.dataOutputStream);
                    msg.dataOutputStream.writeBytes("SUCESS");
                    Thread.sleep(20000);
                    requests.remove("c:"+msg.clientId + ",f:" + msg.fileName);
                }
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
        }
    }
}
