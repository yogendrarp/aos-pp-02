import java.io.*;
import java.net.Socket;
import java.util.Queue;

public class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private final Queue<Message> msgQueue;
    private final String filesInfo;

    public ClientHandler(Socket socket, Queue<Message> queue, String filesInfo) {
        this.clientSocket = socket;
        this.msgQueue = queue;
        this.filesInfo = filesInfo;
    }

    public void run() {
        DataOutputStream out = null;
        DataInputStream in = null;
        try {
            out = new DataOutputStream(clientSocket.getOutputStream());
            in = new DataInputStream(clientSocket.getInputStream());
            System.out.println("Found Client");
            int length = 0;
            length = in.readInt();
            byte[] line = new byte[length];
            if (length > 0) {
                in.readFully(line, 0, length);
                System.out.println(new String(line));
                String[] messageTokens = new String(line).split("#");
                if (messageTokens[0].equals("ENQUIRY")) {
                    System.out.println(filesInfo);
                    out.writeInt(filesInfo.length());
                    out.writeBytes(filesInfo);
                } else if (messageTokens[0].equals("WRITE")) {
                    Message msg = new Message();
                    msg.clientId = Integer.parseInt(messageTokens[1]);
                    msg.timeStamp = Long.parseLong(messageTokens[2]);
                    msg.message = messageTokens[3];
                    msg.fileName = messageTokens[4];
                    msgQueue.add(msg);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
