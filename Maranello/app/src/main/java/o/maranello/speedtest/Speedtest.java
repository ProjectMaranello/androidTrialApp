package o.maranello.speedtest;

import android.content.Context;
import android.util.Log;

import com.loopj.android.http.BlackholeHttpResponseHandler;

import org.apache.commons.io.FilenameUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;
import o.maranello.clients.SlackClient;

/**
 * Created by kristianthornley on 28/11/16.
 * Speedtest port of python speedtest-cli
 */
public class Speedtest {
    private static final String TAG = "Speedtest";
    private final SpeedtestResults results;
    private HashMap<String,Object> config;
    private HashMap<String, Map<String, String>> servers;
    private HashMap<String,Map<String,String>> closest;
    private Map<String,String> best;
    private Double lat;
    private Double lon;
    private Context context;
    private String device;

    public Speedtest(String device, Context context) {
        this.device = device;
        this.context = context;
        results = new SpeedtestResults();
    }

    public void destroy() {
        if (this.config != null) {
            this.config.clear();
        }
        if (this.servers != null) {
            this.servers.clear();
        }
        if (this.closest != null) {
            this.closest.clear();
        }
        if (this.best != null) {
            this.best.clear();
        }
        this.context = null;
        this.device = null;
    }
    /**
     * Download the speedtest.net configuration and return only the data
     * we are interested in
     */
    private void getConfig() throws SpeedTestException {
        Log.i(TAG, "Entry: getConfig");
        config = new HashMap<>();
        HttpURLConnection connection = SpeedTestUtils.buildRequest("http://www.speedtest.net/speedtest-config.php", null);
        connection = SpeedTestUtils.catchRequest(connection, null);
        InputStream response = SpeedTestUtils.getResponseStream(connection);
        BufferedReader reader = new BufferedReader(new InputStreamReader(response));
        String line;
        StringBuilder buffer = new StringBuilder();
        try {
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }
            reader.close();
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = builderFactory.newDocumentBuilder();
            Document document = builder.parse(new InputSource(new StringReader(buffer.toString())));
            XPath path =  XPathFactory.newInstance().newXPath();


            Map<String, String> server_config  = SpeedTestUtils.nodeListToMap((NodeList) path.compile("/settings/server-config/@*").evaluate(document, XPathConstants.NODESET));
            Map<String, String> download  = SpeedTestUtils.nodeListToMap((NodeList) path.compile("/settings/download/@*").evaluate(document, XPathConstants.NODESET));
            Map<String, String> upload  = SpeedTestUtils.nodeListToMap((NodeList) path.compile("/settings/upload/@*").evaluate(document, XPathConstants.NODESET));
            Map<String, String> client  = SpeedTestUtils.nodeListToMap((NodeList) path.compile("/settings/client/@*").evaluate(document, XPathConstants.NODESET));

            List<String> ignore_servers = Arrays.asList(server_config.get("ignoreids").split(","));

            Integer ratio = Integer.valueOf(upload.get("ratio"));
            //System.out.println("Upload Ratio:" + ratio);
            Integer upload_max = Integer.valueOf(upload.get("maxchunkcount"));
            //Integer[] up_sizes = {32768, 65536, 131072, 262144, 524288, 1048576, 7340032};
            //Tweeking this will produce more accurate results but also send more data. original above
            Integer[] up_sizes = {32768, 32768, 32768, 32768, 32768, 131072, 262144};

            Log.i(TAG, "Upload Sizes:" + up_sizes.length);
            Integer[] sizesUpload = new Integer[up_sizes.length - ratio +1];
            System.arraycopy(up_sizes, ratio-1, sizesUpload, 0, up_sizes.length - ratio +1);
            Integer countsUpload = upload_max * 2 / sizesUpload.length;
            Integer[] sizesDownload = {350, 500, 750, 1000, 1500, 2000, 2500, 3000, 3500, 4000};
            Integer countsDownload = Integer.valueOf(download.get("threadsperurl"));
            Integer threadsUpload = Integer.valueOf(upload.get("threads"));
            Integer threadsDownload = Integer.valueOf(download.get("threadsperurl")) *2;
            Integer lengthUpload = Integer.valueOf(upload.get("testlength"));
            Integer lengthDownload = Integer.valueOf(download.get("testlength"));

            Log.i(TAG, "Count Download:" + countsDownload);


            config.put("client",client);
            config.put("ignore_servers",ignore_servers);
            config.put("sizesUpload",sizesUpload);
            config.put("sizesDownload",sizesDownload);
            config.put("countsUpload",countsUpload);
            config.put("countsDownload",countsDownload);
            config.put("threadsUpload",threadsUpload);
            config.put("threadsDownload",threadsDownload);
            config.put("lengthUpload",lengthUpload);
            config.put("lengthDownload",lengthDownload);
            config.put("uploadMax",upload_max);

            lat = Double.valueOf(client.get("lat"));
            lon = Double.valueOf(client.get("lon"));

        } catch (IOException e){
            Log.e(TAG, "IOException");
            throw new SpeedTestException("GET_CONFIG", "Get Config IOException");
        } catch (ParserConfigurationException e) {
            Log.e(TAG, "ParserConfigurationException");
            throw new SpeedTestException("GET_CONFIG", "Get Config ParserConfigurationException");
        } catch (SAXException e) {
            Log.e(TAG, "SAXException");
            throw new SpeedTestException("GET_CONFIG", "Get Config SAXException");
        } catch (XPathExpressionException e) {
            Log.e(TAG, "XPathExpressionException");
            throw new SpeedTestException("GET_CONFIG", "Get Config XPathExpressionException");
        }
        Log.i(TAG, "Exit: getConfig");
    }

    /**
     * Retrieve a the list of speedtest.net servers, optionally filtered
     * to servers matching those specified in the ``servers`` argument
     */
    public void getServers() throws SpeedTestException {
        Log.i(TAG, "Entry: getServers");
        String[] urls = {"http://www.speedtest.net/speedtest-servers-static.php",
                "http://c.speedtest.net/speedtest-servers-static.php",
                "http://www.speedtest.net/speedtest-servers.php",
                "http://c.speedtest.net/speedtest-servers.php"};
        if (servers == null) {
            servers = new HashMap<>();
        }
        for (String url : urls) {
            HttpURLConnection connection = SpeedTestUtils.buildRequest(url + "?threads=" + config.get("threadsDownload"), null);
            connection = SpeedTestUtils.catchRequest(connection, null);
            InputStream response = SpeedTestUtils.getResponseStream(connection);
            if (response == null) {
                throw new SpeedTestException("GET_SERVERS", "Get Servers Response Was Null");
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(response));
            String line;
            StringBuilder buffer = new StringBuilder();
            List<String> ignore_servers = (List<String>) config.get("ignore_servers");
            try {
                while ((line = reader.readLine()) != null) {
                    buffer.append(line);
                }
                reader.close();
                DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = builderFactory.newDocumentBuilder();
                Document document = builder.parse(new InputSource(new StringReader(buffer.toString())));
                //System.out.println(buffer.toString());
                XPath path =  XPathFactory.newInstance().newXPath();
                NodeList possibleServers  = (NodeList) path.compile("/settings/servers/server").evaluate(document, XPathConstants.NODESET);
                for(int r = 0 ; r < possibleServers.getLength(); r++){
                    Node server = possibleServers.item(r);
                    String serverId = server.getAttributes().getNamedItem("id").getNodeValue();
                    Double destLat = Double.valueOf(server.getAttributes().getNamedItem("lat").getNodeValue());
                    Double destLon = Double.valueOf(server.getAttributes().getNamedItem("lon").getNodeValue());
                    Double d = SpeedTestUtils.distance(lat, lon, destLat,destLon);

                    //noinspection SuspiciousMethodCalls
                    if(!ignore_servers.contains(serverId) && !servers.containsValue(serverId)){
                        HashMap<String, String> target = new HashMap<>();
                        target.put("name",server.getAttributes().getNamedItem("name").getNodeValue());
                        target.put("url",server.getAttributes().getNamedItem("url").getNodeValue());
                        target.put("id",serverId);
                        target.put("distance", d.toString());
                        //Log.i(this.TAG, "Server: " + target.get("name") + " is " + d);
                        servers.put(target.get("url"), target);
                    }
                }
            } catch (IOException e){
                Log.e(TAG, "IOException");
                throw new SpeedTestException("GET_SERVERS", "Get Servers IOException");
            } catch (ParserConfigurationException e) {
                Log.e(TAG, "ParserConfigurationException");
                throw new SpeedTestException("GET_SERVERS", "Get Servers ParserConfigurationException");
            } catch (SAXException e) {
                Log.e(TAG, "SAXException");
                throw new SpeedTestException("GET_SERVERS", "Get Servers SAXException");
            } catch (XPathExpressionException e) {
                Log.e(TAG, "XPathExpressionException");
                throw new SpeedTestException("GET_SERVERS", "Get Servers XPathExpressionException");
            } finally {
                connection.disconnect();
            }
        }
        Log.i(TAG, "Exit: getServers");

    }

    /**
     * Limit servers to the closest speedtest.net servers based on
     * geographic distance
     */
    public void getClosestServers() throws SpeedTestException {
        Log.i(TAG, "Entry: getClosestServers");
        if (servers == null){
            try {
                getServers();
                ConfigHelper.getInstance().setServers(servers);
            } catch (SpeedTestException e) {
                servers = ConfigHelper.getInstance().getServers();
            }
        }
        List<Map<String, String>> sorted = new ArrayList<Map<String, String>>(servers.values());
        if (sorted.size() == 0) {
            throw new SpeedTestException("GET_CLOSEST_SERVERS", "No Servers to Rank");
        }
        Comparator<Map<String, String>> comparator = new Comparator<Map<String, String>>() {
            @Override
            public int compare(Map<String, String> left, Map<String, String> right) {
                if (Double.valueOf(left.get("distance")) > Double.valueOf(right.get("distance"))) {
                    return 1;
                } else {
                    return -1;
                }
            }
        };
        Collections.sort(sorted, comparator); // use the comparator as much as u wan
        closest = new HashMap<String, Map<String, String>>();

        for (int i = 0; i < 4; i++) {
            Map<String, String> candidate = sorted.get(i);
            System.out.println(candidate.get("name"));
            closest.put(candidate.get("id"),candidate);
        }
        Log.i(TAG, "Exit: getClosestServers");
    }

    /**
     * Perform a speedtest.net "ping" to determine which speedtest.net
     * server has the lowest latency
     */
    public void getBestServer() throws SpeedTestException {
        Log.i(TAG, "Entry: getBestServer");
        if (closest == null){
            getClosestServers();
        }
        Long bestLatency = null;
        for(Map.Entry<String,Map<String,String>> entry : closest.entrySet()) {
            String pingUrl = "";

            pingUrl += FilenameUtils.getPath(entry.getValue().get("url"));
            pingUrl += "latency.txt";
            Long[] samples = new Long[3];
            for (Integer i = 0; i < 3; i++) {
                Long start = System.currentTimeMillis();
                HttpURLConnection connection = SpeedTestUtils.buildRequest(pingUrl, null);
                DataOutputStream output;
                try {
                    output = new DataOutputStream(connection.getOutputStream());
                    Long end = System.currentTimeMillis();

                    InputStream response = SpeedTestUtils.getResponseStream(connection);

                    BufferedInputStream buffer = new BufferedInputStream(response);
                    Scanner s = new Scanner(buffer).useDelimiter("\\A");
                    String result = s.hasNext() ? s.next() : "";
                    Log.i(TAG, "Ping text " + result);
                    if (buffer != null) {
                        buffer.close();
                    }
                    output.close();
                    Long latency = end - start;
                    if ("test=test".equalsIgnoreCase(result.trim())) {
                        samples[i] = latency;
                    } else {
                        samples[i] = Long.valueOf(3600);
                    }
                } catch (IOException e) {
                    samples[i] = Long.valueOf(3600);
                } finally {
                    connection.disconnect();
                }
            }
            int sum = 0;
            for (Long l : samples) {
                sum += l;
            }
            Double average = 1.0d * sum / samples.length;
            Long latency = average.longValue();
            notifyTestProgress("Device " + this.device + " candidate server : " + entry.getValue().get("url") + " latency is " + latency);
            if (bestLatency == null || bestLatency > latency) {
                bestLatency = latency;
                best = entry.getValue();
                results.setPing(latency);
            }
        }
        results.setServer(best);
        notifyTestProgress("Device " + this.device + " selected best server for test as: " + best.get("url"));
        Log.i(TAG, "Exit: getBestServer");
    }

    /**
     * Test download speed against speedtest.net
     */
    public void download() throws SpeedTestException {

        if (best == null){
            getBestServer();
        }
        ArrayList<String> urls = new ArrayList<>();
        for(int i = 0; i < ((Integer[])config.get("sizesDownload")).length; i ++){
            for(int r = 0; r < ((Integer)config.get("countsDownload")); r ++){
                String size = String.valueOf(((Integer[])config.get("sizesDownload"))[i]);
                String downloadUrl = "";
                downloadUrl += FilenameUtils.getPath(best.get("url"));
                downloadUrl += "random";
                downloadUrl += size;
                downloadUrl += "x";
                downloadUrl += size;
                downloadUrl += ".jpg";
                urls.add(downloadUrl);
            }
        }
        ArrayList<HttpURLConnection> requests = new ArrayList<>();
        ArrayList<Long> finished = new ArrayList<>();
        Integer requestCount = urls.size();
        for(String url : urls){
            requests.add(SpeedTestUtils.buildRequest(url, null));
        }
        Log.i(TAG, "Downloading using Threadcount " + config.get("threadsDownload"));
        BlockingQueue<Map<HTTPDownloader,Thread>> queue = new ArrayBlockingQueue<>((Integer)config.get("threadsDownload"));
        DownloadProducer producer = new DownloadProducer(queue, requests);
        DownloadConsumer consumer = new DownloadConsumer(queue, requestCount, finished);
        //starting producer to produce messages in queue
        Thread prodThread = new Thread(producer);
        //starting consumer to consume messages from queue
        Thread consThread = new Thread(consumer);
        Long start = System.currentTimeMillis();
        prodThread.start();
        consThread.start();
        try {
            while(prodThread.isAlive()){
                prodThread.join(1);
            }
            while(consThread.isAlive()){
                consThread.join(1);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Long stop = System.currentTimeMillis();
        Long totalDownload = 0L;
        for(Long result : finished){
            totalDownload += result;
        }
        Double downloadResult = (totalDownload / (stop - start)) * 8.0;
        if(downloadResult > 100000){
            config.put("threadsUpload",8);
        }
        //queue.remove();
        results.setBytesReceived(totalDownload);
        results.setDownloadSpeed(downloadResult);
    }
    /**
     * Test upload speed against speedtest.net
     */
    public void upload() throws SpeedTestException {
        if (best == null){
            getBestServer();
        }
        ArrayList<Integer> sizes = new ArrayList<>();
        Integer requestCount = (Integer)config.get("uploadMax");
        HashMap<HttpURLConnection, HTTPUploaderData> requests = new HashMap<>();
        ArrayList<Long> finished = new ArrayList<>();
        for(int i = 0; i < ((Integer[])config.get("sizesUpload")).length; i ++) {
            for (int r = 0; r < ((Integer) config.get("countsUpload")); r++) {
                //System.out.println(((Integer[])config.get("sizesUpload"))[i]);
                sizes.add(((Integer[])config.get("sizesUpload"))[i]);
            }
        }
        for(Integer size : sizes){
            // We set ``0`` for ``start`` and handle setting the actual
            // ``start`` in ``HTTPUploader`` to get better measurements
            HTTPUploaderData data = new HTTPUploaderData(size);
            data.createData();
            requests.put(SpeedTestUtils.buildRequest(best.get("url"), data.getData()), data);
        }

        BlockingQueue<Map<HTTPUploader,Thread>> queue = new ArrayBlockingQueue<>((Integer)config.get("threadsUpload"));
        Long start = System.currentTimeMillis();
        UploadProducer producer = new UploadProducer(queue, requests, requestCount);
        UploadConsumer consumer = new UploadConsumer(queue, requestCount, finished);
        //starting producer to produce messages in queue
        Thread prodThread = new Thread(producer);
        //starting consumer to consume messages from queue
        Thread consThread = new Thread(consumer);
        prodThread.start();
        consThread.start();
        try {
            while(prodThread.isAlive()){
                prodThread.join(1);
            }
            while(consThread.isAlive()){
                consThread.join(1);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Long stop = System.currentTimeMillis();
        Long totalUpload = 0L;
        for(Long result : finished){
            totalUpload += result;
        }
        for (HTTPUploaderData data : requests.values()) {
            data.destroy();
        }
        requests.clear();
        Double uploadResult = (totalUpload / (stop - start)) * 8.0;
        results.setBytesSent(totalUpload);
        results.setUploadSpeed(uploadResult);
    }

    /**
     * Run Interface for the speedtest
     *
     * @return Speedtest results
     */
    public SpeedtestResults runTest() throws SpeedTestException {
        try {
            try {
                getConfig();
                ConfigHelper.getInstance().setConfig(config);
            } catch (SpeedTestException e) {
                config = ConfigHelper.getInstance().getConfig();
            }
            download();
            upload();
        } catch (SpeedTestException e) {
            notifyTestProgress("Device " + this.device + " encountered exception: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            notifyTestProgress("Device " + this.device + " encountered generic exception: " + e.getMessage());
            throw new SpeedTestException("GENERIC", e.getMessage());
        }
        return this.results;
    }

    private void notifyTestProgress(String state) {
        try {
            JSONObject record = new JSONObject();
            record.put("text", state);
            StringEntity entity = new StringEntity(record.toString());
            Log.d(TAG, "Submitting Results: " + record.toString());
            SlackClient.post(this.context, entity, new BlackholeHttpResponseHandler() {
                private static final String TAG = "BlackholeHttpResponse";

                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    if (statusCode == 200) {
                        Log.d(TAG, "Successfully Submitted Results");
                    } else {
                        Log.e(TAG, "Exception in Reporting Results");
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable exception) {
                    Log.e(TAG, "Exception in Reporting Results " + statusCode);
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

    }
}