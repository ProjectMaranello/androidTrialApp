package o.maranello.speedtest;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

/**
 * Created by kristianthornley on 28/11/16.
 */
public class HTTPDownloader implements Runnable {
    /*
    Thread class for retrieving a URL
     */
    private HttpURLConnection request;
    private Long start;
    private Long timeout;
    private Long result = Long.valueOf(0);

    public HTTPDownloader(HttpURLConnection request, long start, long timeout){
        this.request = request;
        this.start = start;
        this.timeout = timeout;
    }

    @Override
    public void run() {

        int current = 0;
        System.out.println("Running");
        SpeedTestUtils.catchRequest(request, null);
        InputStream input = SpeedTestUtils.getResponseStream(request);
        BufferedInputStream buffer = new BufferedInputStream(input);
        byte[] bufferLimit = new byte[10240];
        try {
            while ((current = buffer.read(bufferLimit)) != -1) {

                //System.out.println(current);

                result += Long.valueOf(current);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Finished");
    }

    public Long getResult(){
        return result;
    }

    /*

    def run(self):
        try:
            if (timeit.default_timer() - self.starttime) <= self.timeout:
                f = urlopen(self.request)
                while (not SHUTDOWN_EVENT.isSet() and
                        (timeit.default_timer() - self.starttime) <=
                        self.timeout):
                    self.result.append(len(f.read(10240)))
                    if self.result[-1] == 0:
                        break
                f.close()
        except IOError:
            pass
     */
}
