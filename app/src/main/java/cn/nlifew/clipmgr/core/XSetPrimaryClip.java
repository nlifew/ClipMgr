package cn.nlifew.clipmgr.core;

import android.app.Activity;
import android.app.ActivityThread;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;

import androidx.annotation.NonNull;

import cn.nlifew.clipmgr.BuildConfig;
import cn.nlifew.clipmgr.bean.ActionRecord;
import cn.nlifew.clipmgr.bean.PackageRule;
import cn.nlifew.clipmgr.provider.ExportedProvider;
import cn.nlifew.clipmgr.settings.Settings;
import cn.nlifew.clipmgr.ui.request.OnRequestFinishListener;
import cn.nlifew.clipmgr.ui.request.RequestActivity;
import cn.nlifew.clipmgr.util.ClipUtils;
import cn.nlifew.clipmgr.util.DirtyUtils;
import cn.nlifew.clipmgr.util.ToastUtils;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;

public class XSetPrimaryClip extends XC_MethodHook {
    private static final String MY_PACKAGE_NAME = BuildConfig.APPLICATION_ID;


    private final class WatchDog extends BroadcastReceiver {

        WatchDog(Context context) {
            IntentFilter filter = new IntentFilter(RequestActivity.ACTION_ACTIVITY_HAS_FOCUS);
            context.registerReceiver(this, filter);
            mContext = context;
        }

        private final Context mContext;

