package o.maranello;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;
import o.maranello.clients.ContractDetailsClient;
import o.maranello.clients.RegisterClient;

/**
 * Created by kristianthornley on 27/01/17.
 * A screen that captures the users contract data
 */
public class TestSettings extends AppCompatActivity {
    public static final String PREFS_NAME = "MaranelloPrefsFile";

    //Screen references
    private EditText mBroadbandSupplierView;
    private EditText mPlanNameView;
    private EditText mCostPerMonthView;
    private EditText mDownloadSpeedView;
    private EditText mUploadSpeedView;
    private View mProgressView;
    private View mSettingsFormView;

    //Used to block multiple tests from running at the same time
    private SubmitSettingsTask mSubmitSettingsTask = null;

    /**
     * Init the screen
     * @param savedInstanceState saved instance data
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Set the view layout
        setContentView(R.layout.activity_test_settings);
        //Get the shared prefs storage
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        //Initialise the text from the previously captured data or blank
        mBroadbandSupplierView = (EditText) findViewById(R.id.broadbandSupplier);
        mBroadbandSupplierView.setText(settings.getString("broadbandSupplier",""));

        mPlanNameView = (EditText) findViewById(R.id.planName);
        mPlanNameView.setText(settings.getString("planName",""));

        mCostPerMonthView = (EditText) findViewById(R.id.costPerMonth);
        mCostPerMonthView.setText(settings.getString("costPerMonth",""));

        mDownloadSpeedView = (EditText) findViewById(R.id.downloadSpeed);
        mDownloadSpeedView.setText(settings.getString("downloadSpeed",""));

        mUploadSpeedView = (EditText) findViewById(R.id.uploadSpeed);
        mUploadSpeedView.setText(settings.getString("uploadSpeed",""));

        //Get the form view
        mSettingsFormView = findViewById(R.id.settings_form);
        //Get the progress bar
        mProgressView = findViewById(R.id.settings_progress);

    }

    public void submit(View view) {
        //Check there is not already a background task running
        if (mSubmitSettingsTask != null) {
            return;
        }
        //Set the shared context from the form
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        mBroadbandSupplierView.setError(null);
        String broadbandSupplier = mBroadbandSupplierView.getText().toString();
        editor.putString("broadbandSupplier",broadbandSupplier);
        mPlanNameView.setError(null);
        String planName = mPlanNameView.getText().toString();
        editor.putString("planName",planName);
        mCostPerMonthView.setError(null);
        String costPerMonth = mCostPerMonthView.getText().toString();
        editor.putString("costPerMonth",costPerMonth);
        mDownloadSpeedView.setError(null);
        String downloadSpeed = mDownloadSpeedView.getText().toString();
        editor.putString("downloadSpeed",downloadSpeed);
        mUploadSpeedView.setError(null);
        String uploadSpeed = mUploadSpeedView.getText().toString();
        editor.putString("uploadSpeed",uploadSpeed);

        // Commit the edits!
        editor.apply();

        //Show progress bar
        showProgress(true);

        //create the task that's going to submit the data to the server
        mSubmitSettingsTask = new SubmitSettingsTask(settings.getString("deviceId",""), broadbandSupplier, planName, costPerMonth, downloadSpeed, uploadSpeed );
        mSubmitSettingsTask.execute((Void) null);
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

            mSettingsFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mSettingsFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mSettingsFormView.setVisibility(show ? View.GONE : View.VISIBLE);
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
            mSettingsFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    public class SubmitSettingsTask extends AsyncTask<Void, Void, Boolean> {
        private static final String TAG = "SubmitSettingsTask";
        private final String mDeviceId;
        private final String mBroadbandSupplier;
        private final String mPlanName;
        private final String mCostPerMonth;
        private final String mDownloadSpeed;
        private final String mUploadSpeed;
        SubmitSettingsTask(String deviceId, String broadbandSupplier, String planName, String costPerMonth, String downloadSpeed, String uploadSpeed) {
            mDeviceId = deviceId;
            mBroadbandSupplier = broadbandSupplier;
            mPlanName = planName;
            mCostPerMonth = costPerMonth;
            mDownloadSpeed = downloadSpeed;
            mUploadSpeed = uploadSpeed;
        }

        protected Boolean doInBackground(Void... params) {
            RequestParams requestParams = new RequestParams();
            JSONObject jsonParams = new JSONObject();
            try {
                jsonParams.put("deviceId", mDeviceId);
                jsonParams.put("supplier", mBroadbandSupplier);
                jsonParams.put("planName", mPlanName);
                jsonParams.put("costPerMonth", Float.valueOf(mCostPerMonth));
                jsonParams.put("contractDownload", Float.valueOf(mDownloadSpeed));
                jsonParams.put("contractUpload", Float.valueOf(mUploadSpeed));
                StringEntity entity = new StringEntity(jsonParams.toString());
                ContractDetailsClient.post(TestSettings.this.getApplicationContext(), entity, new JsonHttpResponseHandler() {
                    private static final String TAG = "JsonHttpResponseHandler";
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        Boolean success = false;
                        try {
                            if(!TextUtils.isEmpty(response.getString("deviceId"))){
                                success = true;
                            }
                        }catch (JSONException e){
                            Log.e(TAG, "Exception in JSON");
                        }
                        if (success) {
                            Intent intent = new Intent(TestSettings.this, TestResults.class);
                            startActivity(intent);
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mSubmitSettingsTask = null;
                                    showProgress(false);

                                }
                            });
                        }
                    }
                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable exception, JSONObject response){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mSubmitSettingsTask = null;
                                showProgress(false);
                             }
                        });
                    }
                });
            } catch (JSONException e){
                Log.e(TAG, "Exception in JSON");
            } catch (UnsupportedEncodingException e2){
                Log.e(TAG, "Exception in Encoding");
            }
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {

        }

        @Override
        protected void onCancelled() {
            mSubmitSettingsTask = null;
            showProgress(false);
        }
    }
}
