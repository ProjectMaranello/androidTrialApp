package o.maranello.notification;

import android.content.Intent;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.google.firebase.messaging.FirebaseMessaging;
import com.loopj.android.http.BlackholeHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;
import o.maranello.Welcome;
import o.maranello.clients.SlackClient;
import o.maranello.clients.WatsonClient;

/**
 * Created by kristianthornley on 27/11/16.
 * Refreshes the GSM token form Firebase
 */
public class MaranelloInstanceIDListenerService extends FirebaseInstanceIdService {
    private static final String PREFS_NAME = "MaranelloPrefsFile";
    private static final String TAG = "MaranelloInstanceIDList";
    private static final String[] TOPICS = {"global"};

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. This call is initiated by the
     * InstanceID provider.
     */
    @Override
    public void onTokenRefresh() {
        // Fetch updated Instance ID token and notify our app's server of any changes (if applicable).
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed token: " + refreshedToken);
        SharedPreferences sharedAppPreferences = this.getSharedPreferences(PREFS_NAME, 0);

        SharedPreferences.Editor editor = sharedAppPreferences.edit();
        if (!sharedAppPreferences.getString("gsmToken", "").equals(refreshedToken) && !TextUtils.isEmpty(sharedAppPreferences.getString("gsmToken", ""))) {
            //Token Has Changed
            String deviceId = sharedAppPreferences.getString("deviceId", "");
            notifyTokenChanged("Device " + deviceId + " has changed GSM key notifing Watson");
            JSONObject jsonParams = new JSONObject();
            try {
                JSONObject record = new JSONObject();
                record.put("messageType", "token_refresh");
                record.put("device", sharedAppPreferences.getString("deviceId", ""));
                record.put("iotKey", refreshedToken);
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
        editor.putString("gsmToken", refreshedToken).apply();
        sendRegistrationToServer();

        // Subscribe to topic channels

        FirebaseMessaging.getInstance().subscribeToTopic("global");

        // You should store a boolean that indicates whether the generated token has been
        // sent to your server. If the boolean is false, send the token to your server,
        // otherwise your server should have already received the token.
        editor.putBoolean(MarenelloPreferences.SENT_TOKEN_TO_SERVER, true).apply();
        Intent intent = new Intent(this, Welcome.class);
        startService(intent);

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

    private void sendRegistrationToServer() {
        Log.i(TAG, "Entry: sendRegistrationToServer");
        // Add custom implementation, as needed.
        Log.i(TAG, "Exit: sendRegistrationToServer");
    }
}
