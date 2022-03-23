import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Queue;

public class QueueProcessor implements Runnable {
    PriorityQueue<Message> requestQueue;
    String queueName;
    HashSet<String> requests;
    ArrayList<String> otherServers;

    public QueueProcessor(PriorityQueue<Message> requestQueue, String queueName, HashSet<String> requests, ArrayList<String> otherServers) {
        this.requestQueue = requestQueue;
        this.queueName = queueName;
        this.requests = requests;
        this.otherServers = otherServers;
    }

    public void run() {
        while (true) {
            try {
                System.out.println(queueName + " size : " + requestQueue.size());
                if (requestQueue.size() > 0) {
                    Message msg = requestQueue.peek();
                    System.out.println("processing " + msg);
                    String msgString = "WRITE#" + msg.clientId + "#"
                            + (++msg.timeStamp) + "#" + msg.message + "#" + msg.fileName;

                    // TODO: 3/23/2022
                    /*
                     * Check other servers if this request can be processed
                     * */

                    Thread.sleep(5000);
                    requests.add("c:" + msg.clientId + ",f:" + msg.fileName + ",t:" + msg.timeStamp);
                    System.out.println("Hashset size is: " + requests.size());
                }
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
