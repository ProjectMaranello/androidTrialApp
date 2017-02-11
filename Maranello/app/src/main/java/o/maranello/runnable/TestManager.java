package o.maranello.runnable;

import android.content.Context;
import android.util.Log;

import com.loopj.android.http.BlackholeHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;
import o.maranello.clients.SlackClient;

/**
 * Created by kristianthornley on 6/02/17.
 */
public class TestManager {
    private static final String TAG = "TestManager";
    private static TestManager ourInstance = new TestManager();
    private boolean isComplete = false;
    private RunTest currentTest = null;
    private Context context;

    private TestManager() {
    }

    public static TestManager getInstance() {
        return ourInstance;
    }

    public void startTest(Context context, String deviceId) throws TestInProgressException {
        if (currentTest == null) {
            isComplete = false;
            notifyTestProgress("Device " + deviceId + " received message and is starting test");
            this.context = context;
            currentTest = new RunTest(context);
            Thread worker = new Thread(currentTest);
            worker.start();
            while (worker.isAlive()) {
                try {
                    worker.join(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            isComplete = true;
            notifyTestProgress("Device " + deviceId + " received message completed test");
            currentTest = null;
        } else {
            throw new TestInProgressException();
        }

    }

    public boolean isComplete() {
        return isComplete;
    }
    private void notifyTestProgress(String state) {
        try {
            JSONObject record = new JSONObject();
            record.put("text", state);
            StringEntity entity = new StringEntity(record.toString());
            Log.d(TAG, "Submitting Status: " + record.toString());
            SlackClient.post(this.context, entity, new BlackholeHttpResponseHandler() {
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
