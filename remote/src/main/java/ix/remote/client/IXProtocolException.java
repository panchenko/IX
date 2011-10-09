package ix.remote.client;

/**
 * TCP-protocol format error
 */
public class IXProtocolException extends IXException {

    private static final long serialVersionUID = -5501354582656895718L;

    public IXProtocolException() {
        super();
    }

    public IXProtocolException(String message, Throwable cause) {
        super(message, cause);
    }

    public IXProtocolException(String message) {
        super(message);
    }

    public IXProtocolException(Throwable cause) {
        super(cause);
    }

}
