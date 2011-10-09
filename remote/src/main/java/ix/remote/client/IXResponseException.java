package ix.remote.client;

/**
 * Exception when deserializing the response
 */
public class IXResponseException extends IXException {

    private static final long serialVersionUID = 8214917464846707181L;

    public IXResponseException() {
    }

    public IXResponseException(String message) {
        super(message);
    }

    public IXResponseException(Throwable cause) {
        super(cause);
    }

    public IXResponseException(String message, Throwable cause) {
        super(message, cause);
    }

}
