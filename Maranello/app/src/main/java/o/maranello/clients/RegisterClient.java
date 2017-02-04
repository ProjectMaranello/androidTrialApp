package o.maranello.clients;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.SyncHttpClient;

/**
 * Created by kristianthornley on 27/01/17.
 *
 * Static abstraction of the Register Client Service, username, password and GCM key to service
 * Returned is the iot token for auth to watson
 */
public class RegisterClient {

    private static final String BASE_URL = "http://maranelloeventprocessor.mybluemix.net/app/register";
    private static final SyncHttpClient client = new SyncHttpClient();

    /**
     * Log Client in and retrieve IOT Token
     * @param params request params
     * @param responseHandler handles the response
     */
    public static void get(RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.get(BASE_URL, params, responseHandler);
    }

}
