import java.util.Queue;

public class QueueProcessor implements Runnable {
    Queue<Message> requestQueue;

    public QueueProcessor(Queue<Message> requestQueue) {
        this.requestQueue = requestQueue;
    }

    public void run() {
        while (true) {
            System.out.println("Size is : " + requestQueue.size());
        }
    }
}
