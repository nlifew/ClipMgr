package cn.nlifew.clipmgr;

import android.content.IClipboard;
import android.os.IBinder;
import android.util.Log;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

class IClipboardBinderProxy implements InvocationHandler {
    private static final String TAG = "IClipboardBinderProxy";

    private final IBinder mBinder;

    IClipboardBinderProxy(IBinder binder) {
        mBinder = binder;
    }

    private Object queryLocalInterface(Method method, Object[] args) throws Throwable {
        final Object old = method.invoke(mBinder, args);
        if (old != null) {
            return old;
        }
        IClipboard real = IClipboard.Stub.asInterface(mBinder);
        Class<?> cls = real.getClass();
        return Proxy.newProxyInstance(cls.getClassLoader(),
                cls.getInterfaces(), new IClipboardProxy(real));
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        final String name = method.getName();
        Log.i(TAG, "invoke: " + name);

        switch (name) {
            case "queryLocalInterface":
                return queryLocalInterface(method, args);
        }
        return method.invoke(mBinder, args);
    }
}
