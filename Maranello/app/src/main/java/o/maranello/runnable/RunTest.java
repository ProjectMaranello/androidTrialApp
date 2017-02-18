package o.maranello.runnable;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

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
 * Created by kristianthornley on 4/02/17.
 * <p>
 * Runnable thread to do the test and upload the results
 */

public class RunTest implements Runnable {
    private static final String TAG = "RunTest";
    private static final String PREFS_NAME = "MaranelloPrefsFile";

    private Context context;

    public RunTest(Context context) {
        this.context = context;
    }

    @Override
    public void run() {
        Log.i(TAG, "Entry: run");
        SharedPreferences settings = this.context.getSharedPreferences(PREFS_NAME, 0);
        Speedtest test = new Speedtest(settings.getString("deviceId", ""), context);
        SpeedtestResults result = test.runTest();
        test.destroy();
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("ping", String.valueOf(result.getPing()));
        editor.putString("uploadSpeed", String.valueOf(result.getUploadSpeed()));
        editor.putString("downloadSpeed", String.valueOf(result.getDownloadSpeed()));
        editor.apply();
        System.gc();
        JSONObject jsonParams = new JSONObject();
        try {
            JSONObject record = new JSONObject();
            record.put("messageType", "measurement");
            record.put("device", settings.getString("deviceId", ""));
            record.put("pingTime", result.getPing());
            record.put("downloadSpeed", result.getDownloadSpeed());
            record.put("uploadSpeed", result.getUploadSpeed());
            jsonParams.put("d", record);
            jsonParams.put("ts", 0);
            jsonParams.put("serviceRequestor", "Server");
            StringEntity entity = new StringEntity(jsonParams.toString());
            Log.d(TAG, "Submitting Results: " + jsonParams.toString());
            WatsonClient.post(settings.getString("deviceId", "") + "/events/event", this.context, entity, settings.getString("iotKey", ""), new BlackholeHttpResponseHandler() {
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
            Log.e(TAG, "Exception in JSON");
        } catch (UnsupportedEncodingException e2) {
            Log.e(TAG, "Exception in Encoding");
        }

        Log.i(TAG, "Exit: run");
    }
}
