package cn.nlifew.clipmgr.ui.main.rule;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.util.Log;

import org.litepal.LitePal;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.nlifew.clipmgr.app.ThisApp;
import cn.nlifew.clipmgr.bean.PackageRule;
import cn.nlifew.clipmgr.bean.UserRule;
import cn.nlifew.clipmgr.settings.Settings;

class LoadPackageRuleTask extends AsyncTask<Void, Void, List<PackageRuleWrapper>> {
    private static final String TAG = "LoadPackageRuleTask";

    private final WeakReference<PackageRuleAdapter> mAdapter;
    private final PackageManager mPm;
    private final String me;

    LoadPackageRuleTask(PackageRuleAdapter adapter) {
        mAdapter = new WeakReference<>(adapter);
        mPm = adapter.mContext.getPackageManager();
        me = adapter.mContext.getPackageName();
    }

    @Override
    protected List<PackageRuleWrapper> doInBackground(Void... voids) {

        List<PackageRule> rules = LitePal.findAll(PackageRule.class);
        Map<String, PackageRule> old = new HashMap<>(rules.size());
        for (PackageRule r : rules) {
            old.put(r.getPkg(), r);
        }

        boolean showSystemApp = Settings
                .getInstance(ThisApp.getContext())
                .isShowSystemApp();

        List<PackageInfo> apps = mPm.getInstalledPackages(0);
        List<PackageRuleWrapper> wrappers = new ArrayList<>(apps.size());

        for (PackageInfo app : apps) {

            // 过滤掉不喜欢的 app (

            if ((! showSystemApp && (app.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0)
                || me.equals(app.packageName)) {
                continue;
            }

            PackageRule rule = old.get(app.packageName);
            if (rule == null) {
                rule = new PackageRule(app.packageName, PackageRule.RULE_REQUEST);
            }
            PackageRuleWrapper wrapper = new PackageRuleWrapper(rule);
            wrappers.add(wrapper);

            wrapper.appName = app.applicationInfo.loadLabel(mPm).toString();
            wrapper.icon = app.applicationInfo.loadIcon(mPm);
            wrapper.pkg = rule.getPkg();
        }
        return wrappers;
    }

    @Override
    protected void onPostExecute(List<PackageRuleWrapper> userRuleWrappers) {
        final PackageRuleAdapter adapter = mAdapter.get();
        if (adapter == null) {
            Log.w(TAG, "onPostExecute: adapter has been recycled");
            return;
        }
        adapter.updateDataSet(userRuleWrappers);
        adapter.stopLoading();
    }
}
