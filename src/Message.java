/**
 * Class to hold the message that flows between queue and clienthandlers
 */
public class Message {
    int clientId;
    String message;
    long timeStamp;
    String fileName;
    String type;

    @Override
    public String toString() {
        return "Message{" +
                "clientId=" + clientId +
                ", message='" + message + '\'' +
                ", timeStamp=" + timeStamp +
                ", fileName='" + fileName + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}
