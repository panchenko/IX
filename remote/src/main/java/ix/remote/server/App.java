package ix.remote.server;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import com.google.common.io.Closeables;

public class App {

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("port number expected");
        }
        final int port = Integer.parseInt(args[0]);
        final Properties properties = new Properties();
        final FileInputStream stream = new FileInputStream("server.properties");
        try {
            properties.load(stream);
        } finally {
            Closeables.closeQuietly(stream);
        }
        final Server server = new Server(port, properties);
        System.out.println("Listening on " + server.getPort() + " with " + properties.stringPropertyNames());
    }

}
