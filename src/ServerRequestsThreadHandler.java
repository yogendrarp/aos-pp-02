import java.util.ArrayList;

public class ServerRequestsThreadHandler implements Runnable {

    String msg;
    ArrayList<String> servers;

    public ServerRequestsThreadHandler(String msg, ArrayList<String> servers) {
        this.msg = msg;
        this.servers = servers;
    }

    @Override
    public void run() {
        ServerRequests serverRequests1 = new ServerRequests(10);
        Thread server1 = new Thread(serverRequests1);

        ServerRequests serverRequests2 = new ServerRequests(5);
        Thread server2 = new Thread(serverRequests2);
        try {
            server1.join();
            server2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
