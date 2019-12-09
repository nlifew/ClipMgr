package cn.nlifew.clipmgr.core;


import android.app.ActivityThread;
import android.content.ClipData;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;

import cn.nlifew.clipmgr.service.ClipMgrService;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class ClipHook implements IXposedHookLoadPackage {
    private static final String TAG = "ClipHook";
    private static final String MY_PACKAGE_NAME = "cn.nlifew.clipmgr";

    public ClipHook() {
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam param) throws Throwable {
        XposedBridge.log(TAG + " package: " + param.packageName);

        if (MY_PACKAGE_NAME.equals(param.packageName)) {
            // 我 信 我自己
            return;
        }

        XposedHelpers.findAndHookMethod(
                "android.content.ClipboardManager",
                param.classLoader,
                "setPrimaryClip",
                ClipData.class,
                new HookSetPrimaryClipMethod());
    }


    private static final class HookSetPrimaryClipMethod extends XC_MethodHook {

        @Override
        protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
            XposedBridge.log("start");

            final Context c = ActivityThread.currentApplication();
            final ClipData clip = (ClipData) param.args[0];

            Intent intent = new Intent();
            intent.setComponent(new ComponentName(MY_PACKAGE_NAME,
                    MY_PACKAGE_NAME + ".service.ClipMgrService"));

            final ServiceConnection conn = new ServiceConnection() {

                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    IClipMgr mgr = IClipMgr.Stub.asInterface(service);
                    try {
                        mgr.setPrimaryClip(c.getPackageName(), clip);
                    } catch (Exception e) {
                        XposedBridge.log(e);
                    }
                    c.unbindService(this);
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {

                }
            };

            if (! c.bindService(intent, conn, Context.BIND_AUTO_CREATE)) {
                XposedBridge.log("bind ClipMgrService failed");
            } else {
                param.setResult(null);
            }
        }
    }
}
