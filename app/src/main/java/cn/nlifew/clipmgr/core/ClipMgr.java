package cn.nlifew.clipmgr.core;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import org.litepal.LitePal;

import java.util.LinkedList;
import java.util.List;

import cn.nlifew.clipmgr.R;
import cn.nlifew.clipmgr.bean.ActionRecord;
import cn.nlifew.clipmgr.bean.PackageRule;
import cn.nlifew.clipmgr.ui.request.RequestActivity;
import cn.nlifew.clipmgr.util.PackageUtils;
import cn.nlifew.clipmgr.util.ToastUtils;

import static android.content.Context.CLIPBOARD_SERVICE;

/**
 * 注意：这个类的实例在 ClipMgrService 的 onBind() 函数中初始化
 * 如果要在这个类操作 UI，请注意转移到主线程中进行
 */

public class ClipMgr extends IClipMgr.Stub {
    private static final String TAG = "ClipMgr";

    public ClipMgr(Context c) {
        mContext = c;
        mH = new Handler(Looper.getMainLooper());
    }

    private final Context mContext;
    private final Handler mH;

    private final LinkedList<Intent> mRequestCaches = new LinkedList<>();

    @Override
    public void setPrimaryClip(final String pkg, final ClipData clip) {
        Log.d(TAG, "setPrimaryClip: " + pkg + " " + clip);

        // 查询规则
        PackageRule pkgRule = LitePal
                .where(PackageRule.COLUMN_PACKAGE + " = ?", pkg)
                .findFirst(PackageRule.class);
        int rule = pkgRule == null ? PackageRule.RULE_REQUEST : pkgRule.getRule();

        final String appName = PackageUtils.getAppName(mContext, pkg);

        switch (rule) {
            case PackageRule.RULE_GRANT: {
                mH.post(new Runnable() {
                    @Override
                    public void run() {
                        onGrantClipPerm(pkg, appName, clip);
                    }
                });
                break;
            }
            case PackageRule.RULE_DENY: {
                mH.post(new Runnable() {
                    @Override
                    public void run() {
                        onDenyClipPerm(pkg, appName, clip);
                    }
                });
                break;
            }
            case PackageRule.RULE_REQUEST: {
                mH.post(new Runnable() {
                    @Override
                    public void run() {
                        onRequestClipPerm(pkg, appName, clip);
                    }
                });
                break;
            }
        }
    }

    private void onGrantClipPerm(String pkg, String name, ClipData clip) {
        final ClipboardManager cm = (ClipboardManager) mContext
                .getSystemService(CLIPBOARD_SERVICE);
        if (cm == null) {
            ToastUtils.getInstance(mContext).show("无法获取剪贴板服务");
            return;
        }
        cm.setPrimaryClip(clip);

        // 弹出一个 Toast 通知用户

        String text = clip2text(clip);
        StringBuilder sb = new StringBuilder(64);
        sb.append("已允许").append(name).append("修改剪贴板为: ");
        if (text.length() <= 20) {
            sb.append(text);
        } else {
            sb.append(text, 0, 20).append("...");
        }
        ToastUtils.getInstance(mContext).show(sb);

        // 保存这次操作到数据库
        ActionRecord record = new ActionRecord(name, pkg, text,
                ActionRecord.ACTION_GRANT, System.currentTimeMillis());
        record.save();
    }

    private void onDenyClipPerm(String pkg, String name, ClipData clip) {
        String text = clip2text(clip);
        StringBuilder sb = new StringBuilder(64);
        sb.append("已拒绝").append(name).append("修改剪贴板为: ");
        if (text.length() <= 20) {
            sb.append(text);
        } else {
            sb.append(text, 0, 20).append("...");
        }
        ToastUtils.getInstance(mContext).show(sb);

        ActionRecord record = new ActionRecord(name, pkg, text,
                ActionRecord.ACTION_DENY, System.currentTimeMillis());
        record.save();
    }

    private void onRequestClipPerm(final String pkg, final String name, final ClipData clip) {
        String text = clip2text(clip);

        final StringBuilder msg = new StringBuilder(64);
        msg.append('\n').append("尝试修改剪贴板为: ");
        if (text.length() <= 20) {
            msg.append(text);
        } else {
            msg.append(text, 0, 20).append("...");
        }

        /* Builder 的 id 必须为唯一值
         * 这里我们并没有使用包名 pkg 作为 Builder 的 id，
         * 防止遇到连续多次操作剪贴板的情况时，
         * 后面的回调替换掉前面的
         */
        Intent intent = new RequestActivity.Builder(String.valueOf(System.currentTimeMillis()))
                .setTitle(name)
                .setMessage(msg)
                .setPositive("确定")
                .setNegative("取消")
                .setCancelable(false)
                .setRemember("记住我的选择")
                .setCallback(new ActivityRequestCallback(pkg, name, clip))
                .build(mContext);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);


        // 添加进回调队列中
        mRequestCaches.addLast(intent);
        if (mRequestCaches.size() == 1) {
            mContext.startActivity(intent);
        }
    }

    private class ActivityRequestCallback implements
            RequestActivity.OnRequestFinishListener {

        ActivityRequestCallback(String pkg, String name, ClipData clip) {
            mPkg = pkg;
            mName = name;
            mClip = clip;
        }

        private final String mPkg;
        private final String mName;
        private final ClipData mClip;

        @Override
        public void onRequestFinish(String id, int result) {
            int rule;
            if ((result & RESULT_NEGATIVE) != 0) {
                rule = PackageRule.RULE_DENY;
                onDenyClipPerm(mPkg, mName, mClip);
            }
            else {
                // 放宽一下要求，只要不拒绝，就认为同意
                rule = PackageRule.RULE_GRANT;
                onGrantClipPerm(mPkg, mName, mClip);
            }

            if ((result & RESULT_REMEMBER) != 0) {
                // 记住我的选择
                new PackageRule(mPkg, rule).save();
            }

            // 通知下一个 Intent 可以进行请求了
            mRequestCaches.removeFirst();

            /* 关于为什么要向主线程 post 一下而不是直接调用
             * 原因在于：当这个方法被回调时，RequestActivity 还未 finish，
             * 而我们的 Intent 又添加了 FLAG_ACTIVITY_NEW_TASK，
             * 如果此时 startActivity()，这个 Activity 是不会出来的
             */
            mH.post(new Runnable() {
                @Override
                public void run() {
                    if (mRequestCaches.size() != 0) {
                        mContext.startActivity(mRequestCaches.get(0));
                    }
                }
            });
        }
    }

    private static String clip2text(ClipData clip) {
        int n;
        if (clip == null || (n = clip.getItemCount()) == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder(64);
        for (int i = 0; i < n; i++) {
            sb.append(clip.getItemAt(i).getText());
        }
        return sb.toString();
    }
}
