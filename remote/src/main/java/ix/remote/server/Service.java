package ix.remote.server;

import ix.remote.client.IXRemoteException;
import ix.remote.client.IXRequestException;
import ix.remote.client.Results;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Service implements IService {

    private final Object instance;
    private final Class<?> clazz;

    public Service(Object instance) {
        this.instance = instance;
        this.clazz = instance.getClass();
    }

    @Override
    public Object call(String methodName, Object[] params) throws IXRequestException, IXRemoteException {
        final Method method = findMethod(methodName, params);
        final Object result;
        try {
            result = method.invoke(instance, params);
        } catch (IllegalArgumentException e) {
            throw new IXRequestException(e);
        } catch (IllegalAccessException e) {
            throw new IXRequestException(e);
        } catch (InvocationTargetException e) {
            throw new IXRemoteException(e.getTargetException());
        }
        return result == null && method.getReturnType() == Void.TYPE ? Results.VOID : result;
    }

    private Method findMethod(String methodName, Object[] params) throws IXRequestException {
        try {
            final Method method = findExactMethod(methodName, params);
            if (method != null) {
                return method;
            }
        } catch (SecurityException e) {
            throw new IXRequestException(e);
        } catch (NoSuchMethodException e) {
            // fall thru
        }
        // TODO handle primitives, etc
        throw new IXRequestException(String.format("Method \"%s\" not found", methodName));
    }

    private Method findExactMethod(String methodName, Object[] params) throws SecurityException, NoSuchMethodException {
        final Class<?>[] types = new Class[params.length];
        for (int i = 0; i < params.length; ++i) {
            if (params[i] == null) {
                return null;
            }
            types[i] = params[i].getClass();
        }
        return clazz.getMethod(methodName, types);
    }
}
