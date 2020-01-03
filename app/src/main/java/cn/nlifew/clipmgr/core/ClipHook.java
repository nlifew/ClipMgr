package cn.nlifew.clipmgr.core;


import android.app.Activity;
import android.app.ActivityThread;
import android.app.Application;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.Map;

import cn.nlifew.clipmgr.BuildConfig;
import cn.nlifew.clipmgr.util.ReflectUtils;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class ClipHook implements IXposedHookLoadPackage {
    private static final String TAG = "ClipHook";
    private static final String MY_PACKAGE_NAME = BuildConfig.APPLICATION_ID;
    private static final String MY_SERVICE_NAME = ".service.ClipMgrService";


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
                new HookSetPrimaryClipMethod()
        );
    }

    private static final class HookSetPrimaryClipMethod extends XC_MethodHook {

        private Context mContext;
        private final LinkedList<ClipData> mCaches = new LinkedList<>();

        /**
         * 这个方法用于拿到当前应用的 Context
         * 这里我们先使用反射拿到 ClipboardManager 的 Context，
         * 如果失败再尝试 ThreadActivity.currentApplication()
         * 原因是 flyme8 似乎修改了相关 API，使得后者返回我们自己的 Context
         * @param cm ClipboardManager
         * @return ApplicationContext
         */
        private Context getApplicationContext(ClipboardManager cm) {
            if (mContext != null) {
                return mContext;
            }
            // 通过反射，拿到 ClipboardManager 内的 Context
            Field cxt = ReflectUtils.getDeclaredField(ClipboardManager.class, "mContext");
            if (cxt != null) {
                try {
                    mContext = ((Context) cxt.get(cm)).getApplicationContext();
                    return mContext;
                } catch (Exception e) {
                    XposedBridge.log(e);
                }
            }
            // 如果反射失败，通过隐藏 API 拿到 Context
            mContext = ActivityThread.currentApplication();
            return mContext;
        }

        @Override
        protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {

            /* 这里通过绑定服务的方式跳转到我们自己的进程
             * 需要注意的一点是，我们并没有直接使用传入的 ClipData，
             * 而是把 ClipData 先放在一个缓存池中，回调时再取出来。
             * 原因在于：ServiceConnection 的回调次序和 bindService() 之间并不严格对应
             * 举个例子，某个 app 先把剪贴板设置为 1，再马上设置为 2，
             * 正常情况下，应该先处理 1 的请求，再处理 2。
             * 但很多时候是 2 的 ServiceConnection 先回调，从而导致剪贴板数据异常
             */

            final Context c = getApplicationContext((ClipboardManager) param.thisObject);
            final ClipData clip = (ClipData) param.args[0];

            XposedBridge.log( c.getPackageName() + " " + clip);


            /* bugfix: 虽然已经在 handleLoadPackage() 里排除了自己，
             * 但在 Flyme8 上仍然会出现套娃，不知道为什么
             * 看起来和它自带的剪贴板提示有关系
             */
            if (MY_PACKAGE_NAME.equals(c.getPackageName())) {
                XposedBridge.log("wtf: I hook myself ?");
                return;
            }

            Intent intent = new Intent();
            intent.setComponent(new ComponentName(MY_PACKAGE_NAME,
                    MY_PACKAGE_NAME + MY_SERVICE_NAME));

            ServiceConnection conn = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    IClipMgr mgr = IClipMgr.Stub.asInterface(service);
                    try {
                        ClipData clipData = mCaches.removeFirst();
                        mgr.setPrimaryClip(c.getPackageName(), clipData);
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
                XposedBridge.log(c.getPackageName() + "failed to bind service");
                return;
            }
            mCaches.addLast(clip);
            param.setResult(null);
        }
    }
}
