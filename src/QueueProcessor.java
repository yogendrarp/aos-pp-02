import java.util.ArrayList;
import java.util.HashSet;
import java.util.PriorityQueue;

/**
 * Every File has its own queue, Each queue runs as thread on each server
 */
public class QueueProcessor implements Runnable {
    PriorityQueue<Message> requestQueue;
    String queueName;
    //Processed requests used for message passing
    HashSet<String> requests;
    ArrayList<String> otherServers;
    ArrayList<Boolean> currReq;
    private final boolean[] obtainedLocks = new boolean[]{false, false};
    String fullFilePath;

    public QueueProcessor(PriorityQueue<Message> requestQueue, String queueName, HashSet<String> requests, ArrayList<String> otherServers, ArrayList<Boolean> currReq, String fullFilePath) {
        this.requestQueue = requestQueue;
        this.queueName = queueName;
        this.requests = requests;
        this.otherServers = otherServers;
        this.currReq = currReq;
        this.fullFilePath = fullFilePath;
    }

    public void run() {
        while (true) {
            try {
                if (requestQueue.size() > 0) {
                    Message msg = requestQueue.poll();
                    //Handle Write requests from clients directly
                    if (msg.type.equals("WRITE")) {
                        System.out.println("processing " + msg);
                        String msgString = "SERVER#" + msg.clientId + "#"
                                + (++msg.timeStamp) + "#" + msg.message + "#" + msg.fileName;
                        long lamportsClock = msg.timeStamp;
                        // TODO: 3/23/2022
                        /*
                         * Check other servers if this request can be processed, Mutex
                         * */
                        if (!obtainedLocks[0] || !obtainedLocks[1]) {
                            ServerRequestsThreadHandler serverRequestsThreadHandler = new ServerRequestsThreadHandler(msgString, otherServers, lamportsClock, obtainedLocks);
                            Thread sRqTHThread = new Thread(serverRequestsThreadHandler);
                            sRqTHThread.start();
                            //Write To file
                            System.out.println("**** " + msg);
                            FileWriter.AppendToFile(fullFilePath, msg.clientId + ", " + msg.timeStamp + ", " + msg.message);
                            requests.add("c:" + msg.clientId + ",f:" + msg.fileName + ",t:" + msg.timeStamp);
                        } else if (obtainedLocks[0] && obtainedLocks[1]) {
                            // TODO: 3/28/2022 Write Directly
                            /**
                             * Process msg as FINAL WRITE.. have the locks.. optimization bit, if already the locks are acquired then
                             * Roucairol and Carvalho optimization
                             */
                            System.out.println("______Have both the locks, proceeding to finalwrite_____");
                            msgString = msgString.replace("SERVER", "FINALWRITE");
                            ServerRequestsThreadHandler serverRequestsThreadHandler = new ServerRequestsThreadHandler(msgString, otherServers, lamportsClock, obtainedLocks);
                            //Though you have the locks, you must still inform other servers that you have msg to be written
                            Thread sRqTHThread = new Thread(serverRequestsThreadHandler);
                            sRqTHThread.start();
                            System.out.println("**** " + msg);
                            FileWriter.AppendToFile(fullFilePath, msg.clientId + ", " + msg.timeStamp + ", " + msg.message);
                            requests.add("c:" + msg.clientId + ",f:" + msg.fileName + ",t:" + msg.timeStamp);
                        }
                    }//Handle proxy requests
                    else if (msg.type.equals("SERVER")) {
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
                        FileWriter.AppendToFile(fullFilePath, msg.clientId + ", " + msg.timeStamp + ", " + msg.message);
                    }
                } else {
                    // to avoid java consuming up all resources
                    Thread.sleep(5000);
                }
            } catch (InterruptedException e) {
                System.out.println(e.getLocalizedMessage());
            }
        }
    }
}
