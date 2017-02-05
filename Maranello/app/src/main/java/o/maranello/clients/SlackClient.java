package o.maranello.clients;

import android.content.Context;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.SyncHttpClient;

import cz.msebera.android.httpclient.entity.StringEntity;

/**
 * Created by kristianthornley on 27/01/17.
 * Static abstraction of the Slack WebHook Client
 * sends plan info to service for analysis
 */
public class SlackClient {

    private static final String BASE_URL = "https://hooks.slack.com/services/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";
    private static final SyncHttpClient client = new SyncHttpClient();

    /**
     * Post client contract details to server
     *
     * @param context         application context
     * @param entity          the data to send
     * @param responseHandler handles the response
     */
    public static void post(Context context, StringEntity entity, AsyncHttpResponseHandler responseHandler) {
        client.post(context, BASE_URL, entity, "application/json", responseHandler);
    }
}