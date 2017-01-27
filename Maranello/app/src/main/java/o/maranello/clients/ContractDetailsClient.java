package o.maranello.clients;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

/**
 * Created by kristianthornley on 27/01/17.
 */
public class ContractDetailsClient {
    private static final String BASE_URL = "http://maranelloeventprocessor.mybluemix.net/app/contract";
    private static AsyncHttpClient client = new AsyncHttpClient();

    public static void post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.post(getAbsoluteUrl(url), params, responseHandler);
    }
    private static String getAbsoluteUrl(String relativeUrl) {
        return BASE_URL + relativeUrl;
    }

}