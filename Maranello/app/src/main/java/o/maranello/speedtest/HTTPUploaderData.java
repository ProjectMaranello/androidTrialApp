package o.maranello.speedtest;

/**
 * Created by kristianthornley on 28/11/16.
 * File like object to improve cutting off the upload once the timeout
 * has been reached
 */
class HTTPUploaderData {

    private final Integer length;
    private String data;

    public HTTPUploaderData(Integer length) {
        this.length = length;
        this.data = null;
    }

    /**
     * Creates data to upload
     */
    public void createData(){
        String chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        Long multiplier = Math.round(this.length / 36.0);
        String repeated = new String(new char[(int)(long)multiplier]).replace("\0",chars);
        data = "";
        data += "content1=";
        data += repeated.substring(0, repeated.length() - 9);
    }
    public String getData(){
        return this.data;
    }

    public void destroy() {
        this.data = null;
    }
}
