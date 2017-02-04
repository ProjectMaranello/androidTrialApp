package o.maranello.speedtest;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

/**
 * Created by kristianthornley on 3/12/16.
 * produces threads to test download speed
 */
class DownloadProducer implements Runnable {

    private final BlockingQueue<Map<HTTPDownloader, Thread>> queue;
    private final ArrayList<HttpURLConnection> requests;

    public DownloadProducer(BlockingQueue<Map<HTTPDownloader, Thread>> q, ArrayList<HttpURLConnection> requests) {
        this.queue=q;
        this.requests = requests;
    }
    @Override
    public void run() {

        for(HttpURLConnection request : requests ) {
            try {
                HTTPDownloader downloader = new HTTPDownloader(request);
                Thread thread = new Thread(downloader);
                thread.start();
                Map<HTTPDownloader, Thread> map = new HashMap<>();
                map.put(downloader, thread);
                queue.put(map);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}