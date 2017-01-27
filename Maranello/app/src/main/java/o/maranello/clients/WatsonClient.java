package o.maranello.clients;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

/**
 * Created by kristianthornley on 27/01/17.
 */
public class WatsonClient {

    //private static final String BASE_URL = "http://j4698t.messaging.internetofthings.ibmcloud.com:1883/api/v0002/device/types/Maranello_App/devices/app0001/events/event";
    private static final String BASE_URL = "http://j4698t.messaging.internetofthings.ibmcloud.com:1883/api/v0002/device/types/Maranello_App/devices";
    private static AsyncHttpClient client = new AsyncHttpClient();

    public static void post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {


        client.post(getAbsoluteUrl(url), params, responseHandler);
    }

    private static String getAbsoluteUrl(String relativeUrl) {
        return BASE_URL + relativeUrl;
    }
}
