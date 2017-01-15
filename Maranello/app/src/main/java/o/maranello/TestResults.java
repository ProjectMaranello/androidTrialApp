package o.maranello;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.ToggleButton;

import o.maranello.speedtest.Speedtest;
import o.maranello.speedtest.SpeedtestResults;

public class TestResults extends AppCompatActivity {
    public static final String PREFS_NAME = "MaranelloPrefsFile";

    private TextView mUploadValue;
    private TextView mDownloadValue;
    private TextView mPingValue;
    private ToggleButton mTestOn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_results);
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);

        new RunTest().execute();
        //SpeedtestResults results = SpeedTestClient.runTest();



        mTestOn = (ToggleButton) findViewById(R.id.automatedTestStatusValue);
        mTestOn.setChecked(settings.getBoolean("testOn",true));
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
            mUploadValue = (TextView) findViewById(R.id.uploadValue);
            mUploadValue.setText(String.valueOf(result.getUploadSpeed()));

            mDownloadValue = (TextView) findViewById(R.id.downloadValue);
            mDownloadValue.setText(String.valueOf(result.getDownloadSpeed()));

            mPingValue = (TextView) findViewById(R.id.pingValue);
            mPingValue.setText(String.valueOf(result.getPing()));

        }
        @Override
        protected void onPreExecute() {}
        @Override
        protected void onProgressUpdate(Void... values) {}
    }
}
