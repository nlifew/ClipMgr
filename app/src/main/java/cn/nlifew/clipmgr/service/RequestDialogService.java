package cn.nlifew.clipmgr.service;

import android.app.ActivityThread;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ApplicationInfo;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;

import java.util.ArrayList;
import java.util.Objects;


import cn.nlifew.clipmgr.request.IRequestDialog;
import cn.nlifew.clipmgr.request.IRequestFinish;
import cn.nlifew.clipmgr.request.OnRequestFinishListener;
import cn.nlifew.clipmgr.request.RequestDialogParam;
import cn.nlifew.clipmgr.ui.request.SystemRequestDialog;
import cn.nlifew.clipmgr.util.PackageUtils;
import de.robv.android.xposed.XposedBridge;


public class RequestDialogService extends IRequestDialog.Stub {
    private static final String TAG = "RequestDialogServer";

    public static final String NAME = "clipmgr_bridge";

    private SystemRequestDialog mRequestDialog;

    private final Handler mH = new Handler(Looper.getMainLooper());
    private final ArrayList<PendingTransaction> mPendingQueue
            = new ArrayList<>(4); // is better than LinkedList ?


    @Override
    public void show(String packageName, RequestDialogParam param) throws RemoteException {
        PendingTransaction pending = new PendingTransaction(param, packageName);
        mH.post(() -> show(pending));
    }

    private void show(PendingTransaction pending) {
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
                applyParam(mRequestDialog, pending);
            }
            return;
        }

        mPendingQueue.add(pending);

        Context context = ActivityThread.currentApplication();
        mRequestDialog = new SystemRequestDialog(context);
        applyParam(mRequestDialog, pending);

        mRequestDialog.show();
    }

    private void applyParam(SystemRequestDialog dialog,
                            PendingTransaction pending) {
        final RequestDialogParam param = pending.param;
        final ApplicationInfo info = PackageUtils.getApplicationInfo(
                dialog.getContext().getPackageManager(),
                pending.packageName
        );

        if (param.title != null) {
            dialog.setTitle(param.title);
        }
        if (param.message != null) {
            dialog.setMessage(param.message);
        }
        if (param.icon && info != null) {
            Context context = dialog.getContext();
            dialog.setIcon(info.loadIcon(context.getPackageManager()));
        }

        Callback callback = new Callback(param.callback);

        if (param.positive != null) {
            dialog.setButton(DialogInterface.BUTTON_POSITIVE, param.positive, callback);
        }
        if (param.negative != null) {
            dialog.setButton(DialogInterface.BUTTON_NEGATIVE, param.negative, callback);
        }

        dialog.setCancelable(param.cancelable);
        dialog.setOnCancelListener(callback);
    }

    private static final class PendingTransaction {
        final RequestDialogParam param;
        final String packageName;

        PendingTransaction(RequestDialogParam param, String packageName) {
            this.param = param;
            this.packageName = packageName;
        }
    }

    private class Callback implements
            DialogInterface.OnClickListener,
            DialogInterface.OnCancelListener,
            DialogInterface.OnDismissListener {

        Callback(IRequestFinish listener) {
            mRequestFinishListener = listener;
        }

        private final IRequestFinish mRequestFinishListener;

        @Override
        public void onCancel(DialogInterface dialog) {
            dialog.dismiss();
            performCallback(dialog, OnRequestFinishListener.RESULT_CANCEL);
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    performCallback(dialog, OnRequestFinishListener.RESULT_POSITIVE);
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    performCallback(dialog, OnRequestFinishListener.RESULT_NEGATIVE);
                    break;
            }
        }

        @Override
        public void onDismiss(DialogInterface dialog) {

        }

        private void performCallback(DialogInterface d, int result) {
            XposedBridge.log(TAG + ": performCallback: result = " + result);

            SystemRequestDialog dialog = (SystemRequestDialog) d;
            if (dialog.isRememberChecked()) {
                result |= OnRequestFinishListener.RESULT_REMEMBER;
            }

            if (mRequestFinishListener != null) {
                try {
                    mRequestFinishListener.onRequestFinish(result);
                } catch (Throwable t) {
                    XposedBridge.log(TAG + ": performCallback: remote exp");
                    XposedBridge.log(t);
                }
            }

            // 从队列中移除
            mRequestDialog = null;
            mPendingQueue.remove(0);

            // 可能其它 app 也需要修改剪贴板
            if (mPendingQueue.size() != 0) {
                PendingTransaction p = mPendingQueue.remove(0);
                mH.post(() -> show(p));
            }

            XposedBridge.log(TAG + ": performCallback: done");
        }
    }
}
