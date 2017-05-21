package o.maranello.speedtest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by kristianthornley on 21/05/17.
 */

public class Config {


    private Integer threadsDownload = 8;
    private Integer iterationsDownload = 4;
    private Integer[] sizesDownload = {350, 500, 750, 1000, 1500, 2000, 2500, 3000, 3500, 4000};
    private Map<String, String> client;
    private List<String> ignore_servers;
    private Integer threadsUpload = 8;
    private Integer iterationsUpload = 4;
    private Integer[] sizesUpload = {32768, 32768, 32768, 32768, 32768, 131072, 262144};
    private HashMap<String, Map<String, String>> servers;
    private static Config ourInstance = new Config();

    public static synchronized Config getInstance() {
        return ourInstance;
    }

    public Integer getIterationsDownload() {
        return iterationsDownload;
    }

    public void setIterationsDownload(Integer iterationsDownload) {
        this.iterationsDownload = iterationsDownload;
    }

    public Integer[] getSizesDownload() {
        return sizesDownload;
    }

    public void setSizesDownload(Integer[] sizesDownload) {
        this.sizesDownload = sizesDownload;
    }

    public Integer getThreadsUpload() {
        return threadsUpload;
    }

    public void setThreadsUpload(Integer threadsUpload) {
        this.threadsUpload = threadsUpload;
    }

    public Integer getIterationsUpload() {
        return iterationsUpload;
    }

    public void setIterationsUpload(Integer iterationsUpload) {
        this.iterationsUpload = iterationsUpload;
    }

    public Integer[] getSizesUpload() {
        return sizesUpload;
    }

    public void setSizesUpload(Integer[] sizesUpload) {
        this.sizesUpload = sizesUpload;
    }



    public Integer getThreadsDownload() {
        return threadsDownload;
    }

    public void setThreadsDownload(Integer threadsDownload) {
        this.threadsDownload = threadsDownload;
    }

    public List<String> getIgnore_servers() {
        return ignore_servers;
    }

    public void setIgnore_servers(List<String> ignore_servers) {
        this.ignore_servers = ignore_servers;
    }

    public Map<String, String> getClient() {
        return client;
    }

    public void setClient(Map<String, String> client) {
        this.client = client;
    }

    public HashMap<String, Map<String, String>> getServers() {
        return servers;
    }

    public void setServers(HashMap<String, Map<String, String>> servers) {
        this.servers = servers;
    }
}
