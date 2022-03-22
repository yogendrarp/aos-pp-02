import java.io.IOException;
import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.Queue;

public class QueueProcessor implements Runnable {
    PriorityQueue<Message> requestQueue;
    String queueName;

    public QueueProcessor(PriorityQueue<Message> requestQueue, String queueName) {
        this.requestQueue = requestQueue;
        this.queueName = queueName;
    }

    public void run() {
        while (true) {
            try {
                if (requestQueue.size() > 0) {
                    Message msg = requestQueue.peek();
                    System.out.println(msg);
                    msg.dataOutputStream.writeBytes("SUCESS");
                    Thread.sleep(2000);
                }
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
        }
    }
}
