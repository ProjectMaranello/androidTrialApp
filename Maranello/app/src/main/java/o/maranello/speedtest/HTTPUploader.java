package o.maranello.speedtest;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

/**
 * Created by kristianthornley on 28/11/16.
 * The actual uploader
 */
class HTTPUploader implements Runnable {

    //Thread class for retrieving a URL
    private final HttpURLConnection request;
    private final HTTPUploaderData data;
    private Long result = 0L;

    public HTTPUploader(HttpURLConnection request, HTTPUploaderData data) {
        this.request = request;
        this.data = data;
    }

    @Override
    public void run() {
        int current;
        SpeedTestUtils.catchRequest(request, data.getData());
        InputStream input = SpeedTestUtils.getResponseStream(request);
        BufferedInputStream buffer = new BufferedInputStream(input);
        byte[] bufferLimit = new byte[10240];
        try {
            while ((current = buffer.read(bufferLimit)) != -1) {
                result += (long) current;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                buffer.close();
                input.close();
                if (this.request != null) {
                    this.request.disconnect();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public Long getResult(){
        return (long) data.getData().getBytes().length;
    }
}

