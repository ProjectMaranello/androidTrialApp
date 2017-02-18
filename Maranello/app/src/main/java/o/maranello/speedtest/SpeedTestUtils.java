package o.maranello.speedtest;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/**
 * Created by kristianthornley on 30/11/16.
 * Utils for handling speedtest functions
 */
class SpeedTestUtils {

    public static HttpURLConnection buildRequest(String urlString, String urlParameters) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(1000);
            System.setProperty("http.keepAlive", "false");
            connection.setRequestProperty("Connection", "close");
            connection.setRequestProperty("Accept-Encoding", "gzip");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("User-Agent","Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_3) AppleWebKit/537.75.14 (KHTML, like Gecko) Version/7.0.3 Safari/7046A194A");
            connection.setRequestProperty("Cache-Control", "no-cache");
            if(urlParameters != null) {
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Length", Integer.toString(urlParameters.getBytes().length));
            }else{
                connection.setRequestMethod("GET");
            }
            connection.setRequestProperty("Content-Language", "en-US");
            connection.setUseCaches(false);
            connection.setDoOutput(true);
        } catch (IOException e){
            e.printStackTrace();
        }
        return connection;
    }

    public static Map<String, String> nodeListToMap(NodeList nodeList){
        HashMap<String, String> map = new HashMap<>();
        for (int i = 0 ; i < nodeList.getLength(); i++){
            Node node = nodeList.item(i);
            map.put(node.getNodeName(),node.getNodeValue());
        }
        return map;
    }

    public static HttpURLConnection catchRequest(HttpURLConnection connection, String urlParameters){
        DataOutputStream output = null;
        try {
            output = new DataOutputStream(connection.getOutputStream());
            if(urlParameters != null) {
                output.writeBytes(urlParameters);
            }

        } catch (IOException e){
            e.printStackTrace();
        } finally {
            try {
                if (output != null) {
                    output.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return connection;
    }

    public static InputStream getResponseStream(HttpURLConnection connection){
        InputStream input = null;
        try {
            connection.connect();
            if("gzip".equalsIgnoreCase(connection.getHeaderField("Content-Encoding"))) {
                input = new GZIPInputStream(connection.getInputStream());
            }else {
                input = connection.getInputStream();
            }
        } catch (IOException e){
            e.printStackTrace();
        }
        return input;
    }

    public static Double distance(Double originLat, Double originLon, Double destinationLat, Double destinationLon) {
        //Determine distance between 2 sets of [lat,lon] in km

        Integer radius = 6371;  // km

        Double dlat = Math.toRadians(destinationLat - originLat);
        Double dlon = Math.toRadians(destinationLon - originLon);
        Double a = (
                Math.sin(dlat / 2) * Math.sin(dlat / 2) +
                        Math.cos(Math.toRadians(originLat)) *
                                Math.cos(Math.toRadians(destinationLat)) *
                                Math.sin(dlon / 2) * Math.sin(dlon / 2)
        );

        Double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return radius * c;

    }
}