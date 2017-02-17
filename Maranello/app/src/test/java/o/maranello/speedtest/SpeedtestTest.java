package o.maranello.speedtest;

import android.content.Context;
import android.test.mock.MockContext;
import android.util.Log;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static junit.framework.Assert.assertNotNull;

/**
 * Test class of the speedtest port
 */
@SuppressWarnings("unused")
@RunWith(PowerMockRunner.class)
@PrepareForTest({Log.class})
public class SpeedtestTest {
    Context context;
    @Before
    public void setUp() {
        context = new MockContext();
        assertNotNull(context);

        PowerMockito.mockStatic(Log.class);
    }

    @Test
    public void getServers_isCorrect() {

        Speedtest test = new Speedtest("app0001", context);
        try {
            test.getServers();
        } catch (SpeedTestException e) {

        }
    }



    @Test
    public void getClosestServers_isCorrect() {
        Speedtest test = new Speedtest("app0001", context);
        try {
            test.getClosestServers();
        } catch (SpeedTestException e) {

        }

    }

    @Test
    public void getBestServers_isCorrect() {
        Speedtest test = new Speedtest("app0001", context);
        try {
            test.getBestServer();
        } catch (SpeedTestException e) {

        }
    }

    @Test
    public void download_isCorrect() {
        Speedtest test = new Speedtest("app0001", context);
        try {
            test.download();
        } catch (SpeedTestException e) {

        }
    }

    @Test
    public void upload_isCorrect() {
        Speedtest test = new Speedtest("app0001", context);
        try {
            test.upload();
        } catch (SpeedTestException e) {

        }
    }

    @Test
    public void runTest_isCorrect() {
        Speedtest test = new Speedtest("app0001", context);
        SpeedtestResults results = test.runTest();
        System.out.println(results.dump());


    }

}