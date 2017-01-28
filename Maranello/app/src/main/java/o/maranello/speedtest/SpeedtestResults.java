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

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }

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

    public String dump() {
        StringBuilder buffer = new StringBuilder();

        buffer.append("Using Server: " + this.server.get("name"));
        buffer.append("Ping: " + this.ping);
        buffer.append("Bytes Received: " + this.bytesReceived);
        buffer.append("Download Speed: " + this.downloadSpeed);
        buffer.append("Bytes Sent: " + this.bytesSent);
        buffer.append("Upload Speed: " + this.uploadSpeed);
        return buffer.toString();
    }


}
