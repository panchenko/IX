package ix.remote.client;

@SuppressWarnings("serial")
public abstract class IXException extends Exception {

    public IXException() {
    }

    public IXException(String message) {
        super(message);
    }

    public IXException(Throwable cause) {
        super(cause);
    }

    public IXException(String message, Throwable cause) {
        super(message, cause);
    }

}
