package ix.remote.client;

/**
 * Error on server while dispatching request
 */
public class IXRequestException extends IXException {

    private static final long serialVersionUID = -3207612641998050599L;

    public IXRequestException() {
        super();
    }

    public IXRequestException(String message, Throwable cause) {
        super(message, cause);
    }

    public IXRequestException(String message) {
        super(message);
    }

    public IXRequestException(Throwable cause) {
        super(cause);
    }

}
