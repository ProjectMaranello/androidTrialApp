package o.maranello.notification;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.gcm.GcmPubSub;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.loopj.android.http.BlackholeHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;
import o.maranello.R;
import o.maranello.clients.SlackClient;
import o.maranello.clients.WatsonClient;

/**
 * Created by kristianthornley on 27/11/16.
 * Registers the device with Firebase returning GSM token
 *
 */
public class RegistrationIntentService extends IntentService {
    //Common prefs file used throughout the project
    private static final String PREFS_NAME = "MaranelloPrefsFile";
    private static final String TAG = "RegIntentService";
    private static final String[] TOPICS = {"global"};

    public RegistrationIntentService() {
        super(TAG);
    }

    /**
     * Gets GSM token from Firebase
     * @param intent application intent
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i(TAG,"Entry: onHandleIntent");
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences sharedAppPreferences = this.getSharedPreferences(PREFS_NAME,0);

        try {
            // Initially this call goes out to the network to retrieve the token, subsequent calls
            // are local.
            // R.string.gcm_defaultSenderId (the Sender ID) is typically derived from google-services.json.
            // See https://developers.google.com/cloud-messaging/android/start for details on this file.
            InstanceID instanceID = InstanceID.getInstance(this);
            String token = instanceID.getToken(getString(R.string.gcm_defaultSenderId), GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
            Log.i(TAG, "GCM Registration Token: " + token);
            //Persist the token to the shared app context
            SharedPreferences.Editor editor = sharedAppPreferences.edit();
            //If Token is not the same as saved token and the saved token is not empty
            if (!sharedAppPreferences.getString("gsmToken", "").equals(token) && !TextUtils.isEmpty(sharedAppPreferences.getString("gsmToken", ""))) {
                //Token Has Changed
                String deviceId = sharedAppPreferences.getString("deviceId", "");
                notifyTokenChanged("Device " + deviceId + " has changed GSM key notifing Watson");
                JSONObject jsonParams = new JSONObject();
                try {
                    JSONObject record = new JSONObject();
                    record.put("messageType", "token_refresh");
                    record.put("device", sharedAppPreferences.getString("deviceId", ""));
                    record.put("iotKey", token);
                    jsonParams.put("d", record);
                    jsonParams.put("ts", 0);
                    jsonParams.put("serviceRequestor", "Server");
                    StringEntity entity = new StringEntity(jsonParams.toString());
                    Log.d(TAG, "Submitting Token Update: " + jsonParams.toString());
                    WatsonClient.post(sharedAppPreferences.getString("deviceId", "") + "/events/event", this.getApplicationContext(), entity, sharedAppPreferences.getString("iotKey", ""), new BlackholeHttpResponseHandler() {
                        private static final String TAG = "BlackholeHttpResponse";

                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                            if (statusCode == 200) {
                                Log.d(TAG, "Successfully Token Update");
                            } else {
                                Log.e(TAG, "Exception in Token Update");
                            }
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable exception) {
                            Log.e(TAG, "Exception in Token Update " + statusCode);
                        }
                    });
                } catch (JSONException e) {
                    Log.e(TAG, "Exception in JSON");
                } catch (UnsupportedEncodingException e2) {
                    Log.e(TAG, "Exception in Encoding");
                }

            }
            editor.putString("gsmToken", token).apply();
            sendRegistrationToServer();

            // Subscribe to topic channels
            subscribeTopics(token);

            // You should store a boolean that indicates whether the generated token has been
            // sent to your server. If the boolean is false, send the token to your server,
            // otherwise your server should have already received the token.
            sharedPreferences.edit().putBoolean(MarenelloPreferences.SENT_TOKEN_TO_SERVER, true).apply();
            // [END register_for_gcm]
        } catch (Exception e) {
            Log.d(TAG, "Failed to complete token refresh", e);
            // If an exception happens while fetching the new token or updating our registration data
            // on a third-party server, this ensures that we'll attempt the update at a later time.
            sharedPreferences.edit().putBoolean(MarenelloPreferences.SENT_TOKEN_TO_SERVER, false).apply();
        }
        // Notify UI that registration has completed, so the progress indicator can be hidden.
        Intent registrationComplete = new Intent(MarenelloPreferences.REGISTRATION_COMPLETE);
        LocalBroadcastManager.getInstance(this).sendBroadcast(registrationComplete);
        Log.i(TAG,"Exit: onHandleIntent");
    }

    /**
     * Persist registration to third-party servers.
     *
     * Modify this method to associate the user's GCM registration token with any server-side account
     * maintained by your application.
     *
     */
    private void sendRegistrationToServer() {
        Log.i(TAG,"Entry: sendRegistrationToServer");
        // Add custom implementation, as needed.
        Log.i(TAG,"Exit: sendRegistrationToServer");
    }

    /**
     * Subscribe to any GCM topics of interest, as defined by the TOPICS constant.
     *
     * @param token GCM token
     * @throws IOException if unable to reach the GCM PubSub service
     */
    private void subscribeTopics(String token) throws IOException {
        Log.i(TAG,"Entry: subscribeTopics");
        GcmPubSub pubSub = GcmPubSub.getInstance(this);
        for (String topic : TOPICS) {
            pubSub.subscribe(token, "/topics/" + topic, null);
        }
        Log.i(TAG,"Exit: subscribeTopics");
    }

    private void notifyTokenChanged(String state) {
        try {
            JSONObject record = new JSONObject();
            record.put("text", state);
            StringEntity entity = new StringEntity(record.toString());
            Log.d(TAG, "Submitting Status: " + record.toString());
            SlackClient.post(this.getApplicationContext(), entity, new BlackholeHttpResponseHandler() {
                private static final String TAG = "BlackholeHttpResponse";

                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    if (statusCode == 200) {
                        Log.d(TAG, "Successfully Submitted Status");
                    } else {
                        Log.e(TAG, "Exception in Reporting Status");
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable exception) {
                    Log.e(TAG, "Exception in Reporting Status " + statusCode);
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

    }
}