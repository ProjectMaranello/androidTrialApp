package o.maranello.notification;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.loopj.android.http.BlackholeHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;
import o.maranello.clients.SlackClient;
import o.maranello.runnable.TestInProgressException;
import o.maranello.runnable.TestManager;

/**
 * Created by kristianthornley on 27/11/16.
 * Listens to the Google Cloud Messaging Notifications
 *
 */
public class MaranelloGcmListenerService extends FirebaseMessagingService {
    //Common prefs file used throughout the project
    private static final String PREFS_NAME = "MaranelloPrefsFile";
    private static final String TAG = "MaranelloGcmListenerSev";
    //Used to block multiple tests from running at the same time


    /**
     * Called when message is received.
     *
     * @param messageBundle SenderID of the sender.
     *
     */
    @Override
    public void onMessageReceived(RemoteMessage messageBundle) {
        Log.i(TAG, "Entry: onMessageReceived");
        String message = messageBundle.getData().get("message");
        Log.d(TAG, "Message From: " + messageBundle.getFrom());
        Log.d(TAG, "Message Is: " + message);
        //Get the current SSID and check againts the SSID stored e.g. they could be at a friends house
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifiManager.getConnectionInfo();
        String currentSSID = info.getSSID();

        if ("runtest".equalsIgnoreCase(message)) {
            SharedPreferences sharedAppPreferences = this.getSharedPreferences(PREFS_NAME, 0);
            String deviceId = sharedAppPreferences.getString("deviceId", "");

            //If the test is enabled run the test otherwise the user has indicated that it should be turned off
            if (sharedAppPreferences.getBoolean("testOn", false)) {
                if (sharedAppPreferences.getString("ssid", "").equalsIgnoreCase(currentSSID)) {
                    Log.d(TAG, "Execute Test");
                    try {
                        TestManager.getInstance().startTest(getApplicationContext(), deviceId);
                    } catch (TestInProgressException e) {
                        Log.d(TAG, "Test is already running");
                        notifyTestProgress("Device " + deviceId + " received message but test is already running");
                        return;
                    }
                    Log.d(TAG, "Complete");
                } else {
                    if (!TextUtils.isEmpty(currentSSID)) {
                        notifyTestProgress("Device " + deviceId + " received message but is on not on Wifi ");
                    } else {
                        notifyTestProgress("Device " + deviceId + " received message but is on the different Wifi. Current Wifi: " + currentSSID + " Saved Wifi: " + sharedAppPreferences.getString("ssid", ""));
                    }
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
