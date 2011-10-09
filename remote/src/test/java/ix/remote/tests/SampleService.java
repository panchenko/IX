package ix.remote.tests;

import java.util.Date;

public class SampleService {

    public void sleep(Long millis) throws InterruptedException {
        Thread.sleep(millis.longValue());
    }

    public Date getCurrentDate() {
        return new Date();
    }

    public Object getNull() {
        return null;
    }

    protected long currentTime() {
        return System.currentTimeMillis();
    }

    public static class SampleException extends RuntimeException {
        private static final long serialVersionUID = 4447502073481425953L;
    }

    public void error() {
        throw new SampleException();
    }

}
