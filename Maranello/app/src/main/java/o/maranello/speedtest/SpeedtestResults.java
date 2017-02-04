package o.maranello.speedtest;

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

    private static double round(double value) {
        long factor = (long) Math.pow(10, 2);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }

    public Double getDownloadSpeed() {
        return round(downloadSpeed / 1000);
    }

    public void setDownloadSpeed(Double downloadSpeed) {
        this.downloadSpeed = downloadSpeed;
    }

    public Double getUploadSpeed() {
        return round(uploadSpeed / 1000);
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

    public void setServer(Map<String, String> server) {
        this.server = server;
    }

    public void setBytesSent(Long bytesSent) {
        this.bytesSent = bytesSent;
    }

    public void setBytesReceived(Long bytesReceived) {
        this.bytesReceived = bytesReceived;
    }

    public String dump() {
        String buffer = "";

        buffer += "Using Server: " + this.server.get("name");
        buffer += "Ping: " + this.ping;
        buffer += "Bytes Received: " + this.bytesReceived;
        buffer += "Download Speed: " + this.downloadSpeed;
        buffer += "Bytes Sent: " + this.bytesSent;
        buffer += "Upload Speed: " + this.uploadSpeed;
        return buffer;
    }

}
