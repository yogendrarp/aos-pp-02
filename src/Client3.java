import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class Client3 {
    static String[] servers = new String[]{"localhost:5000", "localhost:5001", "localhost:5002"};
    static ArrayList<String> files;
    static String path = "D:\\Code\\aos-pp-02-ra\\";
    static String citiesFile = "citiestexas.txt";
    static long lamportClockValue = 0;


    public static void main(String[] args) throws IOException {

        files = getHostedFileInformation();

        List<String> cities = Files.readAllLines(Path.of(path + citiesFile));
        for (int i = 0; i < 30; i++) {
            int serverCount = servers.length;
            int filesCount = files.size();
            int randomIndex1 = new Random().nextInt((serverCount));
            int randomIndex2 = new Random().nextInt((filesCount));
            int randomCityIndex = new Random().nextInt(cities.size() - 1);
            String[] tokens = servers[randomIndex1].split(":");
            String randomCity = cities.get(randomCityIndex);
            String server = tokens[0];
            int port = Integer.parseInt(tokens[1]);

            try (Socket socket = new Socket(server, port)) {
                DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String msg = "WRITE#3#" + (++lamportClockValue) + "#" + randomCity + "#" + files.get(randomIndex2);
                dataOutputStream.writeInt(msg.length());
                dataOutputStream.writeBytes(msg);
                Thread.sleep(new Random().nextInt(10) * 1000);
                //System.out.println(in.readLine());
            } catch (UnknownHostException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

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

    private static void updatelamportsClock(long readLong) {
        lamportClockValue++;
        lamportClockValue = Math.max(readLong, lamportClockValue);
    }
}
