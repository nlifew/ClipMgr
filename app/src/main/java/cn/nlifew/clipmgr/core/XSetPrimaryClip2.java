package cn.nlifew.clipmgr.core;

import android.app.ActivityThread;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IClipboard;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

import androidx.core.content.ContextCompat;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

import cn.nlifew.clipmgr.bean.ActionRecord;
import cn.nlifew.clipmgr.bean.PackageRule;
import cn.nlifew.clipmgr.ui.request.OnRequestFinishListener;
import cn.nlifew.clipmgr.ui.request.SystemRequestDialog;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;

import static cn.nlifew.clipmgr.core.Helper.clip2SimpleText;
import static cn.nlifew.clipmgr.core.Helper.getCallingPackage;
import static cn.nlifew.clipmgr.core.Helper.savePackageRule;
import static cn.nlifew.clipmgr.ui.request.OnRequestFinishListener.RESULT_POSITIVE;
import static cn.nlifew.clipmgr.ui.request.OnRequestFinishListener.RESULT_REMEMBER;
import static cn.nlifew.clipmgr.util.PackageUtils.getApplicationInfo;
import static cn.nlifew.clipmgr.core.Helper.getPackageRule;
import static cn.nlifew.clipmgr.core.Helper.saveActionRecord;

final class XSetPrimaryClip2 extends XC_MethodHook {
    private static final String TAG = "XSetPrimaryClip2";

    private static Method findAndCheckMethod(IBinder service) {
        for (Method method : service.getClass().getDeclaredMethods()) {
            if ((method.getModifiers() & Modifier.PUBLIC) == 0) {
                continue;
            }

            if (! "setPrimaryClip".equals(method.getName())) {
                continue;
            }

            Class<?>[] params = method.getParameterTypes();
            Class<?> result = method.getReturnType();
            if (params[0] == ClipData.class && result == void.class) {
                return method;
            }
        }
        return null;
    }


    XSetPrimaryClip2(IBinder service) {
        mThis = service;
        mSetPrimaryClip = findAndCheckMethod(service);
    }


    private final Object mThis;
    private final Method mSetPrimaryClip;

    private SystemRequestDialog mRequestDialog;

    private final Handler mH = new Handler(Looper.getMainLooper());
    private final ArrayList<PendingTransaction> mPendingQueue
            = new ArrayList<>(4); // is better than LinkedList ?


    private boolean shouldIgnoreThisCall(Object[] args) {
        if (mSetPrimaryClip == null) {
            return true;
        }

        // 保证第一个参数是 ClipData
        if (! (args[0] instanceof ClipData)) {
            return true;
        }

        // 保证不会递归拦截
        if (mPendingQueue.size() > 0 && Arrays.equals(mPendingQueue.get(0).args, args)) {
            return true;
        }

        return false;
    }

    @Override
    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
        if (shouldIgnoreThisCall(param.args)) {
            return;
        }

        final ClipData clipData = (ClipData) param.args[0];


        final Context context = ActivityThread.currentApplication();
        final String packageName = getCallingPackage(context);
        final int rule = getPackageRule(context, packageName);

        XposedBridge.log(TAG + ": the rule of " + packageName + " is " + rule);

