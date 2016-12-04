package o.maranello.speedtest;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

/**
 * Created by kristianthornley on 3/12/16.
 */
public class UploadConsumer implements Runnable{

    private BlockingQueue<Map<HTTPUploader,Thread>> queue;
    private ArrayList<Long> finished;
    private Integer requestCount;

    public UploadConsumer(BlockingQueue<Map<HTTPUploader,Thread>> q, Integer requestCount, ArrayList<Long> finished){
        this.queue=q;
        this.finished = finished;
        this.requestCount = requestCount;
    }

    @Override
    public void run() {
        while (finished.size() < requestCount){
            try {
                Map<HTTPUploader,Thread> map = queue.take();
                Map.Entry<HTTPUploader,Thread> entry = map.entrySet().iterator().next();
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