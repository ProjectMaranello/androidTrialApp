package o.maranello;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.loopj.android.http.BlackholeHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;
import o.maranello.clients.ContractDetailsClient;
import o.maranello.clients.WatsonClient;
import o.maranello.speedtest.Speedtest;
import o.maranello.speedtest.SpeedtestResults;

public class TestResults extends AppCompatActivity {
    public static final String PREFS_NAME = "MaranelloPrefsFile";

    private TextView mUploadValue;
    private TextView mDownloadValue;
    private TextView mPingValue;
    private ToggleButton mTestOn;
    private View mProgressView;
    private View mTestFormView;
    private SubmitResultsTask mSubmitResultsTask = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_results);
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        //Set the Automated toggle
        mTestOn = (ToggleButton) findViewById(R.id.automatedTestStatusValue);
        mTestOn.setChecked(settings.getBoolean("testOn",true));

        //Get the form view
        mTestFormView = findViewById(R.id.test_form);
        //Get the progress bar
        mProgressView = findViewById(R.id.test_progress);

        showProgress(true);
        new RunTest().execute();
    }

    public void retry(View view) {
        Intent intent = new Intent(this, Register.class);
        startActivity(intent);
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

            mUploadValue = (TextView) findViewById(R.id.uploadValue);
            mUploadValue.setText(String.valueOf(result.getUploadSpeed()));

            mDownloadValue = (TextView) findViewById(R.id.downloadValue);
            mDownloadValue.setText(String.valueOf(result.getDownloadSpeed()));

            mPingValue = (TextView) findViewById(R.id.pingValue);
            mPingValue.setText(String.valueOf(result.getPing()));
            showProgress(false);

            //create the task that's going to submit the data to the server
            mSubmitResultsTask = new SubmitResultsTask(settings.getString("deviceId",""), result.getPing(), result.getDownloadSpeed(), result.getUploadSpeed(), settings.getString("iotKey","") );
            mSubmitResultsTask.execute((Void) null);
        }
        @Override
        protected void onPreExecute() {}
        @Override
        protected void onProgressUpdate(Void... values) {}
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
                WatsonClient.post(mDeviceId +"/events/event", TestResults.this.getApplicationContext(), entity, mToken , new BlackholeHttpResponseHandler() {
                    private static final String TAG = "JsonHttpResponseHandler";
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                        Boolean success = false;
                        if(statusCode == 200){
                            success = true;
                            mSubmitResultsTask = null;
                            Log.i(TAG, "Successfully Submitted Results");
                        }else{
                            Log.e(TAG, "Exception in Reporting Results");
                        }
                        if (success) {
                            mSubmitResultsTask = null;
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mSubmitResultsTask = null;
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
            showProgress(false);
        }
    }
}
