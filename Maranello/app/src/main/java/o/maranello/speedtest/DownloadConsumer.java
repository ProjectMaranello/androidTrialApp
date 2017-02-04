package o.maranello.speedtest;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

/**
 * Created by kristianthornley on 3/12/16.
 * runnable download test
 */
class DownloadConsumer implements Runnable {

    private final BlockingQueue<Map<HTTPDownloader, Thread>> queue;
    private final ArrayList<Long> finished;
    private final Integer requestCount;

    public DownloadConsumer(BlockingQueue<Map<HTTPDownloader,Thread>> q, Integer requestCount, ArrayList<Long> finished){
        this.queue=q;
        this.finished = finished;
        this.requestCount = requestCount;
    }

    @Override
    public void run() {
        while (finished.size() < requestCount){
            Map.Entry<HTTPDownloader, Thread> entry;
            Map<HTTPDownloader, Thread> map = null;
            try {
                map = queue.take();
                entry = map.entrySet().iterator().next();
                while (entry.getValue().isAlive()){
                    entry.getValue().join(1);
                }
                finished.add(entry.getKey().getResult());
                map.clear();
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