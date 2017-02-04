package o.maranello.notification;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;
import com.loopj.android.http.BlackholeHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;
import o.maranello.clients.WatsonClient;
import o.maranello.speedtest.Speedtest;
import o.maranello.speedtest.SpeedtestResults;

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
    private SubmitResultsTask mSubmitResultsTask = null;


    /**
     * Called when message is received.
     *
     * @param from SenderID of the sender.
     * @param data Data bundle containing message data as key/value pairs.
     *             For Set of keys use data.keySet().
     */
    @Override
    public void onMessageReceived(String from, Bundle data) {
        Log.i(TAG,"Entry: onMessageReceived");
        String message = data.getString("message");
        Log.d(TAG, "Message From: " + from);
        Log.d(TAG, "Message Is: " + message);

        //Get the current SSID and check againts the SSID stored e.g. they could be at a friends house
        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifiManager.getConnectionInfo();
        String currentSSID = info.getSSID();

        if("runtest".equalsIgnoreCase(message)){
            SharedPreferences sharedAppPreferences = this.getSharedPreferences(PREFS_NAME,0);
            if (mSubmitResultsTask != null) {
                Log.d(TAG, "Test is already running");
                return;
            }
            //If the test is enabled run the test otherwise the user has indicated that it should be turned off
            if(sharedAppPreferences.getBoolean("testOn",false)){
                if (sharedAppPreferences.getString("ssid", "").equalsIgnoreCase(currentSSID)) {
                    Log.d(TAG, "Execute Test");
                    new RunTest().execute();
                } else {
                    Log.d(TAG, "Wrong SSID");
                }
            }else{
                Log.d(TAG, "Test disabled");
            }


        }else {
            Log.d(TAG, "Message was not runtest");
        }
        Log.i(TAG,"Exit: onMessageReceived");
    }

    /**
     * Clas to run teh test async
     */
    private class RunTest extends AsyncTask<Void, Void, SpeedtestResults> {
        /**
         * Override the Async worker to run the test in the background
         * @param params parameters for the task
         * @return Speedtest results
         */
        @Override
        protected SpeedtestResults doInBackground(Void... params) {
            Log.i(TAG,"Entry: doInBackground");
            Speedtest test = new Speedtest();
            SpeedtestResults results = test.runTest();
            test.destroy();
            Log.i(TAG,"Exit: doInBackground");
            return results;
        }

        /**
         * When the test completes
         * @param result results to send to the server
         */
        @Override
        protected void onPostExecute(SpeedtestResults result) {
            Log.i(TAG,"Entry: onPostExecute");
            //Set the shared context from the form
            SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
            SharedPreferences.Editor editor = settings.edit();
            editor.putString("ping", String.valueOf(result.getPing()));
            editor.putString("uploadSpeed", String.valueOf(result.getUploadSpeed()));
            editor.putString("downloadSpeed", String.valueOf(result.getDownloadSpeed()));
            editor.apply();

            //create the task that's going to submit the data to the server
            mSubmitResultsTask = new SubmitResultsTask(settings.getString("deviceId",""), result.getPing(), result.getDownloadSpeed(), result.getUploadSpeed(), settings.getString("iotKey","") );
            mSubmitResultsTask.execute((Void) null);
            Log.i(TAG,"Exit: onPostExecute");
        }
        @Override
        protected void onPreExecute() {}
        @Override
        protected void onProgressUpdate(Void... values) {}
    }

    /**
     * Submits the data top the Watson API
     */
    public class SubmitResultsTask extends AsyncTask<Void, Void, Boolean> {
        private static final String TAG = "SubmitResultsTask";
        private final String mDeviceId;
        private final Float mPingTime;
        private final Float mDownloadSpeed;
        private final Float mUploadSpeed;
        private final String mToken;

        /**
         * Construct with result data
         * @param deviceId the device id
         * @param pingTime the ping time
         * @param downloadSpeed the recorded download speed
         * @param uploadSpeed the recorded upload speed
         * @param token the iot auth token
         */
        SubmitResultsTask(String deviceId, Long pingTime, Double downloadSpeed, Double uploadSpeed, String token) {
            Log.i(TAG,"Entry: construct");
            mDeviceId = deviceId;
            mPingTime = pingTime.floatValue();
            mDownloadSpeed = downloadSpeed.floatValue();
            mUploadSpeed = uploadSpeed.floatValue();
            mToken = token;
            Log.i(TAG,"Exit: construct");
        }

        /**
         * Another Async thread to post info to Watson
         * @param params parameters for the task
         * @return state
         */
        protected Boolean doInBackground(Void... params) {
            Log.i(TAG,"Entry: doInBackground");

            JSONObject jsonParams = new JSONObject();
            try {
                JSONObject record = new JSONObject();
                record.put("messageType","measurement");
                record.put("device",mDeviceId);
                record.put("pingTime",mPingTime);
                record.put("downloadSpeed",mDownloadSpeed);
                record.put("uploadSpeed",mUploadSpeed);
                jsonParams.put("d", record);
                jsonParams.put("ts", 0);
                jsonParams.put("serviceRequestor", "Server");
                StringEntity entity = new StringEntity(jsonParams.toString());
                Log.d(TAG, "Submitting Results: " + jsonParams.toString());
                WatsonClient.post(mDeviceId +"/events/event", getApplicationContext(), entity, mToken , new BlackholeHttpResponseHandler() {
                    private static final String TAG = "BlackholeHttpResponse";
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        mSubmitResultsTask = null;
                        if(statusCode == 200){
                            Log.d(TAG, "Successfully Submitted Results");
                        }else{
                            Log.e(TAG, "Exception in Reporting Results");
                        }
                    }
                    @Override
                    public void onFailure(int statusCode, Header[] headers,  byte[] responseBody, Throwable exception){
                        mSubmitResultsTask = null;
                    }
                });
            }catch (JSONException e){
                Log.e(TAG, "Exception in JSON");
            }catch (UnsupportedEncodingException e2){
                Log.e(TAG, "Exception in Encoding");
            }
            Log.i(TAG,"Exit: doInBackground");
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {

        }

        @Override
        protected void onCancelled() {
            mSubmitResultsTask = null;
        }
    }
}
