package o.maranello;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class TestSettings extends AppCompatActivity {
    public static final String PREFS_NAME = "MaranelloPrefsFile";
    private EditText mBroadbandSupplierView;
    private EditText mPlanNameView;
    private EditText mCostPerMonthView;
    private EditText mDownloadSpeedView;
    private EditText mUploadSpeedView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_settings);

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);

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


    }

    public void submit(View view) {
        Intent intent = new Intent(this, TestResults.class);
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();

        String broadbandSupplier = mBroadbandSupplierView.getText().toString();
        editor.putString("broadbandSupplier",broadbandSupplier);

        String planName = mPlanNameView.getText().toString();
        editor.putString("planName",planName);

        String costPerMonth = mCostPerMonthView.getText().toString();
        editor.putString("costPerMonth",costPerMonth);

        String downloadSpeed = mDownloadSpeedView.getText().toString();
        editor.putString("downloadSpeed",downloadSpeed);

        String uploadSpeed = mUploadSpeedView.getText().toString();
        editor.putString("uploadSpeed",uploadSpeed);

        // Commit the edits!
        editor.commit();
        startActivity(intent);
    }
}
