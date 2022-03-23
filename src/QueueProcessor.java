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
                System.out.println(queueName + " size : " + requestQueue.size());
                if (requestQueue.size() > 0) {
                    System.out.println("Processing");
                    Message msg = requestQueue.peek();
                    System.out.println("processing " + msg);
                    Thread.sleep(5000);
                    requests.add("c:" + msg.clientId + ",f:" + msg.fileName);
                    System.out.println("Hashset size is: "+requests.size());
                }
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
