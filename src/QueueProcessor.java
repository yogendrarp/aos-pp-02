import java.util.Queue;

public class QueueProcessor implements Runnable {
    Queue<Message> requestQueue;

    public QueueProcessor(Queue<Message> requestQueue) {
        this.requestQueue = requestQueue;
    }

    public void run() {
        int size = requestQueue.size();
        while (true) {
            if (requestQueue.size() > size) {
                System.out.println("Size is : " + requestQueue.size());
                size = requestQueue.size();
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
