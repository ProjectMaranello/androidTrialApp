package o.maranello.speedtest;

/**
 * Created by kristianthornley on 18/02/17.
 * Generic Exception Class for the speedtest paskage
 */
public class SpeedTestException extends Throwable {
    private String code;
    private String message;

    public SpeedTestException(String code, String message) {
        super();
        this.code = code;
        this.message = message;
    }
}
