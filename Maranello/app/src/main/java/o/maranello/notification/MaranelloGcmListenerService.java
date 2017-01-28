package o.maranello.notification;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;
import com.loopj.android.http.BlackholeHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;
import o.maranello.R;
import o.maranello.Welcome;
import o.maranello.clients.WatsonClient;
import o.maranello.speedtest.Speedtest;
import o.maranello.speedtest.SpeedtestResults;

/**
 * Created by kristianthornley on 27/11/16.
 */
public class MaranelloGcmListenerService extends GcmListenerService {
    private static final String TAG = "MaranelloGcmListenerSev";
    public static final String PREFS_NAME = "MaranelloPrefsFile";
    private SubmitResultsTask mSubmitResultsTask = null;


    /**
     * Called when message is received.
     *
     * @param from SenderID of the sender.
     * @param data Data bundle containing message data as key/value pairs.
     *             For Set of keys use data.keySet().
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(String from, Bundle data) {
        String message = data.getString("message");
        Log.d(TAG, "From: " + from);
        Log.d(TAG, "Message: " + message);
        SharedPreferences sharedAppPreferences = this.getSharedPreferences(PREFS_NAME,0);
        if (mSubmitResultsTask != null) {
            return;
        }
        new RunTest().execute();
    }
    // [END receive_message]

    /**
     * Create and show a simple notification containing the received GCM message.
     *
     * @param message GCM message received.
     */
    private void sendNotification(String message) {
        Intent intent = new Intent(this, Welcome.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_info_black_24dp)
                .setContentTitle("GCM Message")
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0, notificationBuilder.build());
    }

    private class RunTest extends AsyncTask<Void, Void, SpeedtestResults> {

        @Override
        protected SpeedtestResults doInBackground(Void... params) {
            Speedtest test = new Speedtest();
            SpeedtestResults results = test.runTest();
            return results;
        }
        @Override
        protected void onPostExecute(SpeedtestResults result) {
            //Set the shared context from the form
            SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);

            //create the task that's going to submit the data to the server
            mSubmitResultsTask = new SubmitResultsTask(settings.getString("deviceId",""), result.getPing(), result.getDownloadSpeed(), result.getUploadSpeed(), settings.getString("iotKey","") );
            mSubmitResultsTask.execute((Void) null);
        }
        @Override
        protected void onPreExecute() {}
        @Override
        protected void onProgressUpdate(Void... values) {}
    }
    public class SubmitResultsTask extends AsyncTask<Void, Void, Boolean> {
        private static final String TAG = "SubmitResultsTask";
        private final String mDeviceId;
        private final Float mPingTime;
        private final Float mDownloadSpeed;
        private final Float mUploadSpeed;
        private final String mToken;

        SubmitResultsTask(String deviceId, Long pingTime, Double downloadSpeed, Double uploadSpeed, String token) {
            mDeviceId = deviceId;
            mPingTime = pingTime.floatValue();
            mDownloadSpeed = downloadSpeed.floatValue();
            mUploadSpeed = uploadSpeed.floatValue();
            mToken = token;
        }

        protected Boolean doInBackground(Void... params) {
            RequestParams requestParams = new RequestParams();
            JSONObject jsonParams = new JSONObject();
            try {
                JSONObject record = new JSONObject();
                record.put("messageType","measurement");
                record.put("device",mDeviceId);
                record.put("pingTime",mPingTime);
                record.put("downloadSpeed",mDownloadSpeed/1000);
                record.put("uploadSpeed",mUploadSpeed/1000);
                jsonParams.put("d", record);
                jsonParams.put("ts", 0);
                jsonParams.put("serviceRequestor", "Server");
                StringEntity entity = new StringEntity(jsonParams.toString());
                Log.i(TAG, "Submitting Results: " + jsonParams.toString());
                WatsonClient.post(mDeviceId +"/events/event", getApplicationContext(), entity, mToken , new BlackholeHttpResponseHandler() {
                    private static final String TAG = "JsonHttpResponseHandler";
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        Boolean success = false;
                        if(statusCode == 200){
                            success = true;
                            mSubmitResultsTask = null;
                            Log.i(TAG, "Successfully Submitted Results");
                        }else{
                            Log.e(TAG, "Exception in Reporting Results");
                        }
                    }
                    @Override
                    public void onFailure(int statusCode, Header[] headers,  byte[] responseBody, Throwable exception){

                    }
                });
            }catch (JSONException e){
                Log.e(TAG, "Exception in JSON");
            }catch (UnsupportedEncodingException e2){
                Log.e(TAG, "Exception in JSON");
            }
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
