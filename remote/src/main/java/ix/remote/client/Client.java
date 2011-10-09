package ix.remote.client;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import ix.remote.protocol.Commands;
import ix.remote.protocol.ResponseKind;
import ix.remote.protocol.Serialization;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

import com.google.common.base.Throwables;
import com.google.common.io.Closeables;

public class Client {

    private static final Logger LOGGER = Logger.getLogger(Client.class);

    private final Socket socket;
    private final DataInputStream in;
    private final DataOutputStream out;
    private final AtomicInteger commandNumberGenerator = new AtomicInteger();

    public Client(String host, int port) throws IOException {
        socket = new Socket(host, port);
        in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
        out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
    }

    public Client(InetAddress address, int port) throws IOException {
        socket = new Socket(address, port);
        in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
        out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
    }

    public void close() {
        try {
            socket.close();
        } catch (IOException e) {
            LOGGER.error("Error closing socket", e);
        }
    }

    static class Request {
        byte responseKind;
        byte[] response;

        Object getObject() throws IXResponseException {
            try {
                final ObjectInputStream objectStream = new ObjectInputStream(new ByteArrayInputStream(response));
                try {
                    return objectStream.readObject();
                } finally {
                    Closeables.closeQuietly(objectStream);
                }
            } catch (IOException e) {
                throw new IXResponseException(e);
            } catch (ClassNotFoundException e) {
                throw new IXResponseException(e);
            }
        }
    }

    private final Map<Integer, Request> activeRequests = new HashMap<Integer, Request>();

    private Request sendCall(String service, String method, Object... params) throws IOException {
        final ByteArrayOutputStream buffer;
        if (params.length > 0) {
            buffer = new ByteArrayOutputStream();
            Serialization.writeParameters(buffer, params);
        } else {
            buffer = null;
        }
        final Request request = new Request();
        final int commandNumber = commandNumberGenerator.incrementAndGet();
        synchronized (activeRequests) {
            activeRequests.put(commandNumber, request);
        }
        synchronized (out) {
            out.writeInt(commandNumber);
            out.writeByte(Commands.CALL);
            out.writeUTF(service);
            out.writeUTF(method);
            if (buffer != null) {
                out.writeInt(buffer.size());
                buffer.writeTo(out);
            } else {
                out.writeInt(0);
            }
            out.flush();
        }
        return request;
    }

    private boolean readerActive;

    private void waitResponse(Request request) throws IOException, IXException {
        synchronized (activeRequests) {
            while (readerActive) {
                if (request.responseKind != ResponseKind.NONE) {
                    return;
                }
                try {
                    activeRequests.wait();
                } catch (InterruptedException e) {
                    throw new IXInterruptedException(e);
                }
            }
            if (request.responseKind != ResponseKind.NONE) {
                return;
            }
            readerActive = true;
        }
        try {
            while (request.responseKind == ResponseKind.NONE) {
                readResponse();
            }
        } finally {
            synchronized (activeRequests) {
                readerActive = false;
                activeRequests.notifyAll();
            }
        }
    }

    private void readResponse() throws IOException, IXException {
        final int commandNumber = in.readInt();
        final byte responseKind = in.readByte();
        final byte[] buffer;
        switch (responseKind) {
        case ResponseKind.VOID:
        case ResponseKind.NULL:
            buffer = null;
            break;
        case ResponseKind.OBJECT:
        case ResponseKind.EXCEPTION:
        case ResponseKind.ERROR: {
            final int length = in.readInt();
            buffer = new byte[length];
            in.readFully(buffer);
            break;
        }
        default:
            throw new IXProtocolException("Wrong response kind " + responseKind);
        }
        synchronized (activeRequests) {
            final Request request = activeRequests.remove(commandNumber);
            if (request != null) {
                request.responseKind = responseKind;
                request.response = buffer;
                activeRequests.notifyAll();
            }
        }
    }

    private static final int MAX_STRING_LENGTH = Character.MAX_VALUE / 3;

    public Object call(String service, String method, Object... params) throws IOException, IXException {
        checkNotNull(service);
        checkArgument(service.length() < MAX_STRING_LENGTH);
        checkNotNull(method);
        checkArgument(method.length() < MAX_STRING_LENGTH);
        if (LOGGER.isInfoEnabled()) {
            final StringBuilder sb = new StringBuilder(128);
            sb.append("Calling ").append(service).append(".").append(method);
            for (Object param : params) {
                sb.append(" ").append(param);
            }
            LOGGER.info(sb);
        }
        final Request request = sendCall(service, method, params);
        waitResponse(request);
        switch (request.responseKind) {
        case ResponseKind.NULL:
            LOGGER.info("Returning null");
            return null;
        case ResponseKind.VOID:
            LOGGER.info("Returning VOID");
            return Results.VOID;
        case ResponseKind.OBJECT:
            final Object value = request.getObject();
            LOGGER.info("Returning " + value);
            return value;
        case ResponseKind.EXCEPTION: {
            final Object exception = request.getObject();
            LOGGER.info("Throwing " + exception);
            if (exception instanceof Throwable) {
                Throwables.propagateIfInstanceOf((Throwable) exception, IXRemoteException.class);
                throw new IXRemoteException((Throwable) exception);
            } else {
                throw new IXRemoteException(String.valueOf(exception));
            }
        }
        case ResponseKind.ERROR: {
            final Object exception = request.getObject();
            LOGGER.info("Throwing " + exception);
            if (exception instanceof Throwable) {
                Throwables.propagateIfInstanceOf((Throwable) exception, IXRequestException.class);
                throw new IXRequestException((Throwable) exception);
            } else {
                throw new IXRequestException(String.valueOf(exception));
            }
        }
        default:
            // can't happen
            throw new IXProtocolException("Wrong responseKind " + request.responseKind);
        }
    }

}
