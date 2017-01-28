package o.maranello.speedtest;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

/**
 * Created by kristianthornley on 3/12/16.
 * runnable download test
 */
public class DownloadConsumer implements Runnable{

    private BlockingQueue<Map<HTTPDownloader,Thread>> queue;
    private ArrayList<Long> finished;
    private Integer requestCount;

    public DownloadConsumer(BlockingQueue<Map<HTTPDownloader,Thread>> q, Integer requestCount, ArrayList<Long> finished){
        this.queue=q;
        this.finished = finished;
        this.requestCount = requestCount;
    }

    @Override
    public void run() {
        while (finished.size() < requestCount){
            try {
                Map<HTTPDownloader,Thread> map = queue.take();
                Map.Entry<HTTPDownloader,Thread> entry = map.entrySet().iterator().next();
                while (entry.getValue().isAlive()){
                    entry.getValue().join(1);
                }
                finished.add(entry.getKey().getResult());

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}