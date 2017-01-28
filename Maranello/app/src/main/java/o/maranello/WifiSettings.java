package o.maranello;

import android.content.Context;
import android.content.Intent;
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

    public static final String PREFS_NAME = "MaranelloPrefsFile";
    private static final String TAG = "WifiSettings";

    /**
     * Init the screen
     * @param savedInstanceState saved instance data
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "Entry: onCreate");
        super.onCreate(savedInstanceState);

        WifiManager wifiManager = (WifiManager) getSystemService (Context.WIFI_SERVICE);
        WifiInfo info = wifiManager.getConnectionInfo ();
        String ssid  = info.getSSID();
        Log.d(TAG, "SSID=" + ssid);
        setContentView(R.layout.activity_wifi_settings);
        Log.i(TAG, "Exit: onCreate");
    }

    /**
     * Report the WififSettings if present
     *
     * @param savedInstanceState saved instance data
     */
    @Override
    public void onPostCreate(Bundle savedInstanceState) {
        Log.i(TAG, "Entry: onPostCreate");
        super.onPostCreate(savedInstanceState);
        TextView title = (TextView) findViewById( R.id.wifiDisplay );
        //FIXME: resolve when we have a physical device
        title.setText("KT_HOME");
        Log.i(TAG, "Exit: onPostCreate");
    }

    /**
     * If no Wifi then present error
     *
     * @param view the event item
     */
    public void error(View view) {
        Log.i(TAG, "Entry: error");
        Intent intent = new Intent(this, Register.class);
        intent.putExtra("message","WIFI Not Enabled");
        startActivity(intent);
        Log.i(TAG, "Exit: error");
    }

    /**
     * User confirms the Wifi
     *
     * @param view the event item
     */
    public void confirm(View view) {
        Log.i(TAG, "Entry: confirm");
        Intent intent = new Intent(this, TestSettings.class);
        startActivity(intent);
        Log.i(TAG, "Entry: confirm");
    }
}
