import java.util.ArrayList;

/**
 * Handle two threads for checking with other servers
 */
public class ServerRequestsThreadHandler implements Runnable {

    String msg;
    ArrayList<String> servers;
    long lamportsClock;
    boolean[] obtainedLocks;


    public ServerRequestsThreadHandler(String msg, ArrayList<String> servers, long lamportsClock, boolean[] obtainedLocks) {
        this.msg = msg;
        this.servers = servers;
        this.lamportsClock = lamportsClock;
        this.obtainedLocks = obtainedLocks;
    }

    @Override
    public void run() {
        int idx1 = 0, idx2 = 1;
        //Create two threads one for each server and infrom it that it has a msg to write, acquire the lock
        ServerRequests serverRequests1 = new ServerRequests(msg, servers.get(idx1), lamportsClock, obtainedLocks, idx1,idx2);
        Thread server1 = new Thread(serverRequests1);

        ServerRequests serverRequests2 = new ServerRequests(msg, servers.get(idx2), lamportsClock, obtainedLocks, idx2,idx1);
        Thread server2 = new Thread(serverRequests2);
        try {
            //Start the process as threads and wait for the them to finish
            server1.start();
            server2.start();
            server1.join();
            server2.join();
            System.out.println("Both the threads finished exec");
        } catch (InterruptedException e) {
            System.out.println(e.getLocalizedMessage());
        }
    }
}
