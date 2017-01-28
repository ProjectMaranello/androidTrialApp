package o.maranello.clients;

import android.content.Context;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.SyncHttpClient;

import cz.msebera.android.httpclient.entity.StringEntity;

/**
 * Created by kristianthornley on 27/01/17.
 */
public class WatsonClient {

    //private static final String BASE_URL = "http://j4698t.messaging.internetofthings.ibmcloud.com:1883/api/v0002/device/types/Maranello_App/devices/app0001/events/event";
    private static final String BASE_URL = "http://j4698t.messaging.internetofthings.ibmcloud.com:1883/api/v0002/device/types/Maranello_App/devices/";
    private static SyncHttpClient client = new SyncHttpClient();

    public static void post(String url, Context context, StringEntity entity, String authToken, AsyncHttpResponseHandler responseHandler) {

        client.setBasicAuth("use-token-auth", authToken);
        client.post(context, getAbsoluteUrl(url), entity, "application/json", responseHandler);
    }

    private static String getAbsoluteUrl(String relativeUrl) {
        return BASE_URL + relativeUrl;
    }
}
