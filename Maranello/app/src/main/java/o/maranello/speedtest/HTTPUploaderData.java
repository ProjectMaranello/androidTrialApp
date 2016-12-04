package o.maranello.speedtest;

/**
 * Created by kristianthornley on 28/11/16.
 * File like object to improve cutting off the upload once the timeout
 * has been reached
 */
public class HTTPUploaderData {

    private Integer length;
    private Integer start;
    private Integer timeout;
    private Integer[] total = {0};
    private String data;

    public HTTPUploaderData(Integer length, Integer start, Integer timeout){
        this.length = length;
        this.start = start;
        this.timeout = timeout;
        this.data = null;
    }

    public void createData(){
        String chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        Long multiplier = Math.round(this.length / 36.0);
        String repeated = new String(new char[(int)(long)multiplier]).replace("\0",chars);
        StringBuffer payload = new StringBuffer();
        payload.append("content1=");
        payload.append(repeated.substring(0,repeated.length()-9));
        data = payload.toString();
    }

    public String getData(){
        return this.data;
    }
    /*




    def read(self, n=10240):
        if ((timeit.default_timer() - self.start) <= self.timeout and
                not SHUTDOWN_EVENT.isSet()):
            chunk = self.data.read(n)
            self.total.append(len(chunk))
            return chunk
        else:
            raise SpeedtestUploadTimeout

    def __len__(self):
        return self.length
        */
}
