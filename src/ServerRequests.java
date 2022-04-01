import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Check with other server
 */
public class ServerRequests implements Runnable {

    String msg;
    String server;
    long lamportClockValue;
    boolean[] obtainedLocks;
    int idx;
    int othIdx;

    public ServerRequests(String msg, String server, long lamportClockValue, boolean[] obtainedLocks, int idx, int othIdx) {
        this.msg = msg;
        this.server = server;
        this.lamportClockValue = lamportClockValue;
        this.obtainedLocks = obtainedLocks;
        this.idx = idx;
        this.othIdx = othIdx;
    }

    @Override
    public void run() {
        String _server = server.split(":")[0];
        int port = Integer.parseInt(server.split(":")[1]);
        DataOutputStream dataOutputStream = null;
        DataInputStream in = null;
        try (Socket socket = new Socket(_server, port)) {
            dataOutputStream = new DataOutputStream(socket.getOutputStream());
            in = new DataInputStream(socket.getInputStream());
            dataOutputStream.writeInt(msg.length());
            dataOutputStream.writeLong(lamportClockValue);
            dataOutputStream.writeBytes(msg);

            while (true) {
                /**
                 *Connect to one of the servers and inform that it has to write
                 * once approved, send the facilitate that it has acquired both the servers permission
                 */
                int length = in.readInt();
                System.out.println("Waiting for the response");
                if (length > 0) {
                    byte[] successMsg = new byte[length];
                    in.readFully(successMsg);
                    System.out.println(new String(successMsg));
                    System.out.println("Obtained lock from" + server + " " + port);
                    this.obtainedLocks[idx] = true;
                    break;
                }
            }
            String[] msgs = msg.split("#");
            int time = Integer.parseInt(msgs[1]) * Integer.parseInt(msgs[2]);
            Thread.sleep(time * 100);
            System.out.println("Have obtained both locks");
            String obtainedAlllocks = "OBTAINEDALLLOCKS";
            dataOutputStream.writeInt(obtainedAlllocks.length());
            dataOutputStream.writeLong(lamportClockValue);
            dataOutputStream.writeBytes(obtainedAlllocks);
        } catch (InterruptedException | IOException e) {
            System.out.println(e.getMessage());
        } finally {
            try {
                dataOutputStream.close();
                in.close();
            } catch (Exception e) {

            }
        }
    }
}
