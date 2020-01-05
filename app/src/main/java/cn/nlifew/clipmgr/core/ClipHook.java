package cn.nlifew.clipmgr.core;


import android.app.Activity;
import android.app.ActivityThread;
import android.content.ClipData;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import java.lang.reflect.Field;
import java.util.Map;

import cn.nlifew.clipmgr.BuildConfig;
import cn.nlifew.clipmgr.bean.ActionRecord;
import cn.nlifew.clipmgr.bean.PackageRule;
import cn.nlifew.clipmgr.ui.request.OnRequestFinishListener;
import cn.nlifew.clipmgr.ui.request.RequestActivity;
import cn.nlifew.clipmgr.util.PackageUtils;
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

    private static final class HookSetPrimaryClipMethod extends XC_MethodHook implements
            ServiceConnection {
        private static boolean sIgnore;
        private static Field sActivitiesField;
        private static Field sStoppedField;
        private static Field sActivityField;


        @SuppressWarnings("unchecked")
        private static Activity getTopActivity() {
            if (sIgnore) {
                return null;
            }
            try {
                if (sActivitiesField == null) {
                    sActivitiesField = ReflectUtils.getDeclaredField(
                            ActivityThread.class, "mActivities"
                    );
                    Class<?> cls = Class.forName("android.app.ActivityThread$ActivityClientRecord");
                    sActivityField = ReflectUtils.getDeclaredField(cls, "activity");
                    sStoppedField = ReflectUtils.getDeclaredField(cls, "stopped");
                }
                ActivityThread thread = ActivityThread.currentActivityThread();
                final Map<Object, Object> mActivities = (Map<Object, Object>)
                        sActivitiesField.get(thread);

                for (Object r : mActivities.values()) {
                    Activity activity = (Activity) sActivityField.get(r);
                    if (activity.isFinishing() || activity.isDestroyed()
                            || ((boolean) sStoppedField.get(r))) {
                        continue;
                    }
                    return activity;
                }
            } catch (Exception e) {
                sIgnore = true;
                Log.e(TAG, "getTopActivity: ", e);
            }
            return null;
        }

        @Override
        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
            Log.i(TAG, "beforeHookedMethod: start");

            mContext = ActivityThread.currentApplication();
            ClipData clip = (ClipData) param.args[0];

            final String pkg = mContext.getPackageName();
            Log.i(TAG, "beforeHookedMethod: " + pkg + " " + clip);

            if (MY_PACKAGE_NAME.equals(pkg)) {
                Log.w(TAG, "beforeHookedMethod: ignore hook myself");
                return;
            }

            if (mWorkingClipData != null) {
                // 上次的活还没干完，这次又来新的活了，资本家呵呵
                // 直接覆盖掉上次的数据，毕竟剪贴板留新不留旧
                mWorkingClipData = clip;
                param.setResult(null);
                return;
            }
            // 接下来准备绑定服务和远程连接
            Intent intent = new Intent();
            intent.setComponent(new ComponentName(
                    MY_PACKAGE_NAME, MY_PACKAGE_NAME + MY_SERVICE_NAME));
            int flag = Context.BIND_AUTO_CREATE | Context.BIND_ABOVE_CLIENT;

            if (! mContext.bindService(intent, this, flag)) {
                Log.w(TAG, "beforeHookedMethod: bind Service failed !");
                return;
            }
            mWorkingClipData = clip;
            param.setResult(null);
        }

        private Context mContext;
        private ClipData mWorkingClipData;


        private boolean startRequestActivity(final IClipMgr mgr,
                                             final ClipData clip) {
            final String pkg = mContext.getPackageName();

            StringBuilder msg = new StringBuilder(64);
            msg.append('\n')
                    .append(PackageUtils.getAppName(mContext, pkg))
                    .append("尝试修改剪贴板为: ");
            for (int i = 0, n = clip == null ? 0 : clip.getItemCount(); i < n; i++) {
                msg.append(clip.getItemAt(i).getText());
            }

            OnRequestFinishListener callback = new OnRequestFinishListener() {
                @Override
                public void onRequestFinish(int result) throws RemoteException {
                    int rule;
                    if ((result & RESULT_NEGATIVE) != 0) {
                        rule = PackageRule.RULE_DENY;
                        mgr.saveActionRecord(pkg, clip, ActionRecord.ACTION_DENY);
                    } else {
                        rule = PackageRule.RULE_GRANT;
                        mgr.saveActionRecord(pkg, clip, ActionRecord.ACTION_GRANT);
                    }
                    if ((result & RESULT_REMEMBER) != 0) {
                        mgr.setPackageRule(pkg, rule);
                    }
                    mContext.unbindService(HookSetPrimaryClipMethod.this);
                }
            };
            RequestActivity.Builder builder = new RequestActivity.Builder()
                    .setTitle("放开我的剪贴板")
                    .setMessage(msg.toString())
                    .setCancelable(false)
                    .setPositive("确定")
                    .setNegative("拒绝")
                    .setRemember("记住我的选择")
                    .setCallback(callback);
            try {
                Activity activity;
                boolean isRadicalMode = mgr.isRadicalMode();
                Log.i(TAG, "startRequestActivity: isRadicalMode: " + isRadicalMode);

                if (mgr.isRadicalMode() && (activity = getTopActivity()) != null) {
                    Log.i(TAG, "startRequestActivity: topActivity: " + activity);
                    builder.buildDialog(activity).show();
                } else {
                    Intent intent = builder.build();
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mContext.startActivity(intent);
                }

                return true;
            } catch (Exception e) {
                Log.e(TAG, "startRequestActivity: ", e);
            }
            return false;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            ClipData clip = mWorkingClipData;
            mWorkingClipData = null;
            String pkg = mContext.getPackageName();

            Log.i(TAG, "onServiceConnected: " + pkg + " " + clip);

            try {
                IClipMgr mgr = IClipMgr.Stub.asInterface(service);
                int rule = mgr.getPackageRule(pkg);
                Log.i(TAG, "onServiceConnected: " + rule);

                switch (rule) {
                    case PackageRule.RULE_REQUEST: {
                        if (startRequestActivity(mgr, clip)) {
                            return; // 直接返回以保留 IBinder
                        }
                        break;
                    }
                    case PackageRule.RULE_GRANT: {
                        mgr.saveActionRecord(pkg, clip, ActionRecord.ACTION_GRANT);
                        break;
                    }
                    case PackageRule.RULE_DENY: {
                        mgr.saveActionRecord(pkg, clip, ActionRecord.ACTION_DENY);
                        break;
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "onServiceConnected: ", e);
            }
            mContext.unbindService(this);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    }
}
