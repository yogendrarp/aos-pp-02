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
    ArrayList<Boolean> currReq;
    private final boolean[] obtainedLocks = new boolean[]{false, false};

    public QueueProcessor(PriorityQueue<Message> requestQueue, String queueName, HashSet<String> requests, ArrayList<String> otherServers, ArrayList<Boolean> currReq) {
        this.requestQueue = requestQueue;
        this.queueName = queueName;
        this.requests = requests;
        this.otherServers = otherServers;
        this.currReq = currReq;
    }

    public void run() {
        while (true) {
            try {
                if (requestQueue.size() > 0) {
                    Message msg = requestQueue.poll();
                    if (msg.type.equals("WRITE")) {
                        System.out.println("processing " + msg);
                        String msgString = "SERVER#" + msg.clientId + "#"
                                + (++msg.timeStamp) + "#" + msg.message + "#" + msg.fileName;
                        long lamportsClock = msg.timeStamp;
                        // TODO: 3/23/2022
                        /*
                         * Check other servers if this request can be processed
                         * */
                        if (!obtainedLocks[0] || !obtainedLocks[1]) {
                            ServerRequestsThreadHandler serverRequestsThreadHandler = new ServerRequestsThreadHandler(msgString, otherServers, lamportsClock, obtainedLocks);
                            Thread sRqTHThread = new Thread(serverRequestsThreadHandler);
                            sRqTHThread.start();
                            sRqTHThread.join();
                            //Write To file
                            requests.add("c:" + msg.clientId + ",f:" + msg.fileName + ",t:" + msg.timeStamp);
                        } else if (obtainedLocks[0] && obtainedLocks[1]) {
                            // TODO: 3/28/2022 Write Directly
                            //Process msg as FINAL WRITE.. have the locks.. optimization bit
                            System.out.println("______Have both the locks, proceeding to finalwrite_____");
                            msgString = msgString.replace("SERVER", "FINALWRITE");
                            ServerRequestsThreadHandler serverRequestsThreadHandler = new ServerRequestsThreadHandler(msgString, otherServers, lamportsClock, obtainedLocks);
                            Thread sRqTHThread = new Thread(serverRequestsThreadHandler);
                            sRqTHThread.start();
                            sRqTHThread.join();
                            System.out.println("**** " + msg);
                            requests.add("c:" + msg.clientId + ",f:" + msg.fileName + ",t:" + msg.timeStamp);
                        }
                    } else if (msg.type.equals("SERVER")) {
                        System.out.println("processing " + msg);
                        obtainedLocks[0] = false;
                        obtainedLocks[1] = false;
                        boolean flag = true;
                        String _reqMsg = "c:" + msg.clientId + ",f:" + msg.fileName + ",t:" + msg.timeStamp;
                        requests.add(_reqMsg);
                        while (flag) {
                            flag = requests.contains(_reqMsg);
                            //Wait Till the Request Has been removed, queue cannot proceed further after handing over the lock.
                        }
                        //Process msg as final write... now you dont have locks.. but you can only write once.
                        System.out.println("**** " + msg);
                    }
                }
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
