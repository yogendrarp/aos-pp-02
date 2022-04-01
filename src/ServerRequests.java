import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Random;

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
            boolean flag = true;
            System.out.println(msg);
            if (msg.startsWith("FINALWRITE")) {
                System.out.println("Msg starts with");
                flag = false;
            }
            //while (flag) {
            System.out.println(this.obtainedLocks[idx] + " " + this.obtainedLocks[othIdx]);
            String[] msgs = msg.split("#");
            int time = Integer.parseInt(msgs[1]) * Integer.parseInt(msgs[2]);
            Thread.sleep(time * 100);
            //if (this.obtainedLocks[idx] && this.obtainedLocks[othIdx]) {
            System.out.println("Have obtained both locks");
            String obtainedAlllocks = "OBTAINEDALLLOCKS";
            dataOutputStream.writeInt(obtainedAlllocks.length());
            dataOutputStream.writeLong(lamportClockValue);
            dataOutputStream.writeBytes(obtainedAlllocks);
            // break;
            //}
            // }
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        } finally {
            try {
                dataOutputStream.close();
                in.close();
            } catch (Exception e) {

            }
        }
    }
}
