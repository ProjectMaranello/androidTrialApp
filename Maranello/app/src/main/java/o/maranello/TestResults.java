package o.maranello;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.ToggleButton;

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
 * Created by kristianthornley on 27/01/17.
 * A login screen that displays the last results of test execution and allows the user to manually run another test
 */
public class TestResults extends AppCompatActivity {

    //Common prefs file used throughout the project
    private static final String PREFS_NAME = "MaranelloPrefsFile";
    private static final String TAG = "TestResults";

    // UI references.
    private TextView mUploadValue;
    private TextView mDownloadValue;
    private TextView mPingValue;
    private ToggleButton mTestOn;
    private View mProgressView;
    private View mTestFormView;

    //Keep track of the result submission task to ensure we can cancel it if requested.
    private SubmitResultsTask mSubmitResultsTask = null;

    /**
     * Init the screen
     * @param savedInstanceState  saved instance data
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG,"Entry: onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_results);
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        //Set the Automated toggle
        mTestOn = (ToggleButton) findViewById(R.id.automatedTestStatusValue);
        mTestOn.setChecked(settings.getBoolean("testOn", false));
        mTestOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                disableTest();
            }
        });

        mUploadValue = (TextView) findViewById(R.id.uploadValue);
        mUploadValue.setText(settings.getString("uploadSpeed","0.0"));

        mDownloadValue = (TextView) findViewById(R.id.downloadValue);
        mDownloadValue.setText(settings.getString("downloadSpeed","0.0"));

        mPingValue = (TextView) findViewById(R.id.pingValue);
        mPingValue.setText(settings.getString("ping","0.0"));

        //Get the form view
        mTestFormView = findViewById(R.id.test_form);
        //Get the progress bar
        mProgressView = findViewById(R.id.test_progress);
        Log.i(TAG,"Exit: onCreate");
    }

    /**
     * Handle the disable test click
     */
    private void disableTest() {
        Log.i(TAG,"Entry: disableTest");
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("testOn", mTestOn.isChecked());
        editor.apply();
        Log.i(TAG,"Exit: disableTest");
    }

    /**
     * Handle the retry test click
     */
    public void retry(@SuppressWarnings("UnusedParameters") View view) {
        Log.i(TAG,"Entry: retry");
        SharedPreferences sharedAppPreferences = this.getSharedPreferences(PREFS_NAME,0);

        //Get the current SSID and check againts the SSID stored e.g. they could be at a friends house
        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifiManager.getConnectionInfo();
        String currentSSID = info.getSSID();

        if (mSubmitResultsTask != null) {
            Log.d(TAG, "Test is already running");
            return;
        }
        //If the test is enabled run the test otherwise the user has indicated that it should be turned off
        if(sharedAppPreferences.getBoolean("testOn",false)){
            if (sharedAppPreferences.getString("ssid", "").equalsIgnoreCase(currentSSID)) {
                showProgress(true);
                new RunTest().execute();
            } else {
                Log.d(TAG, "Wrong SSID");
            }
        }else{
            Log.d(TAG, "Test disabled");
        }
        Log.i(TAG,"Exit: retry");
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mTestFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mTestFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mTestFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mTestFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    /**
     * Async worker to run test on seperate thread
     */
    private class RunTest extends AsyncTask<Void, Void, SpeedtestResults> {
        private static final String TAG = "RunTest";
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

            mUploadValue = (TextView) findViewById(R.id.uploadValue);
            mUploadValue.setText(String.valueOf(result.getUploadSpeed()));

            mDownloadValue = (TextView) findViewById(R.id.downloadValue);
            mDownloadValue.setText(String.valueOf(result.getDownloadSpeed()));

            mPingValue = (TextView) findViewById(R.id.pingValue);
            mPingValue.setText(String.valueOf(result.getPing()));
            showProgress(false);

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
     * Submits the data to the Watson API
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
                Log.i(TAG, "Submitting Results: " + jsonParams.toString());
                WatsonClient.post(mDeviceId +"/events/event", TestResults.this.getApplicationContext(), entity, mToken , new BlackholeHttpResponseHandler() {
                    private static final String TAG = "BlackholeHttpResponse";
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                        Boolean success = false;
                        mSubmitResultsTask = null;
                        if(statusCode == 200){
                            success = true;
                            Log.i(TAG, "Successfully Submitted Results");
                        }else{
                            Log.e(TAG, "Exception in Reporting Results");
                        }
                        if (!success) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                }
                            });
                        }
                    }
                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] response, Throwable exception){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mSubmitResultsTask = null;
                            }
                        });
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
            showProgress(false);
        }
    }
}
