public class Message {
    int clientId;
    String message;
    long timeStamp;
    String fileName;

    @Override
    public String toString() {
        return "Message{" +
                "clientId=" + clientId +
                ", message='" + message + '\'' +
                ", timeStamp=" + timeStamp +
                ", fileName='" + fileName + '\'' +
                '}';
    }
}
