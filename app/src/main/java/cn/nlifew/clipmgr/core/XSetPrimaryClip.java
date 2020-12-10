package cn.nlifew.clipmgr.core;

import android.app.Activity;
import android.app.ActivityThread;
import android.app.AlertDialog;
import android.app.Application;
import android.content.IClipboard;
import android.content.ClipData;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;


import androidx.annotation.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;

import cn.nlifew.clipmgr.bean.ActionRecord;
import cn.nlifew.clipmgr.bean.PackageRule;
import cn.nlifew.clipmgr.provider.ExportedProvider;
import cn.nlifew.clipmgr.ui.request.OnRequestFinishListener;
import cn.nlifew.clipmgr.ui.request.RequestActivity;
import cn.nlifew.clipmgr.util.ClipUtils;
import cn.nlifew.clipmgr.util.DirtyUtils;
import de.robv.android.xposed.XposedBridge;

import static cn.nlifew.clipmgr.core.ClipHook.MY_PACKAGE_NAME;

final class XSetPrimaryClip {
    private static final String TAG = "XSetPrimaryClip";

    private static final class InvokeWrapper {
        InvokeWrapper(IClipboard obj, Method method, Object[] args) {
            mMethod = method;
            mArgs = args;
            mObject = obj;
        }

        private final IClipboard mObject;
        private final Method mMethod;
        private final Object[] mArgs;
        private ClipData mClipData;

        ClipData getClipData() {
            if (mClipData == null) {
                for (Object obj : mArgs) {
                    if (obj instanceof ClipData) {
                        mClipData = (ClipData) obj;
                        break;
                    }
                }
            }
            return mClipData;
        }

        void invoke() {
            try {
                mMethod.invoke(mObject, mArgs);
            } catch (IllegalAccessException
                    | IllegalArgumentException
                    | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public boolean equals(@Nullable Object obj) {
            if (obj == this) return true;
            if (! (obj instanceof InvokeWrapper)) return false;

            InvokeWrapper o = (InvokeWrapper) obj;
            return ClipUtils.equals(getClipData(), o.getClipData());
        }
    }

    private final class RequestFinishCallback extends OnRequestFinishListener {

        @Override
        public void onRequestFinish(int result) throws RemoteException {
            XposedBridge.log("onRequestFinish: result = " + result);
            mRequestDialog = null; // 立即置空防止 setPrimaryClip 拦截

            int packageRule = PackageRule.RULE_REQUEST;
            final ClipData clipData = mWaitingInvoke.getClipData();

            if ((result & RESULT_NEGATIVE) != 0) {      // 禁止访问剪贴板
                packageRule = PackageRule.RULE_DENY;
                saveActionRecord(mContext, mPackageName, clipData, ActionRecord.ACTION_DENY);
                mWaitingInvoke = null;
            }
            else if ((result & RESULT_POSITIVE) != 0) { // 允许访问剪贴板
                packageRule = PackageRule.RULE_GRANT;
                saveActionRecord(mContext, mPackageName, clipData, ActionRecord.ACTION_GRANT);
                mWaitingInvoke.invoke();
            }

            if ((result & RESULT_REMEMBER) != 0) {
                savePackageRule(mContext, mContext.getPackageName(), packageRule);
            }

            XposedBridge.log("onRequestFinish: end");
        }
    }

    XSetPrimaryClip() {
        mContext = ActivityThread.currentApplication();
        mPackageName = mContext.getPackageName();
    }

    private AlertDialog mRequestDialog;
    private final Application mContext;
    private final String mPackageName;
    private InvokeWrapper mWaitingInvoke;

    void setPrimaryClip(IClipboard obj, Method method, Object[] args) {
        final InvokeWrapper wrapper = new InvokeWrapper(obj, method, args);
        final ClipData clipData = wrapper.getClipData();

        // 如果正在展示对话框，更新对话框信息
        if (mRequestDialog != null) {
            XposedBridge.log(TAG + ": setPrimaryClip: dialog is showing, update");
            mWaitingInvoke = wrapper;
            updateRequestDialog(clipData);
            return;
        }

        final int rule = XSetPrimaryClip.getPackageRule(mContext, mPackageName);
        XposedBridge.log(TAG + ": setPrimaryClip: the rule of " + mPackageName +
                " is " + rule);

        switch (rule) {
            case PackageRule.RULE_DENY:         // 授权访问剪贴板
                XSetPrimaryClip.saveActionRecord(mContext, mPackageName,
                        clipData, ActionRecord.ACTION_DENY);
                wrapper.invoke();
                break;
            case PackageRule.RULE_GRANT:        // 拒绝访问剪贴板
                XSetPrimaryClip.saveActionRecord(mContext, mPackageName,
                        clipData, ActionRecord.ACTION_GRANT);
                break;
            case PackageRule.RULE_REQUEST:      // 请求用户授权
                Activity activity = DirtyUtils.getTopActivity();
                XposedBridge.log(TAG + ": setPrimaryClip: TopActivity " + activity);

                if (activity != null) {
                    mWaitingInvoke = wrapper;
                    showRequestDialog(activity, clipData);
                }
                else {
                    /* 由于某些原因无法找到活动的 Activity，拦截掉此次操作
                     * 可能的失败原因：
                     * 1. Android 9 及以上限制了反射
                     * 2. 厂商对 ActivityThread 进行了修改
                     * 3. 本来就没有活动 Activity
                     */
                    saveActionRecord(mContext, mPackageName, clipData, ActionRecord.ACTION_DENY);
                }
                break;
        }
    }

    private void updateRequestDialog(ClipData clipData) {
        mRequestDialog.setMessage(XSetPrimaryClip.clip2SimpleText(clipData));
    }

    private void showRequestDialog(Activity activity, ClipData clipData) {
        ApplicationInfo info = mContext.getApplicationInfo();
        mRequestDialog = new RequestActivity.Builder()
                .setPackageName(mPackageName)
                .setCancelable(false)
                .setPositive("允许")
                .setNegative("拒绝")
                .setRemember("记住我的选择")
                .setMessage(clip2SimpleText(clipData))
                .setCallback(new RequestFinishCallback())
                .buildDialog(activity)
                .setIcon(info.icon)
                .setTitle(info.labelRes)
                .show();
    }

    private static int getPackageRule(Context context, String packageName) {
        if (Objects.equals(MY_PACKAGE_NAME, packageName)) {
            return PackageRule.RULE_GRANT;
        }

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

    private static void saveActionRecord(Context context, String packageName,
                                 ClipData clipData, int action) {

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
}