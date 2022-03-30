import java.io.*;
import java.net.Socket;
import java.util.*;

public class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private final ArrayList<PriorityQueue<Message>> requestQueues;
    private final String filesInfo;
    private final LamportsClock lamportsClock;
    private final HashSet<String> requests;
    private final String lockStatus = "HOLD";

    public ClientHandler(Socket socket, ArrayList<PriorityQueue<Message>> queue, String filesInfo, LamportsClock lamportsClock, HashSet<String> requests) {
        this.clientSocket = socket;
        this.requestQueues = queue;
        this.filesInfo = filesInfo;
        this.lamportsClock = lamportsClock;
        this.requests = requests;
    }

    public void run() {
        DataOutputStream out = null;
        DataInputStream in = null;
        try {
            out = new DataOutputStream(clientSocket.getOutputStream());
            in = new DataInputStream(clientSocket.getInputStream());
            int length = 0;
            length = in.readInt();
            long clock = in.readLong();
            byte[] line = new byte[length];
            if (length > 0) {
                in.readFully(line);
                System.out.println(new String(line));
                String[] messageTokens = new String(line).split("#");
                if (messageTokens[0].equals("ENQUIRY")) {
                    lamportsClock.clockValue++;
                    out.writeInt(filesInfo.length());
                    out.writeLong(lamportsClock.clockValue);
                    out.writeBytes(filesInfo);
                } else if (messageTokens[0].equals("WRITE")) {
                    Message msg = new Message();
                    msg.type = "WRITE";
                    msg.clientId = Integer.parseInt(messageTokens[1]);
                    msg.timeStamp = Long.parseLong(messageTokens[2]);
                    msg.message = messageTokens[3];
                    msg.fileName = messageTokens[4];
                    int idx = getIndexOfFile(msg.fileName, filesInfo);
                    requestQueues.get(idx).add(msg);
                    System.out.println("Received msg from " + msg);
                    lamportsClock.clockValue++;
                    boolean flag = true;
                    while (flag) {
                        boolean containsData = requests.contains("c:" + msg.clientId + ",f:" + msg.fileName + ",t:" + msg.timeStamp);
                        if (containsData) {
                            flag = false;
                            requests.remove("c:" + msg.clientId + ",f:" + msg.fileName + ",t:" + msg.timeStamp);
                        }
                    }
                    System.out.println("Coming out now, its processed");
                    String successMsg = "SUCCESS";
                    out.writeInt(successMsg.length());
                    out.writeBytes(successMsg);
                } else if (messageTokens[0].equals("SERVER")) {
                    Message msg = new Message();
                    msg.type = "SERVER";
                    msg.clientId = Integer.parseInt(messageTokens[1]);
                    msg.timeStamp = Long.parseLong(messageTokens[2]);
                    msg.message = messageTokens[3];
                    msg.fileName = messageTokens[4];
                    int idx = getIndexOfFile(msg.fileName, filesInfo);
                    requestQueues.get(idx).add(msg);
                    lamportsClock.clockValue++;
                    boolean flag = true;
                    while (flag) {
                        boolean containsData = requests.contains("c:" + msg.clientId + ",f:" + msg.fileName + ",t:" + msg.timeStamp);
                        if (containsData) {
                            //Send request back to the stream and enquire if it obtained a lock from other server and then write to file.
                            System.out.println("Other Server request can be processed, handing over the lock");
                            String successMsg = "LOCK";
                            out.writeInt(successMsg.length());
                            out.writeBytes(successMsg);
                            while (true) {
                                length = 0;
                                length = in.readInt();
                                long lcClock = in.readLong();
                                if (length > 0) {
                                    byte[] successmsg = new byte[length];
                                    in.readFully(successmsg);
                                    System.out.println(new String(successmsg));
                                    break;
                                }
                            }
                            requests.remove("c:" + msg.clientId + ",f:" + msg.fileName + ",t:" + msg.timeStamp);
                        }
                    }
                } else if (messageTokens[0].equals("FINALWRITE")) {
                    Message msg = new Message();
                    msg.type = "FINALWRITE";
                    msg.clientId = Integer.parseInt(messageTokens[1]);
                    msg.timeStamp = Long.parseLong(messageTokens[2]);
                    msg.message = messageTokens[3];
                    msg.fileName = messageTokens[4];
                    /*int idx = getIndexOfFile(msg.fileName, filesInfo);
                    requestQueues.get(idx).add(msg);
                    lamportsClock.clockValue++;
                    boolean flag = true;
                    while (flag) {
                        boolean containsData = requests.contains("c:" + msg.clientId + ",f:" + msg.fileName + ",t:" + msg.timeStamp);
                        if (containsData) {
                            flag = false;
                            requests.remove("c:" + msg.clientId + ",f:" + msg.fileName + ",t:" + msg.timeStamp);
                        }
                    }*/
                    System.out.println("Other Server request has been processed");
                    System.out.println("**** " + msg);
                    String successMsg = "WRITTEN_ACK";
                    out.writeInt(successMsg.length());
                    //out.writeLong(++lamportsClock.clockValue);
                    out.writeBytes(successMsg);
                    System.out.println("Wrote everything, I am done here");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int getIndexOfFile(String fileName, String filesInfo) {
        String[] split = filesInfo.split(",");
        int index = 0;
        for (String s : split) {
            if (s.equalsIgnoreCase(fileName)) {
                return index;
            }
            index++;
        }
        return -1;
    }
}
