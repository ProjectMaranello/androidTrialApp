package o.maranello.clients;
import com.loopj.android.http.SyncHttpClient;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

/**
 * Created by kristianthornley on 27/01/17.
 */
public class RegisterClient {

    private static final String BASE_URL = "http://maranelloeventprocessor.mybluemix.net/app/register";
    private static SyncHttpClient client = new SyncHttpClient();

    public static void get(RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.get(BASE_URL, params, responseHandler);
    }

}
