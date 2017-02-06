package o.maranello.heartbeat;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.BatteryManager;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import o.maranello.Maranello;

/**
 * Created by kristianthornley on 6/02/17.
 */

public class BackgroundService extends Service {

    private static final String TAG = "BackgroundService";
    private static final String PREFS_NAME = "MaranelloPrefsFile";

    PeriodicTaskReceiver mPeriodicTaskReceiver = new PeriodicTaskReceiver();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Maranello maranello = (Maranello) getApplicationContext();
        SharedPreferences sharedPreferences = maranello.getSharedPreferences(PREFS_NAME, 0);
        IntentFilter batteryStatusIntentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatusIntent = registerReceiver(null, batteryStatusIntentFilter);

        if (batteryStatusIntent != null) {
            int level = batteryStatusIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = batteryStatusIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            float batteryPercentage = level / (float) scale;
            float lowBatteryPercentageLevel = 0.14f;

            try {
                int lowBatteryLevel = Resources.getSystem().getInteger(Resources.getSystem().getIdentifier("config_lowBatteryWarningLevel", "integer", "android"));
                lowBatteryPercentageLevel = lowBatteryLevel / (float) scale;
            } catch (Resources.NotFoundException e) {
                Log.e(TAG, "Missing low battery threshold resource");
            }

            sharedPreferences.edit().putBoolean("BACKGROUND_SERVICE_BATTERY_CONTROL", batteryPercentage >= lowBatteryPercentageLevel).apply();
        } else {
            sharedPreferences.edit().putBoolean("BACKGROUND_SERVICE_BATTERY_CONTROL", true).apply();
        }

        mPeriodicTaskReceiver.restartPeriodicTaskHeartBeat(getApplicationContext(), maranello);
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


}
