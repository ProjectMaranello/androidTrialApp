package o.maranello.speedtest;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by kristianthornley on 18/02/17.
 */
public class ConfigHelper {
    private static ConfigHelper ourInstance = new ConfigHelper();


    private HashMap<String, Object> config;
    private HashMap<String, Map<String, String>> servers;

    private ConfigHelper() {
    }

    public static ConfigHelper getInstance() {
        return ourInstance;
    }

    public HashMap<String, Map<String, String>> getServers() {
        return servers;
    }

    public void setServers(HashMap<String, Map<String, String>> servers) {
        this.servers = servers;
    }

    public HashMap<String, Object> getConfig() {
        return config;
    }

    public void setConfig(HashMap<String, Object> config) {
        this.config = config;
    }
}
