package o.maranello.heartbeat;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;

import o.maranello.Maranello;

/**
 * Created by kristianthornley on 6/02/17.
 */

public class PeriodicTaskReceiver extends BroadcastReceiver {

    private static final String TAG = "PeriodicTaskReceiver";
    private static final String PREFS_NAME = "MaranelloPrefsFile";
    private static final String INTENT_ACTION = "com.example.app.PERIODIC_TASK_HEART_BEAT";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!TextUtils.isEmpty(intent.getAction())) {
            Maranello maranello = (Maranello) context.getApplicationContext();
            SharedPreferences sharedPreferences = maranello.getSharedPreferences(PREFS_NAME, 0);

            if (intent.getAction().equals("android.intent.action.BATTERY_LOW")) {
                sharedPreferences.edit().putBoolean("BACKGROUND_SERVICE_BATTERY_CONTROL", false).apply();
                stopPeriodicTaskHeartBeat(context);
            } else if (intent.getAction().equals("android.intent.action.BATTERY_OKAY")) {
                sharedPreferences.edit().putBoolean("BACKGROUND_SERVICE_BATTERY_CONTROL", true).apply();
                restartPeriodicTaskHeartBeat(context, maranello);
            } else if (intent.getAction().equals(INTENT_ACTION)) {
                doPeriodicTask(context, maranello);
            }
        }
    }

    private void doPeriodicTask(Context context, Maranello maranello) {
        Log.i(TAG, "Entry: doPeriodicTask GSM Heartbeat");
        context.sendBroadcast(new Intent("com.google.android.intent.action.GTALK_HEARTBEAT"));
        context.sendBroadcast(new Intent("com.google.android.intent.action.MCS_HEARTBEAT"));
        Log.i(TAG, "Exit: doPeriodicTask GSM Heartbeat");
    }

    public void restartPeriodicTaskHeartBeat(Context context, Maranello maranello) {
        SharedPreferences sharedPreferences = maranello.getSharedPreferences(PREFS_NAME, 0);
        boolean isBatteryOk = sharedPreferences.getBoolean("BACKGROUND_SERVICE_BATTERY_CONTROL", true);
        Intent alarmIntent = new Intent(context, PeriodicTaskReceiver.class);
        boolean isAlarmUp = PendingIntent.getBroadcast(context, 0, alarmIntent, PendingIntent.FLAG_NO_CREATE) != null;

        if (isBatteryOk && !isAlarmUp) {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            alarmIntent.setAction(INTENT_ACTION);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, 0);
            alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), AlarmManager.INTERVAL_FIFTEEN_MINUTES, pendingIntent);
        }
    }

    public void stopPeriodicTaskHeartBeat(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent alarmIntent = new Intent(context, PeriodicTaskReceiver.class);
        alarmIntent.setAction(INTENT_ACTION);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, 0);
        alarmManager.cancel(pendingIntent);
    }
}
