package o.maranello.speedtest;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

/**
 * Created by kristianthornley on 3/12/16.
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
                Map<HTTPUploader,Thread> map = new HashMap<HTTPUploader,Thread>();
                map.put(downloader, thread);
                queue.put(map);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if(currentRequestCount >= requestCount){
                System.out.println("Early Exit");
                break;
            }else{
                currentRequestCount++;
            }
        }

    }

}
/**
 def producer(q, requests, request_count):
 for i, request in enumerate(requests[:request_count]):
 thread = HTTPUploader(i, request[0], start, request[1],
 self.config['length']['upload'])
 thread.start()
 q.put(thread, True)
 callback(i, request_count, start=True)
 */