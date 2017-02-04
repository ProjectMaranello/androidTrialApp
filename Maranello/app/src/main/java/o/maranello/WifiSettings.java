package o.maranello;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

/**
 * Created by kristianthornley on 27/01/17.
 * Display a confirmation screen to the user for the selected wifi connection
 */
public class WifiSettings extends AppCompatActivity {

    private static final String PREFS_NAME = "MaranelloPrefsFile";
    private static final String TAG = "WifiSettings";
    private String ssid;
    /**
     * Init the screen
     * @param savedInstanceState saved instance data
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "Entry: onCreate");
        super.onCreate(savedInstanceState);

        WifiManager wifiManager = (WifiManager) getSystemService (Context.WIFI_SERVICE);
        WifiInfo info = wifiManager.getConnectionInfo();
        ssid = info.getSSID();
        Log.d(TAG, "SSID=" + ssid);
        setContentView(R.layout.activity_wifi_settings);
        TextView title = (TextView) findViewById(R.id.wifiDisplay);
        title.setText(ssid);

        Log.i(TAG, "Exit: onCreate");
    }

    /**
     * If no Wifi then present error
     *
     */
    public void error(@SuppressWarnings("UnusedParameters") View view) {
        Log.i(TAG, "Entry: error");
        Intent intent = new Intent(this, Register.class);
        intent.putExtra("message","WIFI Not Enabled");
        startActivity(intent);
        Log.i(TAG, "Exit: error");
    }

    /**
     * User confirms the Wifi
     *
     */
    public void confirm(@SuppressWarnings("UnusedParameters") View view) {
        Log.i(TAG, "Entry: confirm");

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("ssid", ssid);
        editor.apply();

        Intent intent = new Intent(this, TestSettings.class);
        startActivity(intent);
        Log.i(TAG, "Entry: confirm");
    }

}
