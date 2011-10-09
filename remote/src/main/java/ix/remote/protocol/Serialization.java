package ix.remote.protocol;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

public class Serialization {

    public static void writeParameters(OutputStream buffer, Object... values) throws IOException {
        final ObjectOutputStream objectStream = new ObjectOutputStream(buffer);
        objectStream.writeInt(values.length);
        for (Object object : values) {
            objectStream.writeObject(object);
        }
        objectStream.close();
    }

    public static byte[] toByteArray(Object... values) throws IOException {
        final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        writeParameters(buffer, values);
        return buffer.toByteArray();
    }

    public static Object[] readParameters(byte[] buffer) throws IOException, ClassNotFoundException {
        final ByteArrayInputStream bs = new ByteArrayInputStream(buffer);
        final ObjectInputStream objectStream = new ObjectInputStream(bs);
        final int valueCount = objectStream.readInt();
        final Object[] values = new Object[valueCount];
        for (int i = 0; i < valueCount; ++i) {
            values[i] = objectStream.readObject();
        }
        return values;
    }

}