        switch (rule) {
            case PackageRule.RULE_GRANT:    // 授权访问剪贴板
                saveActionRecord(context, packageName, clipData, ActionRecord.ACTION_GRANT);
                break;
            case PackageRule.RULE_DENY:     // 禁止访问剪贴板
                saveActionRecord(context, packageName, clipData, ActionRecord.ACTION_DENY);
                param.setResult(null);
                break;
            case PackageRule.RULE_REQUEST:  // 弹出授权对话框
                param.setResult(null);

                PendingTransaction pt = new PendingTransaction(mSetPrimaryClip, mThis, param.args);
                pt.context = context;
                pt.packageName = packageName;
                pt.clipData = clipData;
                mH.post(() -> showRequestDialog(pt));
                break;
        }
    }

    private void showRequestDialog(PendingTransaction pending) {
        // 如果正在弹窗，只需要加进任务队列
        if (mRequestDialog != null) {
            // 遍历当前队列，找到包名相同的任务，删掉它
            int old = -1;

            for (int i = 0, n = mPendingQueue.size(); i < n; i++) {
                PendingTransaction it = mPendingQueue.get(i);
                if (Objects.equals(it.packageName, pending.packageName)) {
                    old = i;
                    mPendingQueue.remove(old);
                    break;
                }
            }
            mPendingQueue.add(pending);
            if (old == 0) {
                ClipData clipData = pending.clipData;
                mRequestDialog.setMessage(clip2SimpleText(clipData));
            }
            return;
        }

        mPendingQueue.add(pending);

        mRequestDialog = new SystemRequestDialog(pending.context);
        applyParam(mRequestDialog, pending);
        mRequestDialog.show();
    }

    private void applyParam(SystemRequestDialog dialog, PendingTransaction pending) {
        final Context context = pending.context;
        final String packageName = pending.packageName;
        final ClipData clipData = pending.clipData;

        PackageManager pm = context.getPackageManager();
        ApplicationInfo info = getApplicationInfo(pm, packageName);

        CallbackImpl callback = new CallbackImpl();

        dialog.setTitle(info != null ? info.loadLabel(pm) :
                context.getString(android.R.string.unknownName));
        dialog.setIcon(info != null ? info.loadIcon(pm) :
                ContextCompat.getDrawable(context, android.R.drawable.sym_def_app_icon));

        dialog.setCancelable(false);
        dialog.setMessage(clip2SimpleText(clipData));
        dialog.setButton(DialogInterface.BUTTON_POSITIVE, "允许", callback);
        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "拒绝", callback);
    }

    private static final class PendingTransaction {
        private static final String TAG = "PendingTransaction";

        public PendingTransaction(Method method, Object obj, Object[] args) {
            this.method = method;
            this.object = obj;
            this.args = args;

            this.identity = Binder.clearCallingIdentity();
        }

        final Method method;
        final Object object;
        final Object[] args;
        final long identity;

        Context context;
        String packageName;
        ClipData clipData;

        void transact() {
            Binder.restoreCallingIdentity(identity);

            try {
                method.invoke(object, args);
            } catch (Throwable t) {
                XposedBridge.log(TAG + ": transact: failed");
                XposedBridge.log(t);
            }
        }
    }


    private class CallbackImpl extends SystemRequestDialog.Callback {
        private static final String TAG = "CallbackImpl";

        @Override
        public void onRequestFinish(int result) {
            try {
                handleResult(result);
            } catch (Throwable t) {
                XposedBridge.log(TAG + ": onRequestFinish: failed");
                XposedBridge.log(t);
            }

            // 从队列中移除
            mRequestDialog = null;
            mPendingQueue.remove(0);

            // 可能其它 app 也需要修改剪贴板
            if (mPendingQueue.size() != 0) {
                PendingTransaction p = mPendingQueue.remove(0);
                mH.post(() -> showRequestDialog(p));
            }
        }

        private void handleResult(int result) {
            XposedBridge.log(TAG + ": handleResult: received result: " + result);

            // 先不要移除，用来防止递归拦截
            final PendingTransaction pending = mPendingQueue.get(0);

            final int packageRule;
            final Context context = pending.context;
            final String packageName = pending.packageName;
            final ClipData clipData = pending.clipData;

            if ((result & RESULT_POSITIVE) != 0) {      // 允许访问剪贴板
                packageRule = PackageRule.RULE_GRANT;
                saveActionRecord(context, packageName, clipData, ActionRecord.ACTION_GRANT);
                pending.transact();
            }
            else {  // 只要没有明确允许，就是拒绝
                packageRule = PackageRule.RULE_DENY;
                saveActionRecord(context, packageName, clipData, ActionRecord.ACTION_DENY);
            }

            if ((result & RESULT_REMEMBER) != 0) {
                savePackageRule(context, packageName, packageRule);
            }

            XposedBridge.log(TAG + ": handleResult: done");
        }
    }
}
