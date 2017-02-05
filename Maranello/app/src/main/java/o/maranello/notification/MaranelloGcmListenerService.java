package o.maranello.notification;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;
import com.loopj.android.http.BlackholeHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;
import o.maranello.clients.SlackClient;
import o.maranello.runnable.RunTest;

/**
 * Created by kristianthornley on 27/11/16.
 * Listens to the Google Cloud Messaging Notifications
 *
 */
public class MaranelloGcmListenerService extends GcmListenerService {
    //Common prefs file used throughout the project
    private static final String PREFS_NAME = "MaranelloPrefsFile";
    private static final String TAG = "MaranelloGcmListenerSev";
    //Used to block multiple tests from running at the same time
    private RunTest test = null;


    /**
     * Called when message is received.
     *
     * @param from SenderID of the sender.
     * @param data Data bundle containing message data as key/value pairs.
     *             For Set of keys use data.keySet().
     */
    @Override
    public void onMessageReceived(String from, Bundle data) {
        Log.i(TAG, "Entry: onMessageReceived");
        String message = data.getString("message");
        Log.d(TAG, "Message From: " + from);
        Log.d(TAG, "Message Is: " + message);

        //Get the current SSID and check againts the SSID stored e.g. they could be at a friends house
        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifiManager.getConnectionInfo();
        String currentSSID = info.getSSID();

        if ("runtest".equalsIgnoreCase(message)) {
            SharedPreferences sharedAppPreferences = this.getSharedPreferences(PREFS_NAME, 0);
            String deviceId = sharedAppPreferences.getString("deviceId", "");
            if (test != null) {
                Log.d(TAG, "Test is already running");
                notifyTestProgress("Device " + deviceId + " received message but test is already running");
                return;
            }
            //If the test is enabled run the test otherwise the user has indicated that it should be turned off
            if (sharedAppPreferences.getBoolean("testOn", false)) {
                if (sharedAppPreferences.getString("ssid", "").equalsIgnoreCase(currentSSID)) {
                    Log.d(TAG, "Execute Test");
                    notifyTestProgress("Device " + deviceId + " received message and is starting test");
                    test = new RunTest(getApplicationContext());
                    Thread worker = new Thread(test);
                    worker.start();
                    while (worker.isAlive()) {
                        try {
                            worker.join(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    test = null;
                    notifyTestProgress("Device " + deviceId + " received message completed test");
                    Log.d(TAG, "Complete");
                } else {
                    notifyTestProgress("Device " + deviceId + " received message but is on the different Wifi");
                    Log.d(TAG, "Wrong SSID");
                }
            } else {
                notifyTestProgress("Device " + deviceId + " received message testing is disabled");
                Log.d(TAG, "Test disabled");
            }


        } else {
            Log.d(TAG, "Message was not runtest");
        }
        Log.i(TAG, "Exit: onMessageReceived");
    }

    private void notifyTestProgress(String state) {
        try {
            JSONObject record = new JSONObject();
            record.put("text", state);
            StringEntity entity = new StringEntity(record.toString());
            Log.d(TAG, "Submitting Results: " + record.toString());
            SlackClient.post(this.getApplicationContext(), entity, new BlackholeHttpResponseHandler() {
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
