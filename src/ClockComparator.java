import java.util.Comparator;

/**
 * Lamports clock comparator, compare the timestamps, if the timestamps are equal
 * then compare the client Id, used in priority queue
 */
public class ClockComparator implements Comparator<Message> {
    @Override
    public int compare(Message m1, Message m2) {
        if (m1.timeStamp < m2.timeStamp) {
            return -1;
        }
        if (m1.timeStamp > m2.timeStamp) {
            return 1;
        }
        if (m1.clientId < m2.clientId) {
            return -1;
        }
        if (m1.clientId > m2.clientId) {
            return 1;
        }
        return 0;
    }
}
