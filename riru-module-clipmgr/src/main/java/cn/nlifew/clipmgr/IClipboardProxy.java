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

    private final LinkedList<ClipData> mClipCaches = new LinkedList<>();


    private boolean connectToClipMgr(ClipData clip) {
        try {

            final Context c = ActivityThread.currentApplication();
            Log.i(TAG, "connectToClipMgr: " + c.getPackageName());

            Intent intent = new Intent();
            intent.setComponent(new ComponentName(
                    MY_PACKAGE_NAME, MY_PACKAGE_NAME + MY_SERVICE_NAME
            ));
            ServiceConnection conn = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    ClipData clipData = mClipCaches.removeFirst();
                    try {
                        IClipMgr mgr = IClipMgr.Stub.asInterface(service);
                        mgr.setPrimaryClip(c.getPackageName(), clipData);
                        c.unbindService(this);
                    } catch (Exception e) {
                        Log.e(TAG, "onServiceConnected: " + clipData, e);
                    }
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {

                }
            };

            if (c.bindService(intent, conn, Context.BIND_AUTO_CREATE | Context.BIND_ABOVE_CLIENT)) {
                Log.i(TAG, "connectToClipMgr: bind ok");
                mClipCaches.addLast(clip);
                return true;
            }
        } catch (Exception e) {
            Log.e(TAG, "connectToClipMgr: " + clip, e);
        }
        return false;
    }

    private Object setPrimaryClip(Method method, Object[] args) throws Throwable {
        Log.i(TAG, "setPrimaryClip: start");

        final ClipData clip = (ClipData) args[0];
        Log.i(TAG, "connectToClipMgr: " + clip);

        if (connectToClipMgr(clip)) {
            return null;
        }
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