        private final Handler mH = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                mWorkingClipData = null;
                ToastUtils.getInstance(mContext).show("开启 ClipMgr 弹窗失败");
            }
        };

        @Override
        public void onReceive(Context context, Intent intent) {
            XposedBridge.log("onReceive: ok");
            mH.removeCallbacksAndMessages(null);
        }

        void watch() {
            mH.sendMessageDelayed(Message.obtain(), 5000);
        }
    }

    private final class RequestFinishCallback extends OnRequestFinishListener {

        RequestFinishCallback(Context context, ClipData clipData) {
            mQuickShot = clipData;
            mContext = context;
        }

        private final Context mContext;
        private final ClipData mQuickShot;

        private boolean handleRequestResult(ClipData clipData, int result) {

            final boolean clipboardChanged;
            final int packageRule;

            if ((result & RESULT_NEGATIVE) != 0) {      // 禁止访问剪贴板
                clipboardChanged = false;
                packageRule = PackageRule.RULE_DENY;
                saveActionRecord(mContext, clipData, ActionRecord.ACTION_DENY);
            }
            else {                                      // 只要没有明确禁止，就允许访问
                clipboardChanged = true;
                packageRule = PackageRule.RULE_GRANT;
                mWriteListClipData = clipData;
                ClipUtils.setPrimaryClip(mContext, clipData);
                saveActionRecord(mContext, clipData, ActionRecord.ACTION_GRANT);
            }

            if ((result & RESULT_REMEMBER) != 0) {
                savePackageRule(mContext, mContext.getPackageName(), packageRule);
            }
            return clipboardChanged;
        }

        @Override
        public void onRequestFinish(int result) throws RemoteException {
            XposedBridge.log("onRequestFinish: result: " + result);

            if (mRequestDialog != null) {       // 权限申请发生在本地进程
                handleRequestResult(mWorkingClipData, result);
                mRequestDialog = null;
                mWorkingClipData = null;
                return;
            }

            // 如果权限申请发生在远程，就有点麻烦了
            // 因为此进程可能在申请的过程中将 ClipData 改变
            // 因此优先处理保存的 ClipData 快照

            boolean clipboardChanged = handleRequestResult(mQuickShot, result);
            if (ClipUtils.equals(mQuickShot, mWorkingClipData)) {
                mWorkingClipData = null;
            }
            else if ((result & RESULT_REMEMBER) != 0) {
                handleRequestResult(mWorkingClipData, clipboardChanged ?
                        RESULT_POSITIVE : RESULT_NEGATIVE);
                mWorkingClipData = null;
            }
            else {
                startRequestActivity(mContext, mWorkingClipData);
            }
        }
    }

    private ClipData mWorkingClipData;
    private ClipData mWriteListClipData;
    private AlertDialog mRequestDialog;

    private WatchDog mWatchDog;

    private boolean startRequestActivity(Context context, ClipData clipData) {
        RequestActivity.Builder builder = new RequestActivity.Builder()
                .setPackageName(context.getPackageName())
                .setCancelable(false)
                .setPositive("允许")
                .setNegative("拒绝")
                .setRemember("记住我的选择")
                .setTitle("放开我的剪贴板")
                .setMessage(clip2SimpleText(context, clipData))
                .setCallback(new RequestFinishCallback(context, clipData));
        mWorkingClipData = clipData;

        try {
            Activity activity;
            boolean isRadicalMode = isRadicalMode(context);

            XposedBridge.log("startRequestActivity: radical: " + isRadicalMode);

            if (isRadicalMode && (activity = DirtyUtils.getTopActivity()) != null) {

                // 在本进程展示对话框
                XposedBridge.log("startRequestActivity: show Dialog");

                mRequestDialog = builder
                        .buildDialog(activity)
                        .create();
                mRequestDialog.show();
            }
            else {
                // 在另外一个进程中申请权限
                XposedBridge.log("startRequestActivity: startActivity ...");

                Intent intent = builder.build();
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);

                if (mWatchDog == null) {
                    mWatchDog = new WatchDog(context);
                }
                mWatchDog.watch();
            }
            return true;
        } catch (Exception e) {
            XposedBridge.log(e);
            mWorkingClipData = null;
            return false;
        }
    }

    @Override
    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
        final ClipData clipData = (ClipData) param.args[0];
        final Context context = ActivityThread.currentApplication();
        final String packageName = context.getPackageName();

        XposedBridge.log("beforeHookedMethod: " + packageName + " " + ClipUtils.clip2String(clipData));

        // 过滤掉不该拦截的
        if (ClipUtils.equals(mWriteListClipData, clipData) || MY_PACKAGE_NAME.equals(packageName)) {
            XposedBridge.log("beforeHookedMethod: clip is in whitelist, ignore");
            return;
        }
        mWriteListClipData = null;

        // 如果正在请求权限，更新 ClipData 后退出
        if (mWorkingClipData != null) {
            XposedBridge.log("beforeHookedMethod: someone is working, update");

            mWorkingClipData = clipData;
            if (mRequestDialog != null) {
                mRequestDialog.setMessage(clip2SimpleText(context, clipData));
            }

            param.setResult(null);
            return;
        }

        final int rule = findRuleByPackageName(context, packageName);
        XposedBridge.log("beforeHookedMethod: the rule for " + packageName + " is: " + rule);

        switch (rule) {
            case PackageRule.RULE_GRANT:        // 授权访问剪贴板
                saveActionRecord(context, clipData, ActionRecord.ACTION_GRANT);
                break;
            case PackageRule.RULE_DENY:         // 拒绝访问剪贴板
                saveActionRecord(context, clipData, ActionRecord.ACTION_DENY);
                param.setResult(null);
                break;
            case PackageRule.RULE_REQUEST:      // 请求用户授权
                if (startRequestActivity(context, clipData)) {
                    param.setResult(null);
                }
                break;
        }
    }

    private int findRuleByPackageName(Context context, String packageName) {
        Cursor cursor = null;
        try {
            Uri uri = Uri.parse("content://" + ExportedProvider.AUTHORITY
                    + "/" + ExportedProvider.PATH_PACKAGE_RULE
                    + "/" + packageName);
            cursor = context.getContentResolver().query(uri, null,
                    null, null, null);
            if (cursor != null && cursor.moveToNext()) {
                return cursor.getInt(cursor.getColumnIndex(PackageRule.Column.RULE));
            }
        } catch (Exception e) {
            XposedBridge.log("findRuleByPackageName: " + packageName);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return PackageRule.RULE_REQUEST;
    }

    private void saveActionRecord(Context context, ClipData clipData, int action) {
        String packageName = context.getPackageName();

        ContentValues values = new ContentValues();
        values.put(ActionRecord.Column.PACKAGE, packageName);
        values.put(ActionRecord.Column.ACTION, action);
        values.put(ActionRecord.Column.TEXT, ClipUtils.clip2String(clipData));
        values.put(ActionRecord.Column.TIME, System.currentTimeMillis());


        try {
            PackageManager pm = context.getPackageManager();
            ApplicationInfo info = pm.getApplicationInfo(packageName, 0);
            values.put(ActionRecord.Column.APP_NAME, info.loadLabel(pm).toString());
        } catch (PackageManager.NameNotFoundException e) {
            XposedBridge.log(e);
            values.put(ActionRecord.Column.APP_NAME, packageName);
        }

        Uri uri = Uri.parse("content://" + ExportedProvider.AUTHORITY
                + "/" + ExportedProvider.PATH_ACTION_RECORD);
        context.getContentResolver().insert(uri, values);
    }

    private void savePackageRule(Context context, String packageName, int rule) {
        ContentResolver resolver = context.getContentResolver();

        Uri uri = Uri.parse("content://" + ExportedProvider.AUTHORITY
                + "/" + ExportedProvider.PATH_PACKAGE_RULE
                + "/" + packageName);
        resolver.delete(uri, null, null);

        ContentValues values = new ContentValues();
        values.put(PackageRule.Column.PACKAGE, packageName);
        values.put(PackageRule.Column.RULE, rule);

        resolver.insert(uri, values);
    }

    private boolean isRadicalMode(Context context) {
        Cursor cursor = null;
        try {
            Uri uri = Uri.parse("content://" + ExportedProvider.AUTHORITY
                    + "/" + ExportedProvider.PATH_SETTINGS);
            cursor = context.getContentResolver().query(uri,
                    null, null, null, null);
            if (cursor != null && cursor.moveToNext()) {
                int index = cursor.getColumnIndex(Settings.KEY_RADICAL_MODE);
                String value = cursor.getString(index);

                XposedBridge.log("isRadicalMode: index: " + index + " value: " + value);
                return "true".equals(value);
            }
        } catch (Exception e) {
            XposedBridge.log("isRadicalMode: " + e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        // 默认不使用激进模式
        return false;
    }

    private String clip2SimpleText(Context context, ClipData clipData) {
        PackageManager pm = context.getPackageManager();
        StringBuilder sb = new StringBuilder(64)
                .append("\n");

        try {
            ApplicationInfo info = pm.getApplicationInfo(context.getPackageName(), 0);
            sb.append(info.loadLabel(pm));
        } catch (PackageManager.NameNotFoundException e) {
            XposedBridge.log("clip2SimpleText: " + e);
            sb.append(context.getPackageName());
        }
        sb.append("尝试修改剪贴板为：");
        ClipUtils.clip2SimpleString(clipData, sb);
        return sb.toString();
    }
}
