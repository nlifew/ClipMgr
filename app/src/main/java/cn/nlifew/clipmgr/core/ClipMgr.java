package cn.nlifew.clipmgr.core;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.util.Log;

import org.litepal.LitePal;

import cn.nlifew.clipmgr.bean.ActionRecord;
import cn.nlifew.clipmgr.bean.PackageRule;
import cn.nlifew.clipmgr.settings.Settings;
import cn.nlifew.clipmgr.util.PackageUtils;
import cn.nlifew.clipmgr.util.ToastUtils;

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
    public void saveActionRecord(String pkg, final ClipData clip, int action) {
        Log.d(TAG, "saveActionRecord: " + pkg + " " + clip + " " + action);

        String text = clip2text(clip);
        String appName = PackageUtils.getAppName(mContext, pkg);
        final boolean grant = action == ActionRecord.ACTION_GRANT;

        final StringBuilder sb = new StringBuilder(64);
        sb.append(grant ? "已允许" : "已拒绝").append(appName)
                .append("修改剪贴板为: ");
        if (text.length() <= 20) {
            sb.append(text);
        } else {
            sb.append(text, 0, 20).append("...");
        }
        mH.post(new Runnable() {
            @Override
            public void run() {
                ToastUtils.getInstance(mContext).show(sb);

                // 下面的必须放到主线程执行
                // 原因是 ClipboardManager 实例时会调用 Looper.myLooper()
                ClipboardManager cm;
                if (grant && (cm = (ClipboardManager) mContext
                    .getSystemService(Context.CLIPBOARD_SERVICE)) != null) {
                    cm.setPrimaryClip(clip);
                }
            }
        });
        new ActionRecord(appName, pkg, text, action).save();
    }

    @Override
    public synchronized int getPackageRule(String pkg) throws RemoteException {
        PackageRule rule = LitePal
                .where(PackageRule.COLUMN_PACKAGE + " = ?", pkg)
                .findFirst(PackageRule.class);
        return rule == null ? PackageRule.RULE_REQUEST : rule.getRule();
    }


    @Override
    public synchronized void setPackageRule(String pkg, int r) throws RemoteException {
        PackageRule rule = LitePal
                .where(PackageRule.COLUMN_PACKAGE + " = ?", pkg)
                .findFirst(PackageRule.class);
        if (rule == null) {
            rule = new PackageRule(pkg, r);
        } else {
            rule.setRule(r);
        }
        rule.save();
    }

    @Override
    public boolean isRadicalMode() {
        return Settings.getInstance(mContext).isRadicalMode();
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
