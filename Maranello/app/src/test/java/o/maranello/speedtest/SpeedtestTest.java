package o.maranello.speedtest;

import android.util.Log;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * Test class of the speedtest port
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({Log.class})
public class SpeedtestTest {

    @Before
    public void setUp() {
        PowerMockito.mockStatic(Log.class);
    }

    @Test
    public void getConfig_isCorrect() throws Exception {
        Speedtest test = new Speedtest();

    }

    @Test
    public void getServers_isCorrect() throws Exception {
        Speedtest test = new Speedtest();
        test.getServers();

    }



    @Test
    public void getClosestServers_isCorrect() throws Exception {
        Speedtest test = new Speedtest();
        test.getClosestServers();

    }

    @Test
    public void getBestServers_isCorrect() throws Exception {
        Speedtest test = new Speedtest();
        test.getBestServer();

    }

    @Test
    public void download_isCorrect() throws Exception {
        Speedtest test = new Speedtest();
        test.download();

    }

    @Test
    public void upload_isCorrect() throws Exception {
        Speedtest test = new Speedtest();
        test.upload();

    }

    @Test
    public void runTest_isCorrect() throws Exception {
        Speedtest test = new Speedtest();
        SpeedtestResults results = test.runTest();
        System.out.println(results.dump());


    }

}