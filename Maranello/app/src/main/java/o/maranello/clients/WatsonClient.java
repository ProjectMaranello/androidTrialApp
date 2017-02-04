package o.maranello.clients;

import android.content.Context;
import android.util.Log;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.SyncHttpClient;

import cz.msebera.android.httpclient.entity.StringEntity;

/**
 * Created by kristianthornley on 27/01/17.
 * Static abstraction of the Watson IOT Service
 * sends results of the speed test to watson
 */
public class WatsonClient {
    private static final String TAG = "WatsonClient";

    private static final String BASE_URL = "http://j4698t.messaging.internetofthings.ibmcloud.com:1883/api/v0002/device/types/Maranello_App/devices/";
    private static final SyncHttpClient client = new SyncHttpClient();

    /**
     * Post result data to watson
     * @param url endpoint url suffix
     * @param context application context
     * @param entity the data to send
     * @param authToken iot token
     * @param responseHandler handle the response
     */
    public static void post(String url, Context context, StringEntity entity, String authToken, AsyncHttpResponseHandler responseHandler) {
        Log.i(TAG,"Entry: post");
        client.setBasicAuth("use-token-auth", authToken);
        client.post(context, getAbsoluteUrl(url), entity, "application/json", responseHandler);
        Log.i(TAG,"Exit: post");
    }

    /**
     * Add additional path to base path for user device
     * @param relativeUrl url suffix
     * @return url
     */
    private static String getAbsoluteUrl(String relativeUrl) {
        return BASE_URL + relativeUrl;
    }
}
