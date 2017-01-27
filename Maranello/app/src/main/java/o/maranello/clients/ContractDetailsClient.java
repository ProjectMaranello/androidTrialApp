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
public class ContractDetailsClient {
    private static final String BASE_URL = "http://maranelloeventprocessor.mybluemix.net/app/contract";
    private static SyncHttpClient client = new SyncHttpClient();

    public static void post(Context context, StringEntity entity, AsyncHttpResponseHandler responseHandler) {
        client.post(context, BASE_URL, entity, "application/json", responseHandler);
    }
}