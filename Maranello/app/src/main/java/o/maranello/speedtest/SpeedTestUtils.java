package o.maranello.speedtest;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/**
 * Created by kristianthornley on 30/11/16.
 */
public class SpeedTestUtils {

    public static HttpURLConnection buildRequest(String urlString, HashMap<String, String> data, String urlParameters){
        HttpURLConnection connection = null;
        try {
            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();



            connection.setRequestProperty("Accept-Encoding", "gzip");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("User-Agent","Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_3) AppleWebKit/537.75.14 (KHTML, like Gecko) Version/7.0.3 Safari/7046A194A");
            connection.setRequestProperty("Cache-Control", "no-cache");
            if(urlParameters != null) {
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Length", Integer.toString(urlParameters.getBytes().length));
            }else{
                connection.setRequestMethod("GET");
            }
            connection.setRequestProperty("Content-Language", "en-US");
            connection.setUseCaches(false);
            connection.setDoOutput(true);
        } catch (IOException e){
            e.printStackTrace();
        }
        return connection;
    }

    public static Map<String, String> nodeListToMap(NodeList nodeList){
        HashMap<String, String> map = new HashMap<String, String>();
        for (int i = 0 ; i < nodeList.getLength(); i++){
            Node node = nodeList.item(i);
            map.put(node.getNodeName(),node.getNodeValue());
        }
        return map;
    }

    public static HttpURLConnection catchRequest(HttpURLConnection connection, String urlParameters){
        try {
            DataOutputStream output = new DataOutputStream (connection.getOutputStream());
            if(urlParameters != null) {
                output.writeBytes(urlParameters);
            }
            output.close();
        } catch (IOException e){
            e.printStackTrace();
        }
        return connection;
    }

    public static InputStream getResponseStream(HttpURLConnection connection){
        InputStream input = null;
        try {
            if("gzip".equalsIgnoreCase(connection.getHeaderField("Content-Encoding"))) {
                input = new GZIPInputStream(connection.getInputStream());
            }else {
                input = connection.getInputStream();
            }
        } catch (IOException e){
            e.printStackTrace();
        }
        return input;
    }

    public static Double distance(Double originLat, Double originLon, Double destinationLat, Double destinationLon) {
        //Determine distance between 2 sets of [lat,lon] in km

        Integer radius = 6371;  // km

        Double dlat = Math.toRadians(destinationLat - originLat);
        Double dlon = Math.toRadians(destinationLon - originLon);
        Double a = (
                Math.sin(dlat / 2) * Math.sin(dlat / 2) +
                        Math.cos(Math.toRadians(originLat)) *
                                Math.cos(Math.toRadians(destinationLat)) *
                                Math.sin(dlon / 2) * Math.sin(dlon / 2)
        );

        Double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        Double d = radius * c;

        return d;

    }
}

/*
def build_request(url, data=None, headers=None, bump=''):
    """Build a urllib2 request object

    This function automatically adds a User-Agent header to all requests

    """

    if not USER_AGENT:
        build_user_agent()

    if not headers:
        headers = {}

    if url[0] == ':':
        schemed_url = '%s%s' % (SCHEME, url)
    else:
        schemed_url = url

    if '?' in url:
        delim = '&'
    else:
        delim = '?'

    # WHO YOU GONNA CALL? CACHE BUSTERS!
    final_url = '%s%sx=%s.%s' % (schemed_url, delim,
                                 int(timeit.time.time() * 1000),
                                 bump)

    headers.update({
        'User-Agent': USER_AGENT,
        'Cache-Control': 'no-cache',
    })

    printer('%s %s' % (('GET', 'POST')[bool(data)], final_url),
            debug=True)

    return Request(final_url, data=data, headers=headers)

def bound_socket(*args, **kwargs):
    """Bind socket to a specified source IP address"""

    sock = SOCKET_SOCKET(*args, **kwargs)
    sock.bind((SOURCE, 0))
    return sock





def build_user_agent():
    """Build a Mozilla/5.0 compatible User-Agent string"""

    global USER_AGENT
    if USER_AGENT:
        return USER_AGENT

    ua_tuple = (
        'Mozilla/5.0',
        '(%s; U; %s; en-us)' % (platform.system(), platform.architecture()[0]),
        'Python/%s' % platform.python_version(),
        '(KHTML, like Gecko)',
        'speedtest-cli/%s' % __version__
    )
    USER_AGENT = ' '.join(ua_tuple)
    printer(USER_AGENT, debug=True)
    return USER_AGENT



def get_attributes_by_tag_name(dom, tag_name):
    """Retrieve an attribute from an XML document and return it in a
    consistent format

    Only used with xml.dom.minidom, which is likely only to be used
    with python versions older than 2.5
    """
    elem = dom.getElementsByTagName(tag_name)[0]
    return dict(list(elem.attributes.items()))


def print_dots(current, total, start=False, end=False):
    """Built in callback function used by Thread classes for printing
    status
    """

    if SHUTDOWN_EVENT.isSet():
        return

    sys.stdout.write('.')
    if current + 1 == total and end is True:
        sys.stdout.write('\n')
    sys.stdout.flush()


def do_nothing(*args, **kwargs):
    pass

 */