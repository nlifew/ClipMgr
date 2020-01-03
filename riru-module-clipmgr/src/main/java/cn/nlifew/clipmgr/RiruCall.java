package cn.nlifew.clipmgr;


import android.os.IBinder;
import android.os.ServiceManager;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.Map;

@SuppressWarnings("unused")
public class RiruCall {
    private static final String TAG = "RiruCall";
    private static final String MY_PACKAGE_NAME = BuildConfig.APPLICATION_ID;


    private static String sAppDataDir;

    public static void onPrepareForkAndSpecialize(String appDataDir) {
        Log.i(TAG, "onPrepareForkAndSpecialize: " + appDataDir);
        sAppDataDir = appDataDir;
    }

    public static void onFinishForkAndSpecialize() {
        Log.i(TAG, "onFinishForkAndSpecialize: " + sAppDataDir);
        if (sAppDataDir.endsWith(MY_PACKAGE_NAME)) {
            Log.i(TAG, "onFinishForkAndSpecialize: ignore our manager");
        }
        else {
            hookAndReplaceServiceMap();
        }
    }

    public static void onPrepareForkSystemServer() {
        Log.i(TAG, "onPrepareForkSystemServer: start");
    }

    public static void onFinishForkSystemServer() {
        Log.i(TAG, "onFinishForkSystemServer: start");
    }

    @SuppressWarnings("unchecked")
    private static void hookAndReplaceServiceMap() {

        try {
            Field sCacheField = ServiceManager.class
                    .getDeclaredField("sCache");
            sCacheField.setAccessible(true);

            Map<String, IBinder> sCache = (Map<String, IBinder>)
                    sCacheField.get(null);
            HashMapProxy spy = new HashMapProxy(sCache);

            sCacheField.set(null, spy);
            sCacheField.setAccessible(false);
        } catch (Exception e) {
            Log.e(TAG, "hookAndReplaceClipBinder: ", e);
        }
    }
}
