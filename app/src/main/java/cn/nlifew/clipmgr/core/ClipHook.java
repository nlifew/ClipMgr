package cn.nlifew.clipmgr.core;

import android.app.ActivityThread;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.IClipboard;
import android.content.Intent;
import android.os.IBinder;
import android.os.ServiceManager;
import android.system.Os;
import android.util.Log;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Objects;

import cn.nlifew.clipmgr.BuildConfig;
import cn.nlifew.clipmgr.util.ReflectUtils;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class ClipHook implements IXposedHookLoadPackage {
    private static final String TAG = "ClipHook";
    public static final String MY_PACKAGE_NAME = BuildConfig.APPLICATION_ID;
    private static final String CLIPBOARD_MANAGER_IMPL = "ClipMgrImpl";

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam param) throws Throwable {
        XposedBridge.log(TAG + " package: " + param.packageName +
                " pid: " + Os.getpid());

        if (MY_PACKAGE_NAME.equals(param.packageName)) {
            // 我 信 我自己
            return;
        }
        tryHookSystemService(param.classLoader);
    }

    private void tryHookSystemService(ClassLoader cl) {
        Map<String, IBinder> sCache = sCache();
        if (sCache == null) {
            XposedBridge.log(TAG + ": tryHookSystemService: " +
                    "missing Landroid/os/ServiceManager->sCache");
            return;
        }

        if (sCache.containsKey(CLIPBOARD_MANAGER_IMPL)) {
            XposedBridge.log(TAG + ": tryHookSystemService: hook installed, ignore");
            return;
        }

        IBinder impl = ServiceManager.getService(Context.CLIPBOARD_SERVICE);
        sCache.put(CLIPBOARD_MANAGER_IMPL, impl);
        sCache.put(Context.CLIPBOARD_SERVICE, new IClipboardBinder(impl));

        XposedBridge.log(TAG + ": tryHookSystemService: install succeed");
    }

    @SuppressWarnings("unchecked")
    private static Map<String, IBinder> sCache() {
        try {
            Field sCacheField = ReflectUtils
                    .getDeclaredField(ServiceManager.class, "sCache");
            Object map;
            if (sCacheField != null && (map = sCacheField.get(null)) != null) {
                return (Map<String, IBinder>) map;
            }
        } catch (IllegalAccessException e) {
            XposedBridge.log(TAG + ": sCache: " + e);
        }
        return null;
    }
}
