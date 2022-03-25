public class ServerRequests implements Runnable {

    int sleep;

    public ServerRequests(int time) {
        sleep = time;
    }

    @Override
    public void run() {
        try {
            System.out.println("I am a thread and I sleep");
            Thread.sleep(sleep * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
