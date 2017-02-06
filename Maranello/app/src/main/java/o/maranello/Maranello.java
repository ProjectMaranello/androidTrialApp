package o.maranello;

import android.app.Application;
import android.content.Intent;
import android.util.Log;

import o.maranello.heartbeat.BackgroundService;


/**
 * Created by kristianthornley on 6/02/17.
 */

public class Maranello extends Application {

    private static final String TAG = "Maranello";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Create");

        // Initialize the singletons so their instances
        // are bound to the application process.
        Intent startServiceIntent = new Intent(getApplicationContext(), BackgroundService.class);
        startService(startServiceIntent);


    }


}
