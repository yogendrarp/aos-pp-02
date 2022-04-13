import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Client code for connecting to the servers on sockets
 */
public class Client3 {
    static String[] servers = new String[]{"dc01.utdallas.edu:5000", "dc02.utdallas.edu:5001", "dc03.utdallas.edu:5002"};
    static ArrayList<String> files;
    static String path = "/home/012/y/yr/yrp200001/aospp2/";
    static String citiesFile = "citiestexas.txt";
    static long lamportClockValue = 0;


    public static void main(String[] args) throws IOException, InterruptedException {

        files = getHostedFileInformation();

        List<String> cities = Files.readAllLines(Paths.get(path + citiesFile));
        /*
         * Each client creates 30 requests randomly to the server to a random file
         * */
        for (int i = 0; i < 30; i++) {
            int serverCount = servers.length;
            int filesCount = files.size();
            /*
             * Random pickings, pick random server, random file and a random city name to append so that the file
             * reads can be done easily
             * */
            int randomIndex1 = new Random().nextInt((serverCount));
            int randomIndex2 = new Random().nextInt((filesCount));
            int randomCityIndex = new Random().nextInt(cities.size() - 1);
            String[] tokens = servers[randomIndex1].split(":");
            String randomCity = cities.get(randomCityIndex);
            String server = tokens[0];
            int port = Integer.parseInt(tokens[1]);

            /*
             * Socket connection
             * */
            try (Socket socket = new Socket(server, port)) {
                DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
                DataInputStream in = new DataInputStream(socket.getInputStream());
                // Lamports clock for internal clock sync, d=1
                //WRITE#1 1-> indicates client id
                int clientId = 3;
                String msg = "WRITE#" + clientId + "#" + (++lamportClockValue) + "#" + randomCity + "#" + files.get(randomIndex2);
                System.out.println("Sending " + msg + " to " + server + ":" + port);
                dataOutputStream.writeInt(msg.length());
                dataOutputStream.writeLong(lamportClockValue);
                dataOutputStream.writeBytes(msg);
                int count = 0;
                //Inorder to avoid any overutilization of resources, never hits though
                while (count < 10) {
                    int length = in.readInt();
                    if (length > 0) {
                        byte[] successMsg = new byte[length];
                        in.readFully(successMsg);
                        System.out.println(new String(successMsg));
                        break;
                    }
                    Thread.sleep(1000);
                    count++;
                }
            } catch (UnknownHostException | InterruptedException e) {
                e.printStackTrace();
            }
            //Sleep random milliseconds, to improve readablity of Sys outs, else too quick ops, difficult to follow and debug
            Thread.sleep(new Random().nextInt(15) * 100);
        }
    }

    /*
     * Initial enquiry, get hosted file information from a random server
     * */
    private static ArrayList<String> getHostedFileInformation() throws IOException {
        int serverCount = servers.length;
        int randomIndex1 = new Random().nextInt((serverCount));
        String[] tokens = servers[randomIndex1].split(":");
        String server = tokens[0];
        int port = Integer.parseInt(tokens[1]);
        Socket socket = new Socket(server, port);
        DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
        DataInputStream in = new DataInputStream(socket.getInputStream());
        String enquiry = "ENQUIRY";
        dataOutputStream.writeInt(enquiry.length());
        dataOutputStream.writeLong(lamportClockValue);
        dataOutputStream.writeBytes(enquiry);
        while (true) {
            int length = in.readInt();
            updatelamportsClock(in.readLong());
            if (length > 0) {
                byte[] msg = new byte[length];
                in.readFully(msg);
                String filesInfo = new String(msg);
                return new ArrayList<String>(Arrays.asList(filesInfo.split(",")));
            }
        }
    }

    //Lamports clock is updated with curr+1 and clock from server
    private static void updatelamportsClock(long readLong) {
        lamportClockValue++;
        lamportClockValue = Math.max(readLong, lamportClockValue);
    }
}
