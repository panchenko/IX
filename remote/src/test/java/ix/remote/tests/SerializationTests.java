package ix.remote.tests;

import ix.remote.protocol.Serialization;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

public class SerializationTests extends Assert {

    @Test
    public void string() throws IOException, ClassNotFoundException {
        byte[] b = Serialization.toByteArray("Hello");
        final Object[] values = Serialization.readParameters(b);
        assertEquals(1, values.length);
        assertEquals("Hello", values[0]);
    }

    @Test
    public void strings() throws IOException, ClassNotFoundException {
        byte[] b = Serialization.toByteArray("Hello", "world");
        final Object[] values = Serialization.readParameters(b);
        assertEquals(2, values.length);
        assertEquals("Hello", values[0]);
        assertEquals("world", values[1]);
    }

    @Test
    public void nulls() throws IOException, ClassNotFoundException {
        byte[] b = Serialization.toByteArray(null, null);
        final Object[] values = Serialization.readParameters(b);
        assertEquals(2, values.length);
        assertNull(values[0]);
        assertNull(values[1]);
    }

}
