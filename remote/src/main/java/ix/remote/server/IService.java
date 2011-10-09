package ix.remote.server;

import ix.remote.client.IXRemoteException;
import ix.remote.client.IXRequestException;

public interface IService {

    public Object call(String method, Object[] params) throws IXRequestException, IXRemoteException;

}
