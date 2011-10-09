package ix.remote.client;

/**
 * Exception while executing the request.
 */
public class IXRemoteException extends IXException {

    private static final long serialVersionUID = 6752403237261470288L;

    public IXRemoteException() {
        super();
    }

    public IXRemoteException(String message, Throwable cause) {
        super(message, cause);
    }

    public IXRemoteException(String message) {
        super(message);
    }

    public IXRemoteException(Throwable cause) {
        super(cause);
    }

}
