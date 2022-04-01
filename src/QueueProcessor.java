import java.io.IOException;
import java.util.*;

public class QueueProcessor implements Runnable {
    PriorityQueue<Message> requestQueue;
    String queueName;
    HashMap<String, Boolean> requests;
    ArrayList<String> otherServers;
    ArrayList<Boolean> currReq;
    private final boolean[] obtainedLocks = new boolean[]{false, false};
    String fullFilePath;

    public QueueProcessor(PriorityQueue<Message> requestQueue, String queueName, HashMap<String, Boolean> requests, ArrayList<String> otherServers, ArrayList<Boolean> currReq, String fullFilePath) {
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
                    if (msg.type.equals("WRITE")) {
                        System.out.println("processing " + msg);
                        String msgString = "SERVER#" + msg.clientId + "#"
                                + (++msg.timeStamp) + "#" + msg.message + "#" + msg.fileName;
                        long lamportsClock = msg.timeStamp;
                        // TODO: 3/23/2022
                        /*
                         * Check other servers if this request can be processed
                         * */

                        if (obtainedLocks[0] && obtainedLocks[1]) {
                            // TODO: 3/28/2022 Write Directly
                            //Process msg as FINAL WRITE.. have the locks.. optimization bit
                            System.out.println("______Have both the locks, proceeding to finalwrite_____");
                            msgString = msgString.replace("SERVER", "FINALWRITE");
                            RequestState state = new RequestState();
                            state.aborted = false;
                            ServerRequestsThreadHandler serverRequestsThreadHandler = new ServerRequestsThreadHandler(msgString, otherServers, lamportsClock, obtainedLocks, state);
                            Thread sRqTHThread = new Thread(serverRequestsThreadHandler);
                            sRqTHThread.start();
                            sRqTHThread.join();
                            if (state.aborted) {
                                requestQueue.add(msg);
                            } else {
                                System.out.println("**** " + msg);
                                FileWriter.AppendToFile(fullFilePath, msg.clientId + ", " + msg.timeStamp + ", " + msg.message);
                                requests.put("c:" + msg.clientId + ",f:" + msg.fileName + ",t:" + msg.timeStamp, true);
                            }
                        } else {// if (!obtainedLocks[0] || !obtainedLocks[1]) {
                            RequestState state = new RequestState();
                            state.aborted = false;
                            ServerRequestsThreadHandler serverRequestsThreadHandler = new ServerRequestsThreadHandler(msgString, otherServers, lamportsClock, obtainedLocks, state);
                            Thread sRqTHThread = new Thread(serverRequestsThreadHandler);
                            sRqTHThread.start();
                            sRqTHThread.join();
                            if (state.aborted) {
                                requestQueue.add(msg);
                            } else {
                                //Write To file
                                System.out.println("**** " + msg);
                                FileWriter.AppendToFile(fullFilePath, msg.clientId + ", " + msg.timeStamp + ", " + msg.message);
                                requests.put("c:" + msg.clientId + ",f:" + msg.fileName + ",t:" + msg.timeStamp, true);
                            }
                        }
                    } else if (msg.type.equals("SERVER")) {
                        System.out.println("processing " + msg);

                        obtainedLocks[0] = false;
                        obtainedLocks[1] = false;
                        boolean flag = true;
                        String _reqMsg = "c:" + msg.clientId + ",f:" + msg.fileName + ",t:" + msg.timeStamp;
                        requests.put(_reqMsg, false);
                        while (flag) {
                            flag = !requests.get(_reqMsg);
                            //Wait Till the Request Has been removed, queue cannot proceed further after handing over the lock.
                        }
                        if (requests.get(_reqMsg)) {
                            //Process msg as final write... now you dont have locks.. but you can only write once.
                            Thread.sleep(2000);
                            System.out.println("**** " + msg);
                            FileWriter.AppendToFile(fullFilePath, msg.clientId + ", " + msg.timeStamp + ", " + msg.message);
                        }
                        else{
                            requests.remove(_reqMsg);
                        }
                    }
                }
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
