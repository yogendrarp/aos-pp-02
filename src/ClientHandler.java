import java.io.*;
import java.net.Socket;
import java.util.PriorityQueue;
import java.util.Queue;

public class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private final PriorityQueue<Message> msgQueue;
    private final String filesInfo;
    private final LamportsClock lamportsClock;

    public ClientHandler(Socket socket, PriorityQueue<Message> queue, String filesInfo, LamportsClock lamportsClock) {
        this.clientSocket = socket;
        this.msgQueue = queue;
        this.filesInfo = filesInfo;
        this.lamportsClock = lamportsClock;
    }

    public void run() {
        DataOutputStream out = null;
        DataInputStream in = null;
        try {
            out = new DataOutputStream(clientSocket.getOutputStream());
            in = new DataInputStream(clientSocket.getInputStream());
            int length = 0;
            length = in.readInt();
            long clock=in.readLong();
            byte[] line = new byte[length];
            if (length > 0) {
                in.readFully(line);
                System.out.println(new String(line));
                String[] messageTokens = new String(line).split("#");
                if (messageTokens[0].equals("ENQUIRY")) {
                    System.out.println("Enough Written");
                    lamportsClock.clockValue++;
                    out.writeInt(filesInfo.length());
                    out.writeLong(lamportsClock.clockValue);
                    out.writeBytes(filesInfo);
                    System.out.println("Enough Written 2");
                } else if (messageTokens[0].equals("WRITE")) {
                    Message msg = new Message();
                    msg.clientId = Integer.parseInt(messageTokens[1]);
                    msg.timeStamp = Long.parseLong(messageTokens[2]);
                    msg.message = messageTokens[3];
                    msg.fileName = messageTokens[4];
                    System.out.println(msg);
                    msgQueue.add(msg);
                    lamportsClock.clockValue++;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
