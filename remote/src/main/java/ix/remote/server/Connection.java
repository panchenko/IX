package ix.remote.server;

import ix.remote.client.IXProtocolException;
import ix.remote.client.IXRemoteException;
import ix.remote.client.IXRequestException;
import ix.remote.client.Results;
import ix.remote.protocol.Commands;
import ix.remote.protocol.ResponseKind;
import ix.remote.protocol.Serialization;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Map;

public class Connection implements Runnable {

    private final Server server;
    private final Socket socket;
    private final DataInputStream in;
    private final DataOutputStream out;
    protected final Map<String, IService> services;

    public Connection(Server server, Socket socket, Map<String, IService> services) throws IOException {
        this.server = server;
        this.socket = socket;
        in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
        out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
        this.services = services;
    }

    public void start() {
        new Thread(this).start();
    }

    @Override
    public void run() {
        try {
            for (;;) {
                handleCommand();
            }
        } catch (IOException e) {
            if (!socket.isClosed()) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } catch (IXProtocolException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        try {
            socket.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        server.removeConnection(this);
    }

    private class Task implements Runnable {

        final int commandNumber;
        final String serviceName;
        final String methodName;
        final byte[] buffer;

        public Task(int commandNumber, String serviceName, String methodName, byte[] buffer) {
            this.commandNumber = commandNumber;
            this.serviceName = serviceName;
            this.methodName = methodName;
            this.buffer = buffer;
        }

        @Override
        public void run() {
            final IService service = services.get(serviceName);
            if (service == null) {
                sendResponse(commandNumber, ResponseKind.ERROR, String.format("Service %s not found", serviceName));
                return;
            }
            final Object[] params;
            try {
                params = buffer != null ? Serialization.readParameters(buffer) : new Object[0];
            } catch (IOException e) {
                sendResponse(commandNumber, ResponseKind.ERROR, e);
                return;
            } catch (ClassNotFoundException e) {
                sendResponse(commandNumber, ResponseKind.ERROR, e);
                return;
            }
            final Object value;
            try {
                value = service.call(methodName, params);
            } catch (IXRequestException e) {
                sendResponse(commandNumber, ResponseKind.ERROR, e);
                return;
            } catch (IXRemoteException e) {
                sendResponse(commandNumber, ResponseKind.EXCEPTION, e);
                return;
            }
            if (value == null) {
                sendResponse(commandNumber, ResponseKind.NULL, null);
            } else if (value == Results.VOID) {
                sendResponse(commandNumber, ResponseKind.VOID, null);
            } else {
                sendResponse(commandNumber, ResponseKind.OBJECT, value);
            }
        }
    }

    private void handleCommand() throws IOException, IXProtocolException {
        final int commandNumber = in.readInt();
        final byte command = in.readByte();
        if (command != Commands.CALL) {
            throw new IXProtocolException("Wrong command code " + command);
        }
        final String serviceName = in.readUTF();
        final String methodName = in.readUTF();
        final int bufferLen = in.readInt();
        final byte[] buffer;
        if (bufferLen != 0) {
            buffer = new byte[bufferLen];
            in.readFully(buffer);
        } else {
            buffer = null;
        }
        server.pool.execute(new Task(commandNumber, serviceName, methodName, buffer));
    }

    protected void sendResponse(int commandNumber, byte responseKind, Object value) {
        try {
            synchronized (out) {
                out.writeInt(commandNumber);
                out.writeByte(responseKind);
                if (value != null) {
                    final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                    final ObjectOutputStream objectStream = new ObjectOutputStream(bytes);
                    objectStream.writeObject(value);
                    out.writeInt(bytes.size());
                    bytes.writeTo(out);
                }
                out.flush();
            }
        } catch (IOException e) {
            // TODO
        }
    }

    public void close() {
        try {
            this.socket.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
