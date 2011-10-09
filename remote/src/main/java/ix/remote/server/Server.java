package ix.remote.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class Server implements Runnable {

    private final ServerSocket socket;
    protected final ExecutorService pool = Executors.newCachedThreadPool();
    private final Properties properties;
    private final List<Connection> clients = Lists.newArrayList();
    private Thread thread;

    public Server(int port, Properties properties) throws IOException {
        this.properties = properties;
        this.socket = new ServerSocket(port);
        this.thread = new Thread(this);
        thread.start();
    }

    public InetAddress getInetAddress() {
        return socket.getInetAddress();
    }

    public int getPort() {
        return socket.getLocalPort();
    }

    private static final Logger LOGGER = Logger.getLogger(Connection.class);

    public void close() {
        try {
            this.socket.close();
            this.thread.join();
        } catch (Exception e) {
            LOGGER.error("Error closing server", e);
        }
        final Connection[] connections;
        synchronized (this.clients) {
            connections = this.clients.toArray(new Connection[this.clients.size()]);
            this.clients.clear();
        }
        for (Connection connection : connections) {
            connection.close();
        }
    }

    @Override
    public void run() {
        for (;;) {
            try {
                final Socket client = socket.accept();
                final Connection c = new Connection(this, client, createServices());
                synchronized (clients) {
                    clients.add(c);
                }
                c.start();
            } catch (IOException e) {
                if (socket.isClosed()) {
                    return;
                }
                LOGGER.error(e);
            }
        }
    }

    protected Map<String, IService> createServices() {
        final Map<String, IService> services = Maps.newHashMap();
        for (String serviceName : properties.stringPropertyNames()) {
            final String className = properties.getProperty(serviceName);
            try {
                final Class<?> clazz = Class.forName(className);
                final Object instance = clazz.newInstance();
                services.put(serviceName, new Service(instance));
            } catch (Exception e) {
                LOGGER.error("Error creating service " + serviceName + "=" + className, e);
            }
        }
        return services;
    }

    protected void removeConnection(Connection client) {
        synchronized (clients) {
            clients.remove(client);
        }
    }

}
