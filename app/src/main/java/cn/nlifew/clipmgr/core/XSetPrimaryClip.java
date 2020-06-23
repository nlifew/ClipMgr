package cn.nlifew.clipmgr.core;

import android.app.Activity;
import android.app.ActivityThread;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.system.Os;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

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


    private final class RequestFinishCallback extends OnRequestFinishListener {

        RequestFinishCallback(Context context) {
            mContext = context;
        }

        private final Context mContext;


        @Override
        public void onRequestFinish(int result) throws RemoteException {
            XposedBridge.log("onRequestFinish: result = " + result);
            mRequestDialog = null; // 立即置空防止 setPrimaryClip 拦截

            final int packageRule;

            if ((result & RESULT_NEGATIVE) != 0) {      // 禁止访问剪贴板
                packageRule = PackageRule.RULE_DENY;
                saveActionRecord(mContext, mWorkingClipData, ActionRecord.ACTION_DENY);
                mWorkingClipData = null;
            }
            else {                                      // 只要没有明确禁止，就允许访问
                packageRule = PackageRule.RULE_GRANT;
                ClipUtils.setPrimaryClip(mContext, mWorkingClipData);
                saveActionRecord(mContext, mWorkingClipData, ActionRecord.ACTION_GRANT);
            }

            if ((result & RESULT_REMEMBER) != 0) {
                savePackageRule(mContext, mContext.getPackageName(), packageRule);
            }

            XposedBridge.log("onRequestFinish: end");
        }
    }

    private AlertDialog mRequestDialog;
    private static ClipData mWorkingClipData;

    private boolean startRequestActivity(Context context, ClipData clipData) {
        // 尝试从 ActivityThread 中找到活动的 Activity
        Activity activity = DirtyUtils.getTopActivity();
        if (activity != null) {
            XposedBridge.log("startRequestActivity: TopActivity found");

            mWorkingClipData = clipData;

            ApplicationInfo info = context.getApplicationInfo();

            mRequestDialog = new RequestActivity.Builder()
                    .setPackageName(context.getPackageName())
                    .setCancelable(false)
                    .setPositive("允许")
                    .setNegative("拒绝")
                    .setRemember("记住我的选择")
                    .setMessage(clip2SimpleText(clipData))
                    .setCallback(new RequestFinishCallback(context))
                    .buildDialog(activity)
                    .setIcon(info.icon)
                    .setTitle(info.labelRes)
                    .show();
            return true;
        }
        /* 由于某些原因无法找到活动的 Activity，
         * 拦截掉此次操作
         * 可能的失败原因：
         * 1. Android 9 及以上限制了反射
         * 2. 厂商对 ActivityThread 进行了修改
         * 3. 本来就没有活动 Activity
         */
        XposedBridge.log("startRequestActivity: TopActivity NOT found");
        saveActionRecord(context, clipData, ActionRecord.ACTION_DENY);
        return true;
    }

    @Override
    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
        final ClipData clipData = (ClipData) param.args[0];
        final Context context = ActivityThread.currentApplication();
        final String packageName = context.getPackageName();

        logMethodParam(param);

        // 过滤掉不该拦截的
        if (ClipUtils.equals(mWorkingClipData, clipData) ||
                MY_PACKAGE_NAME.equals(packageName)) {
            XposedBridge.log("beforeHookedMethod: clip data is in whitelist, return");
            return;
        }
        // 如果正在展示对话框，更新对话框
        if (mRequestDialog != null) {
            XposedBridge.log("beforeHookedMethod: dialog is showing, update");

            mRequestDialog.setMessage(clip2SimpleText(clipData));
            mWorkingClipData = clipData;
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

    private static int findRuleByPackageName(Context context, String packageName) {
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

    private static void saveActionRecord(Context context, ClipData clipData, int action) {
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

    private static void savePackageRule(Context context, String packageName, int rule) {
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

    private static String clip2SimpleText(ClipData clipData) {
        StringBuilder sb = new StringBuilder(64)
                .append("\n尝试修改剪贴板为：");
        ClipUtils.clip2SimpleString(clipData, sb);
        return sb.toString();
    }

    private static void logMethodParam(MethodHookParam param) {
        Context context = ActivityThread.currentApplication();
        ClipData clipData = (ClipData) param.args[0];

        XposedBridge.log("logMethodParam: " +
                context.getPackageName() + ":" + Os.getpid() + " " +
                "[" + clipData + "/" + mWorkingClipData + "] = [" +
                ClipUtils.equals(clipData, mWorkingClipData));
    }
}
