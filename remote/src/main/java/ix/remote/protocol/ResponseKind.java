package ix.remote.protocol;

/**
 * Response codes
 */
public class ResponseKind {
    public static final byte NONE = 0;

    /**
     * response is "void"
     */
    public static final byte VOID = 1;

    /**
     * response is "null"
     */
    public static final byte NULL = 2;

    /**
     * response is some object. followed by int32 packet length and serialized object
     */
    public static final byte OBJECT = 3;

    /**
     * Error while executing the remote call. followed by int32 packet length and serialized object
     */
    public static final byte EXCEPTION = 100;

    /**
     * Error while dispatching remote call (service/method not found). followed by int32 packet length and serialized object
     */
    public static final byte ERROR = 101;
}
