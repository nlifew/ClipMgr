package cn.nlifew.clipmgr.core;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.util.Log;

import org.litepal.LitePal;

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

    @Override
    public void setPrimaryClip(final String pkg, final ClipData clip) throws RemoteException {
        Log.d(TAG, "setPrimaryClip: " + pkg + " " + clip);

        // 查询规则
        PackageRule pkgRule = LitePal
                .where(PackageRule.COLUMN_PACKAGE + " = ?", pkg)
                .findFirst(PackageRule.class);
        int rule = pkgRule == null ? PackageRule.RULE_REQUEST : pkgRule.getRule();

        final String appName = PackageUtils.getAppName(mContext.getPackageManager(), pkg);

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

        StringBuilder msg = new StringBuilder(64);
        msg.append('\n').append(name).append("尝试修改剪贴板为: ");
        if (text.length() <= 20) {
            msg.append(text);
        } else {
            msg.append(text, 0, 20).append("...");
        }

        RequestActivity.Builder builder = new RequestActivity.Builder(pkg);
        Intent intent = builder
                .setTitle(mContext.getString(R.string.app_name))
                .setMessage(msg)
                .setPositive("确定")
                .setNegative("取消")
                .setCancelable(false)
                .setRemember("记住我的选择")
                .setCallback(new RequestActivity.OnRequestFinishListener() {
                    @Override
                    public void onRequestFinish(String id, int result) {
                        int rule;
                        if ((result & RESULT_POSITIVE) != 0) {
                            rule = PackageRule.RULE_GRANT;
                            onGrantClipPerm(pkg, name, clip);
                        }
                        else if ((result & RESULT_NEGATIVE) != 0) {
                            rule = PackageRule.RULE_DENY;
                            onDenyClipPerm(pkg, name, clip);
                        }
                        else {
                            Log.w(TAG, "onRequestFinish: unknown result: " + result + " " + id);
                            return;
                        }
                        if ((result & RESULT_REMEMBER) != 0) {
                            // 记住我的选择
                            PackageRule pkgRule = new PackageRule(pkg, rule);
                            pkgRule.save();
                        }
                    }
                })
                .build(mContext);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
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
