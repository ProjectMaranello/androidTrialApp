package o.maranello.speedtest;

import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

/**
 * Created by kristianthornley on 3/12/16.
 * produces threads to test upload speed
 */
class UploadProducer implements Runnable {

    private final BlockingQueue<Map<HTTPUploader, Thread>> queue;
    private final HashMap<HttpURLConnection, HTTPUploaderData> requests;
    private final Integer requestCount;

    public UploadProducer(BlockingQueue<Map<HTTPUploader, Thread>> q, HashMap<HttpURLConnection, HTTPUploaderData> requests, Integer requestCount) {
        this.queue=q;
        this.requests = requests;
        this.requestCount = requestCount;
    }
    @Override
    public void run() {
        Integer currentRequestCount = 0;
        for(Map.Entry<HttpURLConnection,HTTPUploaderData> request : requests.entrySet() ) {
            try {
                HTTPUploader uploader = new HTTPUploader(request.getKey(), request.getValue());
                Thread thread = new Thread(uploader);
                thread.start();
                Map<HTTPUploader, Thread> map = new HashMap<>();
                map.put(uploader, thread);
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