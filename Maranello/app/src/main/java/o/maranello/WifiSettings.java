package o.maranello;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

/**
 * Created by kristianthornley on 27/01/17.
 * Display a confirmation screen to the user for the selected wifi connection
 */
public class WifiSettings extends AppCompatActivity {

    private static final String TAG = "WifiSettings";
    public static final String PREFS_NAME = "MaranelloPrefsFile";

    /**
     * Init the screen
     * @param savedInstanceState saved instance data
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WifiManager wifiManager = (WifiManager) getSystemService (Context.WIFI_SERVICE);
        WifiInfo info = wifiManager.getConnectionInfo ();
        String ssid  = info.getSSID();
        Log.v(TAG, "SSID=" + ssid);
        setContentView(R.layout.activity_wifi_settings);
    }

    @Override
    public void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        TextView title = (TextView) findViewById( R.id.wifiDisplay );
        //FIXME: resolve when we have a physical device
        title.setText("KT_HOME");

    }

    public void error(View view) {
        Intent intent = new Intent(this, Register.class);
        intent.putExtra("message","WIFI Not Enabled");
        startActivity(intent);
    }

    public void confirm(View view) {
        Intent intent = new Intent(this, TestSettings.class);
        startActivity(intent);
    }
}
