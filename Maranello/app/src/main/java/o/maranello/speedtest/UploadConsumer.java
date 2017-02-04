package o.maranello.speedtest;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

/**
 * Created by kristianthornley on 3/12/16.
 * runnable upload test
 */
class UploadConsumer implements Runnable {

    private final BlockingQueue<Map<HTTPUploader, Thread>> queue;
    private final ArrayList<Long> finished;
    private final Integer requestCount;

    public UploadConsumer(BlockingQueue<Map<HTTPUploader,Thread>> q, Integer requestCount, ArrayList<Long> finished){
        this.queue=q;
        this.finished = finished;
        this.requestCount = requestCount;
    }

    @Override
    public void run() {
        while (finished.size() < requestCount){
            Map.Entry<HTTPUploader, Thread> entry;
            Map<HTTPUploader, Thread> map = null;
            try {
                map = queue.take();
                entry = map.entrySet().iterator().next();
                while (entry.getValue().isAlive()){
                    entry.getValue().join(1);
                }
                finished.add(entry.getKey().getResult());
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                if (map != null) {
                    map.clear();
                }
            }
        }
    }
}