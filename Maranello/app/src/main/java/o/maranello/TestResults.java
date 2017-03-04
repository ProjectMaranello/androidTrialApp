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

import o.maranello.runnable.TestInProgressException;
import o.maranello.runnable.TestManager;

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

    //Used to block multiple tests from running at the same time
    private SubmitTestTask mSubmitTestTask = null;
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
        mDownloadValue = (TextView) findViewById(R.id.downloadValue);
        mPingValue = (TextView) findViewById(R.id.pingValue);
        setValues();
        //Get the form view
        mTestFormView = findViewById(R.id.test_form);
        //Get the progress bar
        mProgressView = findViewById(R.id.test_progress);
        Log.i(TAG,"Exit: onCreate");
    }

    private void setValues() {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        mUploadValue.setText(settings.getString("uploadSpeed", "0.0"));
        mDownloadValue.setText(settings.getString("downloadSpeed", "0.0"));
        mPingValue.setText(settings.getString("ping", "0.0"));
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
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifiManager.getConnectionInfo();
        String currentSSID = info.getSSID();
        showProgress(true);

        //If the test is enabled run the test otherwise the user has indicated that it should be turned off
        if(sharedAppPreferences.getBoolean("testOn",false)){
            if (sharedAppPreferences.getString("ssid", "").equalsIgnoreCase(currentSSID)) {
                String deviceId = sharedAppPreferences.getString("deviceId", "");
                Log.d(TAG, "Execute Test");
                mSubmitTestTask = new SubmitTestTask(deviceId);
                mSubmitTestTask.execute((Void) null);

                /*while(!TestManager.getInstance().isComplete()){

                }*/
                //setValues();
                //showProgress(false);
                Log.d(TAG, "Complete");
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
     * Submits the data to the server
     */
    public class SubmitTestTask extends AsyncTask<Void, Void, Boolean> {
        private static final String TAG = "SubmitTestTask";
        private final String mDeviceId;

        SubmitTestTask(String deviceId) {
            Log.i(TAG, "Entry: construct");
            mDeviceId = deviceId;
            Log.i(TAG, "Exit: construct");
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                TestManager.getInstance().startTest(getApplicationContext(), mDeviceId);
                while (!TestManager.getInstance().isComplete()) {
                    Thread.sleep(1000);
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setValues();
                        showProgress(false);
                    }
                });

            } catch (TestInProgressException e) {
                Log.d(TAG, "Test is already running");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            return null;
        }
    }
}
