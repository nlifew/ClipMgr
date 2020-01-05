package cn.nlifew.clipmgr;

import android.app.ActivityThread;
import android.content.ClipData;
import android.content.ComponentName;
import android.content.Context;
import android.content.IClipboard;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.LinkedList;

import cn.nlifew.clipmgr.core.IClipMgr;

class IClipboardProxy implements InvocationHandler {
    private static final String TAG = "IClipboardProxy";
    private static final String MY_PACKAGE_NAME = BuildConfig.APPLICATION_ID;
    private static final String MY_SERVICE_NAME = ".service.ClipMgrService";

    private final IClipboard mObject;

    IClipboardProxy(IClipboard ic) {
        mObject = ic;
    }


    private Object setPrimaryClip(Method method, Object[] args) throws Throwable {
        Log.i(TAG, "setPrimaryClip: start");

        final ClipData clip = (ClipData) args[0];
        Log.i(TAG, "connectToClipMgr: " + clip);

        return method.invoke(mObject, args);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        final String name = method.getName();
        Log.i(TAG, "invoke: " + name);

        if ("setPrimaryClip".equals(name)) {
            return setPrimaryClip(method, args);
        }
        return method.invoke(mObject, args);
    }
}
