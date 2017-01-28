package o.maranello.speedtest;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by kristianthornley on 28/11/16.
 * Class for holding the results of a speedtest, including:
 */
public class SpeedtestResults {
    private Double downloadSpeed;
    private Double uploadSpeed;
    private Long ping;
    private Map<String,String> server;
    private Long bytesSent;
    private Long bytesReceived;


    public Double getDownloadSpeed() {
        return round(downloadSpeed/1000, 2);
    }

    public void setDownloadSpeed(Double downloadSpeed) {
        this.downloadSpeed = downloadSpeed;
    }

    public Double getUploadSpeed() {
        return round(uploadSpeed/1000,2);
    }

    public void setUploadSpeed(Double uploadSpeed) {
        this.uploadSpeed = uploadSpeed;
    }

    public Long getPing() {
        return ping;
    }

    public void setPing(Long ping) {
        this.ping = ping;
    }

    public Map<String, String> getServer() {
        return server;
    }

    public void setServer(Map<String, String> server) {
        this.server = server;
    }

    public Long getBytesSent() {
        return bytesSent;
    }

    public void setBytesSent(Long bytesSent) {
        this.bytesSent = bytesSent;
    }

    public Long getBytesReceived() {
        return bytesReceived;
    }

    public void setBytesReceived(Long bytesReceived) {
        this.bytesReceived = bytesReceived;
    }

    public void dump(){

        System.out.println("Using Server: " + this.server.get("name"));
        System.out.println("Ping: " + this.ping);
        System.out.println("Bytes Received: " + this.bytesReceived);
        System.out.println("Download Speed: " + this.downloadSpeed);
        System.out.println("Bytes Sent: " + this.bytesSent);
        System.out.println("Upload Speed: " + this.uploadSpeed);

    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }


}
