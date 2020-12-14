package cn.nlifew.clipmgr.core;

import android.app.ActivityThread;
import android.content.ClipData;
import android.content.Context;
import android.content.IClipboard;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;

import java.lang.reflect.Method;
import java.util.Objects;

import cn.nlifew.clipmgr.bean.ActionRecord;
import cn.nlifew.clipmgr.bean.PackageRule;
import static cn.nlifew.clipmgr.core.Helper.*;

import cn.nlifew.clipmgr.request.OnRequestFinishListener;
import cn.nlifew.clipmgr.request.RequestDialogManager;
import cn.nlifew.clipmgr.request.RequestDialogParam;
import cn.nlifew.clipmgr.util.PackageUtils;
import cn.nlifew.clipmgr.util.ToastUtils;
import de.robv.android.xposed.XposedBridge;


final class XSetPrimaryClip {
    private static final String TAG = "XSetPrimaryClip";

    private static boolean ERROR = true;

    static {
        try {
            // 遍历找到 setPrimaryClip 函数并比较签名
            Class<IClipboard> clazz = IClipboard.class;
            for (Method method : clazz.getDeclaredMethods()) {
                if (!Objects.equals(method.getName(), "setPrimaryClip")) {
                    continue;
                }
                Class<?>[] params = method.getParameterTypes();
                Class<?> ret = method.getReturnType();
                if (params[0] == ClipData.class && ret == void.class) {
                    ERROR = false;
                }
                break;
            }
        } catch (Throwable t) {
            XposedBridge.log(TAG + ": static: failed to obtain expected #setPrimaryClip");
            XposedBridge.log(t);
        }
    }

    private TransactionWrapper mWorkingTransaction;
    private final Callback mCallback = new Callback();


    boolean setPrimaryClip(IBinder remote, int code, Parcel data, Parcel reply, int flags) throws RemoteException{
        if (ERROR) {
            return remote.transact(code, data, reply, flags);
        }

        final ClipData clipData;
        final int pos = data.dataPosition();
        data.setDataPosition(0);
        data.enforceInterface(IClipBridge.DESCRIPTOR);
        clipData = data.readInt() == 0 ? null : ClipData.CREATOR.createFromParcel(data);
        data.setDataPosition(pos);

        final Context context = ActivityThread.currentApplication();
        final String packageName = context.getPackageName();
        final int rule = getPackageRule(context, packageName);

        XposedBridge.log(TAG + ": the rule of " + packageName + " is " + rule);

        boolean result = true;

        switch (rule) {
            case PackageRule.RULE_GRANT:    // 授权访问剪贴板
                result = remote.transact(code, data, reply, flags);
                saveActionRecord(context, packageName, clipData, ActionRecord.ACTION_GRANT);
                break;
            case PackageRule.RULE_DENY:     // 禁止访问剪贴板
                reply.writeNoException();
                saveActionRecord(context, packageName, clipData, ActionRecord.ACTION_DENY);
                break;
            case PackageRule.RULE_REQUEST:  // 弹出授权对话框
                TransactionWrapper w = new TransactionWrapper(remote, code, data, flags);
                w.context = context;
                w.clipData = clipData;
                w.packageName = packageName;

                mWorkingTransaction = w;
                showRequestDialog(context, packageName, clipData);
                break;
        }
        return result;
    }

    private void showRequestDialog(Context context, String packageName, ClipData clipData) {
        RequestDialogManager dm = RequestDialogManager.getInstance(context);
        if (! dm.available()) {
            ToastUtils.getInstance(context).show("未能获取弹窗服务");
            mCallback.handleResult(OnRequestFinishListener.RESULT_POSITIVE);
            return;
        }

        PackageManager pm = context.getPackageManager();
        ApplicationInfo info = PackageUtils.getApplicationInfo(pm, packageName);

        RequestDialogParam param = new RequestDialogParam();
        param.icon = true;
        param.title = info.loadLabel(pm).toString();
        param.message = clip2SimpleText(clipData);
        param.positive = "允许";
        param.negative = "拒绝";
        param.remember = "记住我的选择";
        param.cancelable = false;
        param.callback = mCallback;

        dm.show(param);
    }


    private static final class TransactionWrapper {
        private static final String TAG = "TransactionWrapper";

        TransactionWrapper(IBinder object, int code, Parcel data, int flags) {
            this.object = object;
            this.code = code;
            this.flags = flags;

            // 由于切换了线程，parcel 会失效，因此要使用中间变量
            data.setDataPosition(0);
            this.data = data.marshall();
        }

        final int code;
        final int flags;
        final IBinder object;
        final byte[] data;

        Context context;
        ClipData clipData;
        String packageName;

        boolean transact() {
            Parcel _data = Parcel.obtain();
            Parcel _reply = Parcel.obtain();

            boolean result = true;
            try {
                _data.unmarshall(data, 0, data.length);
                object.transact(code, _data, _reply, flags);
            } catch (Throwable t) {
                result = false;
                XposedBridge.log(TAG + ": transact: failed");
                XposedBridge.log(t);
            } finally {
                _data.recycle();
                _reply.recycle();
            }
            return result;
        }
    }

    private final class Callback extends OnRequestFinishListener {
        private static final String TAG = "Callback";

        /**
         * 将会被 system_server 的主线程调用
         * 尽可能捕获住任何异常，防止宿主进程崩溃
         * @param result {@link OnRequestFinishListener}
         * @throws RemoteException e
         */
        @Override
        public void onRequestFinish(int result) throws RemoteException {
            try {
                handleResult(result);
            } catch (Throwable t) {
                XposedBridge.log(TAG + ": onRequestFinish: failed");
                XposedBridge.log(t);
            }
        }

        private void handleResult(int result) {
            XposedBridge.log(TAG + ": onRequestFinish: received result: " + result);

            final TransactionWrapper wrapper = mWorkingTransaction;
            mWorkingTransaction = null;

            final int packageRule;
            final Context context = wrapper.context;
            final String packageName = wrapper.packageName;
            final ClipData clipData = wrapper.clipData;

            if ((result & RESULT_POSITIVE) != 0) {      // 允许访问剪贴板
                packageRule = PackageRule.RULE_GRANT;
                saveActionRecord(context, packageName, clipData, ActionRecord.ACTION_GRANT);
                wrapper.transact();
            }
            else {  // 只要没有明确允许，就是拒绝
                packageRule = PackageRule.RULE_DENY;
                saveActionRecord(context, packageName, clipData, ActionRecord.ACTION_DENY);
            }

            if ((result & RESULT_REMEMBER) != 0) {
                savePackageRule(context, packageName, packageRule);
            }

            XposedBridge.log(TAG + ": onRequestFinish: done");
        }
    }
}
