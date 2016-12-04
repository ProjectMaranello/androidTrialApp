package o.maranello.speedtest;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

/**
 * Created by kristianthornley on 28/11/16.
 */
public class HTTPUploader implements Runnable {
    /*
    Thread class for retrieving a URL
     */
    private HttpURLConnection request;
    private HTTPUploaderData data;
    private Long start;
    private Long timeout;
    private Long result = Long.valueOf(0);

    public HTTPUploader(HttpURLConnection request, HTTPUploaderData data, long start, long timeout){
        this.request = request;
        this.start = start;
        this.timeout = timeout;
        this.data = data;
    }

    @Override
    public void run() {

        int current = 0;
        System.out.println("Running");
        SpeedTestUtils.catchRequest(request, data.getData());

        InputStream input = SpeedTestUtils.getResponseStream(request);
        BufferedInputStream buffer = new BufferedInputStream(input);
        byte[] bufferLimit = new byte[10240];
        try {
            System.out.println(request.getResponseCode());
            while ((current = buffer.read(bufferLimit)) != -1) {
                System.out.println(data.getData().length());
                result += Long.valueOf(current);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Finished");
    }

    public Long getResult(){
        return Long.valueOf(data.getData().getBytes().length);
    }
}

