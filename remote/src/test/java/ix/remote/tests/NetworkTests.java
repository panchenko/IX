package ix.remote.tests;

import ix.remote.client.IXException;
import ix.remote.client.Client;
import ix.remote.client.IXRemoteException;
import ix.remote.client.IXRequestException;
import ix.remote.client.Results;
import ix.remote.server.Server;
import ix.remote.tests.SampleService.SampleException;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

public class NetworkTests {

    private Server server;
    protected Client client;

    @Before
    public void setup() throws IOException {
        final Properties properties = new Properties();
        properties.put(SampleService.class.getSimpleName(), SampleService.class.getName());
        server = new Server(0, properties);
        client = new Client(server.getInetAddress(), server.getPort());
    }

    @After
    public void close() {
        if (server != null) {
            server.close();
            server = null;
        }
        if (client != null) {
            client.close();
            client = null;
        }
    }

    @Test
    public void currentDate() throws IOException, IXException {
        final Object value = client.call(SampleService.class.getSimpleName(), "getCurrentDate");
        Assert.assertTrue(value instanceof Date);
    }

    @Test
    public void sleep() throws IOException, IXException {
        final Object value = client.call(SampleService.class.getSimpleName(), "sleep", 500L);
        Assert.assertSame(Results.VOID, value);
    }

    @Test
    public void getNull() throws IOException, IXException {
        final Object value = client.call(SampleService.class.getSimpleName(), "getNull");
        Assert.assertNull(value);
    }

    @Test
    public void unknownService() throws IOException, IXException {
        try {
            client.call("AAAAAAAAAA", "getCurrentDate");
            Assert.fail("error expected as currentTime is protected");
        } catch (IXRequestException e) {
            // expected
        }
    }

    @Test
    public void protectedMethod() throws IOException, IXException {
        try {
            client.call(SampleService.class.getSimpleName(), "currentTime");
            Assert.fail("error expected as currentTime is protected");
        } catch (IXRequestException e) {
            // expected
        }
    }

    @Test
    public void exception() throws IOException, IXException {
        try {
            client.call(SampleService.class.getSimpleName(), "error");
            Assert.fail("error must throw exception");
        } catch (IXRemoteException e) {
            // expected
            Assert.assertTrue(e.getCause() instanceof SampleException);
        }
    }

    @Test
    public void concurrency() throws InterruptedException {
        counter.set(0);
        final List<Thread> threads = Lists.newArrayList();
        for (int i = 0; i < 100; ++i) {
            final Thread thread = new Thread(new Caller());
            thread.start();
            threads.add(thread);
        }
        for (Thread thread : threads) {
            thread.join();
        }
        Assert.assertEquals(1000, counter.get());
    }

    protected AtomicInteger counter = new AtomicInteger();

    protected class Caller implements Runnable {

        @Override
        public void run() {
            try {
                for (int i = 0; i < 10; ++i) {
                    client.call(SampleService.class.getSimpleName(), "sleep", 500L);
                    client.call(SampleService.class.getSimpleName(), "getCurrentDate");
                    counter.incrementAndGet();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
}
