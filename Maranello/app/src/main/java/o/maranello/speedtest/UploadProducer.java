package o.maranello.speedtest;

import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

/**
 * Created by kristianthornley on 3/12/16.
 * produces threads to test upload speed
 */
public class UploadProducer implements Runnable {

    private BlockingQueue<Map<HTTPUploader,Thread>> queue;
    private HashMap<HttpURLConnection,HTTPUploaderData> requests;
    private Long timeout;
    private Long start;
    private Integer requestCount;

    public UploadProducer(BlockingQueue<Map<HTTPUploader,Thread>> q, HashMap<HttpURLConnection,HTTPUploaderData> requests, Integer requestCount, Long timeout, Long start){
        this.queue=q;
        this.requests = requests;
        this.timeout = timeout;
        this.start = start;
        this.requestCount = requestCount;
    }
    @Override
    public void run() {
        Integer currentRequestCount = 0;
        for(Map.Entry<HttpURLConnection,HTTPUploaderData> request : requests.entrySet() ) {
            try {
                HTTPUploader downloader = new HTTPUploader(request.getKey(), request.getValue(), start, timeout);
                Thread thread = new Thread(downloader);
                thread.start();
                Map<HTTPUploader, Thread> map = new HashMap<>();
                map.put(downloader, thread);
                queue.put(map);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if(currentRequestCount >= requestCount){
                break;
            }else{
                currentRequestCount++;
            }
        }
    }
}