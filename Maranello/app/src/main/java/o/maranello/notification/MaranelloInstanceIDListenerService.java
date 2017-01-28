package o.maranello.notification;

import android.content.Intent;
import android.util.Log;

import com.google.android.gms.iid.InstanceIDListenerService;

import o.maranello.Welcome;

/**
 * Created by kristianthornley on 27/11/16.
 * Refreshes the GSM token form Firebase
 */
public class MaranelloInstanceIDListenerService extends InstanceIDListenerService {

    private static final String TAG = "MaranelloInstanceIDList";

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. This call is initiated by the
     * InstanceID provider.
     */
    @Override
    public void onTokenRefresh() {
        // Fetch updated Instance ID token and notify our app's server of any changes (if applicable).
        Log.d(TAG, "Token Refresh");

        Intent intent = new Intent(this, Welcome.class);
        startService(intent);
    }
}
