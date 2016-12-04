package o.maranello;

import java.util.HashMap;

/**
 * Created by kristianthornley on 27/11/16.
 */
public class SpeedTestClient {

    HashMap<String, String> apiData = new  HashMap<String, String>();

    public void runTest(){
        //Look here https://github.com/bertrandmartel/speed-test-lib/tree/master/speedtest/src/main/java/fr/bmartel/speedtest

        String recommendedserverid;
        String hash;
        /*String download = int(round(self.download / 1000.0, 0));
        String ping = int(round(self.ping, 0));
        String upload = int(round(self.upload / 1000.0, 0));*/
        String bytesreceived;
        String bytessent;
        String serverid;

        String template = "recommendedserverid=%s" +
                "&ping=%s" +
                "&screenresolution=" +
                "&promo=" +
                "&download=%s" +
                "&screendpi=" +
                "&upload=%s" +
                "&testmethod=http" +
                "&hash=%s" +
                "&touchscreen=none" +
                "&startmode=pingselect" +
                "&accuracy=1" +
                "&bytesreceived=%s" +
                "&bytessent=%s" +
                "&serverid=%s";
        //String message = String.format(recommendedserverid, ping, download, upload, hash, bytesreceived, bytessent, serverid);
        /*
        download = int(round(self.download / 1000.0, 0))
        ping = int(round(self.ping, 0))
        upload = int(round(self.upload / 1000.0, 0))
        headers = {'Referer': 'http://c.speedtest.net/flash/speedtest.swf'}
        request = build_request('://www.speedtest.net/api/api.php',data='&'.join(api_data).encode(),headers=headers)
         */
    }

}
