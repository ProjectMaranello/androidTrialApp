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
public class DownloadProducer implements Runnable {

    private BlockingQueue<Map<HTTPDownloader,Thread>> queue;
    private ArrayList<HttpURLConnection> requests;
    private Long timeout = Long.valueOf(0);
    private Long start = Long.valueOf(0);
    public DownloadProducer(BlockingQueue<Map<HTTPDownloader,Thread>> q, ArrayList<HttpURLConnection> requests, Integer requestCount, Long timeout, Long start){
        this.queue=q;
        this.requests = requests;
        this.timeout = timeout;
        this.start = start;
    }
    @Override
    public void run() {

        for(HttpURLConnection request : requests ) {
            try {
                HTTPDownloader downloader = new HTTPDownloader(request, start, timeout);
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