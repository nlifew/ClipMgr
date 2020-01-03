package cn.nlifew.clipmgr;

import android.os.IBinder;
import android.os.ServiceManager;
import android.util.Log;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import static android.content.Context.CLIPBOARD_SERVICE;

public class HashMapProxy extends HashMap<String, IBinder> {
    private static final String TAG = "HashMapProxy";

    private boolean mBlock = true;
    private IBinder mProxy;

    HashMapProxy(Map<String, IBinder> map) {
        super(map);
    }

    @Override
    public IBinder get(Object key) {
        final IBinder old = super.get(key);
        if (! (CLIPBOARD_SERVICE.equals(key) && mBlock)) {
            return old;
        }
        if (mProxy != null) {
            return mProxy;
        }
        try {
            IBinder rawBinder = old;
            if (rawBinder == null) {
                mBlock = false; // 防止递归
                rawBinder = ServiceManager.getService(CLIPBOARD_SERVICE);
                mBlock = true;
            }
            Class<?> cls = rawBinder.getClass();
            mProxy = (IBinder) Proxy.newProxyInstance(
                    cls.getClassLoader(),
                    cls.getInterfaces(),
                    new IClipboardBinderProxy(rawBinder));
            return mProxy;
        } catch (Exception e) {
            Log.e(TAG, "get: failed to generate Proxy", e);
        }
        return old;
    }
}
