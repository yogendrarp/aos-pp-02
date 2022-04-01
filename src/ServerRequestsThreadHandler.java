import java.util.ArrayList;

public class ServerRequestsThreadHandler implements Runnable {

    String msg;
    ArrayList<String> servers;
    long lamportsClock;
    boolean[] obtainedLocks;
    boolean[] aborted;
    RequestState state;


    public ServerRequestsThreadHandler(String msg, ArrayList<String> servers, long lamportsClock, boolean[] obtainedLocks, RequestState state) {
        this.msg = msg;
        this.servers = servers;
        this.lamportsClock = lamportsClock;
        this.obtainedLocks = obtainedLocks;
        aborted = new boolean[2];
        this.state = state;
    }

    @Override
    public void run() {
        int idx1 = 0, idx2 = 1;
        aborted[idx1] = false;
        aborted[idx2] = false;
        ServerRequests serverRequests1 = new ServerRequests(msg, servers.get(idx1), lamportsClock, obtainedLocks, idx1, idx2, aborted);
        Thread server1 = new Thread(serverRequests1);

        ServerRequests serverRequests2 = new ServerRequests(msg, servers.get(idx2), lamportsClock, obtainedLocks, idx2, idx1, aborted);
        Thread server2 = new Thread(serverRequests2);
        try {
            server1.start();
            server2.start();
            server1.join();
            server2.join();
            if (aborted[idx1] || aborted[idx2]) {
                state.aborted = true;
                server1.interrupt();
                server2.interrupt();
            }
            System.out.println("Both the threads finished exec");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
