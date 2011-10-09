package ix.remote.client;

public class IXInterruptedException extends IXException {

    private static final long serialVersionUID = 1L;

    public IXInterruptedException() {
        super();
    }

    public IXInterruptedException(String message, Throwable cause) {
        super(message, cause);
    }

    public IXInterruptedException(String message) {
        super(message);
    }

    public IXInterruptedException(Throwable cause) {
        super(cause);
    }

}
