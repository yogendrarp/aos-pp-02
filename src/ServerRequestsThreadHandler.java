import java.util.ArrayList;

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
        ServerRequests serverRequests1 = new ServerRequests(msg, servers.get(idx1), lamportsClock, obtainedLocks, idx1);
        Thread server1 = new Thread(serverRequests1);

        ServerRequests serverRequests2 = new ServerRequests(msg, servers.get(idx2), lamportsClock, obtainedLocks, idx2);
        Thread server2 = new Thread(serverRequests2);
        try {
            server1.start();
            server2.start();
            server1.join();
            server2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
